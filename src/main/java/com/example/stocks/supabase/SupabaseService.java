package com.example.stocks.supabase;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupabaseService {

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

