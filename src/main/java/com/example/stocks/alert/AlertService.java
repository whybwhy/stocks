package com.example.stocks.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.example.stocks.kis.KisPriceFetcher;
import com.example.stocks.kis.KisOverseasPriceFetcher;

/**
 * Supabase에서 알람을 읽고, 국내/해외 현재가와 비교 후:
 *  - is_active=true  + 목표가 도달 → 텔레그램 발송 후 is_active=false
 *  - is_active=false + 현재가가 목표가 반대편으로 복귀 → is_active=true (재활성화)
 *
 * market 구분: KR(국내), NAS/NYS/AMS(미국)
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RestClient supabaseRestClient;
    private final TelegramService telegramService;
    private final KisPriceFetcher kisPriceFetcher;
    private final KisOverseasPriceFetcher overseasPriceFetcher;

    public AlertService(@Qualifier("supabaseRestClient") RestClient supabaseRestClient,
                        TelegramService telegramService,
                        KisPriceFetcher kisPriceFetcher,
                        KisOverseasPriceFetcher overseasPriceFetcher) {
        this.supabaseRestClient = supabaseRestClient;
        this.telegramService = telegramService;
        this.kisPriceFetcher = kisPriceFetcher;
        this.overseasPriceFetcher = overseasPriceFetcher;
    }

    /**
     * 전체 알람 체크. 스케줄러에서 호출됨.
     * KR/US 각각 장 시간일 때만 해당 시장 알람을 체크.
     */
    public void checkAlerts() {
        List<PriceAlertDto> allAlerts = getAllAlerts();
        if (allAlerts.isEmpty()) return;

        ZonedDateTime nowKst = ZonedDateTime.now(KST);

        // 국내 알람 체크
        if (isKrMarketTime(nowKst) && kisPriceFetcher != null && kisPriceFetcher.isConfigured()) {
            List<PriceAlertDto> krAlerts = allAlerts.stream().filter(PriceAlertDto::isKr).collect(Collectors.toList());
            if (!krAlerts.isEmpty()) {
                Set<String> codes = krAlerts.stream().map(PriceAlertDto::getStockCode)
                        .filter(s -> s != null && !s.isBlank()).collect(Collectors.toSet());
                Map<String, BigDecimal> prices = kisPriceFetcher.fetchPrices(codes);
                if (!prices.isEmpty()) processAlerts(krAlerts, prices);
            }
        }

        // 미국 알람 체크
        if (isUsMarketTime(nowKst) && overseasPriceFetcher != null && overseasPriceFetcher.isConfigured()) {
            checkUsAlerts(allAlerts);
        }
    }

    private void checkUsAlerts(List<PriceAlertDto> allAlerts) {
        List<PriceAlertDto> usAlerts = allAlerts.stream().filter(PriceAlertDto::isUs).collect(Collectors.toList());
        if (usAlerts.isEmpty()) return;

        // 거래소별로 그룹핑 (NAS, NYS, AMS)
        Map<String, List<PriceAlertDto>> byExchange = usAlerts.stream()
                .collect(Collectors.groupingBy(a -> a.getMarket().toUpperCase()));

        Map<String, BigDecimal> allPrices = new HashMap<>();

        for (Map.Entry<String, List<PriceAlertDto>> entry : byExchange.entrySet()) {
            String excd = entry.getKey();
            Set<String> symbols = entry.getValue().stream()
                    .map(a -> a.getStockCode().toUpperCase())
                    .collect(Collectors.toSet());
            Map<String, BigDecimal> prices = overseasPriceFetcher.fetchPrices(excd, symbols);
            allPrices.putAll(prices);
        }

        if (!allPrices.isEmpty()) {
            // US 알람의 stock_code를 대문자로 맞춰 비교
            for (PriceAlertDto alert : usAlerts) {
                BigDecimal price = allPrices.get(alert.getStockCode().toUpperCase());
                if (price == null) continue;
                boolean active = Boolean.TRUE.equals(alert.getIsActive());
                if (active && isTriggered(alert, price)) {
                    sendAlert(alert, price);
                    markTriggered(alert.getId());
                } else if (!active && isResetCondition(alert, price)) {
                    reactivate(alert.getId(), price, alert);
                }
            }
        }
    }

    /**
     * WebSocket 실시간 체결가 수신 시 단건 체크 (국내 전용).
     */
    public void checkSingleAlert(String stockCode, BigDecimal currentPrice) {
        if (stockCode == null || currentPrice == null) return;

        List<PriceAlertDto> allAlerts = getAllAlerts();
        List<PriceAlertDto> matched = allAlerts.stream()
                .filter(a -> stockCode.equals(a.getStockCode()))
                .collect(Collectors.toList());

        processAlerts(matched, Map.of(stockCode, currentPrice));
    }

    private void processAlerts(List<PriceAlertDto> alerts, Map<String, BigDecimal> prices) {
        for (PriceAlertDto alert : alerts) {
            BigDecimal currentPrice = prices.get(alert.getStockCode());
            if (currentPrice == null) continue;

            boolean active = Boolean.TRUE.equals(alert.getIsActive());

            if (active) {
                if (isTriggered(alert, currentPrice)) {
                    sendAlert(alert, currentPrice);
                    markTriggered(alert.getId());
                }
            } else {
                if (isResetCondition(alert, currentPrice)) {
                    reactivate(alert.getId(), currentPrice, alert);
                }
            }
        }
    }

    private boolean isTriggered(PriceAlertDto alert, BigDecimal currentPrice) {
        if (alert.getTargetPrice() == null) return false;
        if ("ABOVE".equalsIgnoreCase(alert.getCondition())) {
            return currentPrice.compareTo(alert.getTargetPrice()) >= 0;
        } else if ("BELOW".equalsIgnoreCase(alert.getCondition())) {
            return currentPrice.compareTo(alert.getTargetPrice()) <= 0;
        }
        return false;
    }

    private boolean isResetCondition(PriceAlertDto alert, BigDecimal currentPrice) {
        if (alert.getTargetPrice() == null) return false;
        if ("ABOVE".equalsIgnoreCase(alert.getCondition())) {
            return currentPrice.compareTo(alert.getTargetPrice()) < 0;
        } else if ("BELOW".equalsIgnoreCase(alert.getCondition())) {
            return currentPrice.compareTo(alert.getTargetPrice()) > 0;
        }
        return false;
    }

    private void sendAlert(PriceAlertDto alert, BigDecimal currentPrice) {
        String symbolDisplay = alert.getSymbol() != null && !alert.getSymbol().isBlank()
                ? alert.getSymbol() : alert.getStockCode();

        String flag = alert.isUs() ? "🇺🇸" : "🇰🇷";
        String currency = alert.isUs() ? "$" : "";
        String unit = alert.isUs() ? "" : "원";

        StringBuilder sb = new StringBuilder();
        sb.append(flag).append(" <b>").append(symbolDisplay).append("</b>\n");
        sb.append("목표 : ").append(currency).append(formatPrice(alert.getTargetPrice(), alert.isUs())).append(unit).append("\n");
        sb.append("현재 : <b>").append(currency).append(formatPrice(currentPrice, alert.isUs())).append(unit).append("</b>");

        String label = alert.getLabel();
        if (label != null && !label.isBlank()) {
            sb.append("\n").append(label);
        }

        String msg = sb.toString();

        telegramService.broadcast(msg);
        log.info("ALERT TRIGGERED: {} {} target={} current={}",
                symbolDisplay, alert.getCondition(), alert.getTargetPrice(), currentPrice);
    }

    private String formatPrice(BigDecimal price, boolean isUs) {
        if (price == null) return "-";
        if (isUs) {
            return String.format("%,.2f", price);
        }
        if (price.scale() <= 0 || price.stripTrailingZeros().scale() <= 0) {
            return String.format("%,.0f", price);
        }
        return String.format("%,.2f", price);
    }

    // ─── 장 시간 판단 ───

    /** 한국 장: 평일 08:50~15:35 KST */
    static boolean isKrMarketTime(ZonedDateTime nowKst) {
        DayOfWeek dow = nowKst.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return false;
        LocalTime t = nowKst.toLocalTime();
        return !t.isBefore(LocalTime.of(8, 50)) && !t.isAfter(LocalTime.of(15, 35));
    }

    /**
     * 미국 장: 정규장 09:30~16:00 ET → KST 23:30~06:00 (서머타임 22:30~05:00)
     * 여유 있게 KST 22:00~07:00 범위로 체크.
     */
    static boolean isUsMarketTime(ZonedDateTime nowKst) {
        DayOfWeek dow = nowKst.getDayOfWeek();
        LocalTime t = nowKst.toLocalTime();

        // 일요일 전체: 운영 없음
        if (dow == DayOfWeek.SUNDAY) return false;

        // 토요일 새벽 00:00~07:00: 금요 야간
        if (dow == DayOfWeek.SATURDAY) {
            return t.isBefore(LocalTime.of(7, 0));
        }

        // 평일 22:00~23:59 (당일 야간 시작)
        boolean nightStart = !t.isBefore(LocalTime.of(22, 0));
        // 평일 00:00~07:00 (전일 야간 연장)
        boolean earlyMorning = t.isBefore(LocalTime.of(7, 0));

        return nightStart || earlyMorning;
    }

    // ─── Supabase CRUD ───

    public List<PriceAlertDto> getActiveAlerts() {
        try {
            List<PriceAlertDto> list = supabaseRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/price_alerts")
                            .queryParam("select", "*")
                            .queryParam("is_active", "eq.true")
                            .queryParam("order", "id.asc")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PriceAlertDto>>() {});
            return list != null ? list : List.of();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("Failed to fetch active alerts: {} cause={}", e.getMessage(), cause.getMessage(), e);
            return List.of();
        }
    }

    public List<PriceAlertDto> getAllAlerts() {
        try {
            List<PriceAlertDto> list = supabaseRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/price_alerts")
                            .queryParam("select", "*")
                            .queryParam("order", "id.asc")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PriceAlertDto>>() {});
            return list != null ? list : List.of();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("Failed to fetch alerts: {} cause={}", e.getMessage(), cause.getMessage(), e);
            return List.of();
        }
    }

    public PriceAlertDto createAlert(PriceAlertDto dto) {
        dto.setIsActive(true);
        if (dto.getMarket() == null || dto.getMarket().isBlank()) {
            dto.setMarket("KR");
        }
        List<PriceAlertDto> result = supabaseRestClient.post()
                .uri("/rest/v1/price_alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Prefer", "return=representation")
                .body(dto)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PriceAlertDto>>() {});
        return result != null && !result.isEmpty() ? result.get(0) : dto;
    }

    public void deleteAlert(Long id) {
        supabaseRestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/price_alerts")
                        .queryParam("id", "eq." + id)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }

    private void markTriggered(Long id) {
        String now = ZonedDateTime.now(KST).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String body = "{\"is_active\": false, \"triggered_at\": \"" + now + "\"}";
        try {
            supabaseRestClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/price_alerts")
                            .queryParam("id", "eq." + id)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to mark alert {} as triggered: {}", id, e.getMessage());
        }
    }

    private void reactivate(Long id, BigDecimal currentPrice, PriceAlertDto alert) {
        String symbolDisplay = alert.getSymbol() != null && !alert.getSymbol().isBlank()
                ? alert.getSymbol() : alert.getStockCode();
        try {
            supabaseRestClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/price_alerts")
                            .queryParam("id", "eq." + id)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"is_active\": true}")
                    .retrieve()
                    .toBodilessEntity();
            log.info("ALERT REACTIVATED: {} target={} currentPrice={}",
                    symbolDisplay, alert.getTargetPrice(), currentPrice);
        } catch (Exception e) {
            log.error("Failed to reactivate alert {}: {}", id, e.getMessage());
        }
    }
}
