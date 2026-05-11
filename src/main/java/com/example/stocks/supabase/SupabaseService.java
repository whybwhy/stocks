package com.example.stocks.supabase;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.example.stocks.alert.PriceAlertDto;
import com.example.stocks.alert.PriceAlertTriggerDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupabaseService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseService.class);

    private final RestClient supabaseRestClient;

    public SupabaseService(@Qualifier("supabaseRestClient") RestClient supabaseRestClient) {
        this.supabaseRestClient = supabaseRestClient;
    }

    public String getTableRows(String table, String select) {
        return supabaseRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + table)
                        .queryParam("select", select)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    /** 목록 조회: id 내림차순, 페이징. 기본 25개씩 사용 시 size=25, page 1부터. */
    public StockPageResult getStocks(String service, int limit, int offset) {
        ResponseEntity<List<StockDto>> response = supabaseRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + service)
                        .queryParam("select", "*")
                        .queryParam("order", "id.desc")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .header("Prefer", "count=exact")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<StockDto>>() {});
        List<StockDto> list = response.getBody() != null ? response.getBody() : List.of();
        long total = parseTotalFromContentRange(response.getHeaders().getFirst("Content-Range"));
        return new StockPageResult(list, total);
    }

    private static long parseTotalFromContentRange(String contentRange) {
        if (contentRange == null || !contentRange.contains("/")) return 0;
        try {
            return Long.parseLong(contentRange.substring(contentRange.indexOf('/') + 1).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static final String PRICE_ALERTS_TABLE = "price_alerts";
    private static final String PRICE_ALERT_TRIGGERS_TABLE = "price_alert_triggers";

    private static final String PRICE_ALERT_SELECT_COLUMNS =
            "id,market,stock_code,symbol,target_price,condition,label,source,is_active,triggered_at,created_at";
    private static final String PRICE_ALERT_TRIGGER_SELECT_COLUMNS =
            "id,alert_id,market,stock_code,symbol,target_price,condition,trigger_price,label,source,triggered_at";

    /**
     * price_alerts 목록 (id 내림차순). source가 null/blank/ALL 이면 전체.
     * 그 외에는 CHARTBOY, MY, MANUAL 중 하나로 필터.
     *
     * @param exactSymbolOrCode 이미 검증된 검색어 — {@code symbol} 또는 {@code stock_code} 와 <strong>전체 일치</strong>(PostgREST {@code or})
     */
    public PriceAlertPageResult getPriceAlerts(int limit, int offset, String source,
                                               Optional<String> exactSymbolOrCode) {
        ResponseEntity<List<PriceAlertDto>> response = supabaseRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/rest/v1/" + PRICE_ALERTS_TABLE)
                            .queryParam("select", PRICE_ALERT_SELECT_COLUMNS)
                            .queryParam("order", "id.desc")
                            .queryParam("limit", limit)
                            .queryParam("offset", offset);
                    String s = source != null ? source.trim() : "";
                    if (!s.isEmpty() && !"ALL".equalsIgnoreCase(s)) {
                        uriBuilder.queryParam("source", "eq." + s);
                    }
                    exactSymbolOrCode.ifPresent(v ->
                            uriBuilder.queryParam("or", postgrestSymbolOrStockCodeOr(v)));
                    return uriBuilder.build();
                })
                .header("Prefer", "count=estimated")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<PriceAlertDto>>() {});
        List<PriceAlertDto> list = response.getBody() != null ? response.getBody() : List.of();
        long total = parseTotalFromContentRange(response.getHeaders().getFirst("Content-Range"));
        return new PriceAlertPageResult(list, total);
    }

    /**
     * PostgREST: (symbol.eq."…",stock_code.eq."…") — 값은 애플리케이션에서 화이트리스트 검증 후 전달.
     */
    private static String postgrestSymbolOrStockCodeOr(String value) {
        String q = postgrestDoubleQuoted(value);
        return "(symbol.eq." + q + ",stock_code.eq." + q + ")";
    }

    /**
     * 검색창 자동완성용: {@code symbol}/{@code stock_code} 부분 일치(ilike)로 distinct 종목 후보를 반환합니다.
     * 값은 호출 측에서 화이트리스트 검증 후 전달해야 합니다(쉼표·따옴표·{@code *}/{@code %} 미허용).
     *
     * @param prefix 검증된 검색어 (1자 이상)
     * @param source CHARTBOY/MY/MANUAL/ALL — null·blank·ALL 이면 전체
     * @param max    최대 후보 개수 (1..50)
     */
    public List<Map<String, String>> suggestPriceAlerts(String prefix, String source, int max) {
        if (prefix == null || prefix.isBlank()) return List.of();
        int safeMax = Math.max(1, Math.min(50, max));
        // PostgREST or=()· ilike — 따옴표·% 형식이 인코딩에 가장 안정적
        String pat = postgrestDoubleQuoted("%" + prefix + "%");
        String orFilter = "(symbol.ilike." + pat + ",stock_code.ilike." + pat + ")";
        List<Map<String, Object>> rows = supabaseRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/rest/v1/" + PRICE_ALERTS_TABLE)
                            .queryParam("select", "symbol,stock_code")
                            .queryParam("or", orFilter)
                            .queryParam("order", "id.desc")
                            .queryParam("limit", safeMax * 4); // 후보 여유분 → 앱에서 distinct
                    String s = source != null ? source.trim() : "";
                    if (!s.isEmpty() && !"ALL".equalsIgnoreCase(s)) {
                        uriBuilder.queryParam("source", "eq." + s);
                    }
                    return uriBuilder.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (rows == null || rows.isEmpty()) return List.of();

        java.util.LinkedHashMap<String, Map<String, String>> distinct = new java.util.LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            String symbol = r.get("symbol") != null ? r.get("symbol").toString().trim() : "";
            String code = r.get("stock_code") != null ? r.get("stock_code").toString().trim() : "";
            String key = symbol + "|" + code;
            if (key.equals("|")) continue;
            distinct.computeIfAbsent(key, k -> Map.of("symbol", symbol, "stockCode", code));
            if (distinct.size() >= safeMax) break;
        }
        return new java.util.ArrayList<>(distinct.values());
    }

    private static String postgrestDoubleQuoted(String value) {
        String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    /**
     * 목표가 돌파 로그 1건 저장. 실패 시 로그만 남기고 예외는 삼킵니다 (알람 처리 흐름 유지).
     */
    public void insertPriceAlertTrigger(PriceAlertDto alert, BigDecimal triggerPrice, String triggeredAtIso) {
        if (alert == null || alert.getId() == null || triggerPrice == null) {
            return;
        }
        try {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("alert_id", alert.getId());
            row.put("market", alert.getMarket() != null && !alert.getMarket().isBlank() ? alert.getMarket().trim() : "KR");
            row.put("stock_code", alert.getStockCode() != null ? alert.getStockCode().trim() : "");
            row.put("symbol", trimOrNull(alert.getSymbol()));
            row.put("target_price", alert.getTargetPrice());
            row.put("condition", alert.getCondition() != null && !alert.getCondition().isBlank()
                    ? alert.getCondition().trim() : "ABOVE");
            row.put("trigger_price", triggerPrice);
            row.put("label", trimOrNull(alert.getLabel()));
            row.put("source", alert.getSource() != null && !alert.getSource().isBlank() ? alert.getSource().trim() : "MY");
            if (triggeredAtIso != null && !triggeredAtIso.isBlank()) {
                row.put("triggered_at", triggeredAtIso);
            }
            supabaseRestClient.post()
                    .uri("/rest/v1/" + PRICE_ALERT_TRIGGERS_TABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Prefer", "return=minimal")
                    .body(row)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("insertPriceAlertTrigger failed alertId={}: {}", alert.getId(), e.getMessage());
        }
    }

    /**
     * 돌파 로그 목록. {@code source} 가 비어 있지 않으면 해당 출처로 필터.
     * {@code q} 가 있으면 종목명·코드 부분 일치({@code ilike}).
     */
    public PriceAlertTriggerPageResult getPriceAlertTriggers(int limit, int offset, String source,
                                                             Optional<String> q) {
        ResponseEntity<List<PriceAlertTriggerDto>> response = supabaseRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/rest/v1/" + PRICE_ALERT_TRIGGERS_TABLE)
                            .queryParam("select", PRICE_ALERT_TRIGGER_SELECT_COLUMNS)
                            .queryParam("order", "triggered_at.desc")
                            .queryParam("limit", limit)
                            .queryParam("offset", offset);
                    String s = source != null ? source.trim() : "";
                    if (!s.isEmpty() && !"ALL".equalsIgnoreCase(s)) {
                        uriBuilder.queryParam("source", "eq." + s);
                    }
                    q.ifPresent(v -> {
                        String pat = postgrestDoubleQuoted("%" + v + "%");
                        uriBuilder.queryParam("or", "(symbol.ilike." + pat + ",stock_code.ilike." + pat + ")");
                    });
                    return uriBuilder.build();
                })
                .header("Prefer", "count=estimated")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<PriceAlertTriggerDto>>() {});
        List<PriceAlertTriggerDto> list = response.getBody() != null ? response.getBody() : List.of();
        long total = parseTotalFromContentRange(response.getHeaders().getFirst("Content-Range"));
        return new PriceAlertTriggerPageResult(list, total);
    }

    /**
     * 돌파 로그 화면 자동완성: 트리거 로그에 나온 종목명·코드 부분 일치 후보.
     */
    public List<Map<String, String>> suggestPriceAlertTriggers(String prefix, String source, int max) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }
        int safeMax = Math.max(1, Math.min(50, max));
        String pat = postgrestDoubleQuoted("%" + prefix + "%");
        String orFilter = "(symbol.ilike." + pat + ",stock_code.ilike." + pat + ")";
        List<Map<String, Object>> rows = supabaseRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/rest/v1/" + PRICE_ALERT_TRIGGERS_TABLE)
                            .queryParam("select", "symbol,stock_code")
                            .queryParam("or", orFilter)
                            .queryParam("order", "triggered_at.desc")
                            .queryParam("limit", safeMax * 6);
                    String s = source != null ? source.trim() : "";
                    if (!s.isEmpty() && !"ALL".equalsIgnoreCase(s)) {
                        uriBuilder.queryParam("source", "eq." + s);
                    }
                    return uriBuilder.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        LinkedHashMap<String, Map<String, String>> distinct = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            String symbol = r.get("symbol") != null ? r.get("symbol").toString().trim() : "";
            String code = r.get("stock_code") != null ? r.get("stock_code").toString().trim() : "";
            String key = symbol + "|" + code;
            if (key.equals("|")) {
                continue;
            }
            distinct.computeIfAbsent(key, k -> Map.of("symbol", symbol, "stockCode", code));
            if (distinct.size() >= safeMax) {
                break;
            }
        }
        return new java.util.ArrayList<>(distinct.values());
    }

    public PriceAlertDto getPriceAlertById(Long id) {
        if (id == null) return null;
        List<PriceAlertDto> list = supabaseRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + PRICE_ALERTS_TABLE)
                        .queryParam("select", "*")
                        .queryParam("id", "eq." + id)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PriceAlertDto>>() {});
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    /** 신규 price_alerts 행. DB 기본값으로 triggered_at, created_at 설정. */
    public PriceAlertDto insertPriceAlert(PriceAlertDto dto) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("market", dto.getMarket() != null && !dto.getMarket().isBlank() ? dto.getMarket().trim() : "KR");
        row.put("stock_code", dto.getStockCode() != null ? dto.getStockCode().trim() : "");
        row.put("symbol", trimOrNull(dto.getSymbol()));
        row.put("target_price", dto.getTargetPrice());
        row.put("condition", dto.getCondition() != null && !dto.getCondition().isBlank() ? dto.getCondition().trim() : "ABOVE");
        row.put("label", trimOrNull(dto.getLabel()));
        row.put("source", dto.getSource() != null && !dto.getSource().isBlank() ? dto.getSource().trim() : "MY");
        row.put("is_active", dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);
        List<PriceAlertDto> out = supabaseRestClient.post()
                .uri("/rest/v1/" + PRICE_ALERTS_TABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Prefer", "return=representation")
                .body(row)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PriceAlertDto>>() {});
        return out != null && !out.isEmpty() ? out.get(0) : null;
    }

    /**
     * id 기준 부분 수정. clearTriggered 이면 triggered_at 을 null 로 초기화(재알람 가능).
     */
    public void updatePriceAlert(Long id, PriceAlertDto dto, boolean clearTriggered) {
        if (id == null) return;
        Map<String, Object> patch = new LinkedHashMap<>();
        patch.put("market", dto.getMarket() != null && !dto.getMarket().isBlank() ? dto.getMarket().trim() : "KR");
        patch.put("stock_code", dto.getStockCode() != null ? dto.getStockCode().trim() : "");
        patch.put("symbol", trimOrNull(dto.getSymbol()));
        patch.put("target_price", dto.getTargetPrice());
        patch.put("condition", dto.getCondition() != null && !dto.getCondition().isBlank() ? dto.getCondition().trim() : "ABOVE");
        patch.put("label", trimOrNull(dto.getLabel()));
        patch.put("source", dto.getSource() != null && !dto.getSource().isBlank() ? dto.getSource().trim() : "MY");
        patch.put("is_active", dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);
        if (clearTriggered) {
            patch.put("triggered_at", null);
        }
        supabaseRestClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + PRICE_ALERTS_TABLE)
                        .queryParam("id", "eq." + id)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(patch)
                .retrieve()
                .toBodilessEntity();
    }

    public void deletePriceAlert(Long id) {
        if (id == null) return;
        supabaseRestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + PRICE_ALERTS_TABLE)
                        .queryParam("id", "eq." + id)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** 엑셀(CSV) 다운로드용 전체 목록. id 내림차순, 최대 5000건. */
    public List<StockDto> getStocksForExport(String service) {
        StockPageResult result = getStocks(service, 5000, 0);
        return result.list();
    }

    /** stock(종목명)으로 단건 조회. 없으면 null. (동일 종목 있으면 update 하기 위함) */
    public StockDto getStockByStockName(String table, String stockName) {
        if (stockName == null || stockName.isBlank()) return null;
        String filterValue = "eq.\"" + stockName.replace("\"", "\"\"") + "\"";
        List<StockDto> list = supabaseRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + table)
                        .queryParam("select", "*")
                        .queryParam("stock", filterValue)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StockDto>>() {});
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    /** id로 단건 조회. 없으면 null. */
    public StockDto getStockById(String table, Long id) {
        if (id == null) return null;
        List<StockDto> list = supabaseRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + table)
                        .queryParam("select", "*")
                        .queryParam("id", "eq." + id)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StockDto>>() {});
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    /** 신규 삽입. */
    public StockDto insertStock(String table, StockDto dto) {
        return supabaseRestClient.post()
                .uri("/rest/v1/" + table)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Prefer", "return=representation")
                .body(dto)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StockDto>>() {}).get(0);
    }

    /** id로 기존 행 수정. */
    public void updateStock(String table, Long id, StockDto dto) {
        supabaseRestClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + table)
                        .queryParam("id", "eq." + id)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    /** id로 행 삭제. */
    public void deleteStock(String table, Long id) {
        if (id == null) return;
        supabaseRestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + table)
                        .queryParam("id", "eq." + id)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }

    private static final String SERVICE_PERMISSION_TABLE = "service_permission";

    /**
     * 해당 서비스에 대한 허용 닉네임 목록 조회 (service 기준).
     * 0건이면 "전체 허용" 의미.
     */
    public List<String> getAllowedNicknamesForService(String service) {
        if (service == null || service.isBlank()) return List.of();
        try {
            List<ServicePermissionDto> list = supabaseRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/" + SERVICE_PERMISSION_TABLE)
                            .queryParam("select", "nickname")
                            .queryParam("service", "eq." + service.trim())
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ServicePermissionDto>>() {});
            if (list == null) return List.of();
            return list.stream()
                    .map(ServicePermissionDto::getNickname)
                    .filter(n -> n != null && !n.isBlank())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 해당 닉네임이 접근 가능한 서비스 목록 조회 (nickname 기준).
     */
    public List<String> getServicesForNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) return List.of();
        String filter = "eq.\"" + nickname.trim().replace("\"", "\"\"") + "\"";
        try {
            List<ServicePermissionDto> list = supabaseRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/" + SERVICE_PERMISSION_TABLE)
                            .queryParam("select", "service")
                            .queryParam("nickname", filter)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ServicePermissionDto>>() {});
            if (list == null) return List.of();
            return list.stream()
                    .map(ServicePermissionDto::getService)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 해당 닉네임이 해당 서비스 접근 권한이 있는지 여부.
     * 해당 서비스에 권한 행이 0건이면 전체 허용(true), 1건 이상이면 목록에 포함된 경우만 true.
     */
    public boolean hasAccess(String nickname, String service) {
        List<String> allowed = getAllowedNicknamesForService(service);
        if (allowed.isEmpty()) return true;
        if (nickname == null || nickname.isBlank()) return false;
        String n = nickname.trim();
        return allowed.stream()
                .anyMatch(a -> a != null && a.trim().equalsIgnoreCase(n));
    }

    /** 이력 저장 (생성/변경 시 호출). */
    public void insertHistory(String account, String targetTable, Long targetId) {
        HistoryDto dto = new HistoryDto();
        dto.setAccount(account);
        dto.setTargetTable(targetTable);
        dto.setTargetId(targetId);
        supabaseRestClient.post()
                .uri("/rest/v1/history")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }
}

