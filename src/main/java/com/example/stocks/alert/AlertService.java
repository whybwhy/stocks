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

/**
 * Supabase에서 알람을 읽고, 한국투자증권 API 현재가와 비교 후:
 *  - is_active=true  + 목표가 도달 → 텔레그램 발송 후 is_active=false
 *  - is_active=false + 현재가가 목표가 아래로 복귀 → is_active=true (재활성화)
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RestClient supabaseRestClient;
    private final TelegramService telegramService;
    private final KisPriceFetcher kisPriceFetcher;

    public AlertService(@Qualifier("supabaseRestClient") RestClient supabaseRestClient,
                        TelegramService telegramService,
                        KisPriceFetcher kisPriceFetcher) {
        this.supabaseRestClient = supabaseRestClient;
        this.telegramService = telegramService;
        this.kisPriceFetcher = kisPriceFetcher;
    }

    /**
     * 활성/비활성 알람 전체 체크. 스케줄러에서 호출됨.
     */
    public void checkAlerts() {
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        if (!isMarketCheckTime(nowKst)) {
            log.debug("Skipping alerts - outside KR market hours");
            return;
        }

        if (kisPriceFetcher == null || !kisPriceFetcher.isConfigured()) {
            log.warn("KIS API not configured; skipping price alerts");
            return;
        }

        List<PriceAlertDto> allAlerts = getAllAlerts();
        if (allAlerts.isEmpty()) return;

        Set<String> stockCodes = allAlerts.stream()
                .map(PriceAlertDto::getStockCode)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toSet());

        Map<String, BigDecimal> prices = kisPriceFetcher.fetchPrices(stockCodes);
        if (prices.isEmpty()) return;

        processAlerts(allAlerts, prices);
    }

    /**
     * WebSocket 실시간 체결가 수신 시 단건 체크.
     */
    public void checkSingleAlert(String stockCode, BigDecimal currentPrice) {
        if (stockCode == null || currentPrice == null) return;

        List<PriceAlertDto> allAlerts = getAllAlerts();
        List<PriceAlertDto> matched = allAlerts.stream()
                .filter(a -> stockCode.equals(a.getStockCode()))
                .collect(Collectors.toList());

        processAlerts(matched, Map.of(stockCode, currentPrice));
    }

    /**
     * 알람 목록과 현재가 맵으로:
     * - 활성 알람: 목표가 도달 시 발송 + 비활성화
     * - 비활성 알람: 현재가가 목표가 아래로 복귀 시 재활성화
     */
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

    /**
     * 목표가 도달 여부.
     * ABOVE: 현재가 >= 목표가
     * BELOW: 현재가 <= 목표가
     */
    private boolean isTriggered(PriceAlertDto alert, BigDecimal currentPrice) {
        if (alert.getTargetPrice() == null) return false;
        if ("ABOVE".equalsIgnoreCase(alert.getCondition())) {
            return currentPrice.compareTo(alert.getTargetPrice()) >= 0;
        } else if ("BELOW".equalsIgnoreCase(alert.getCondition())) {
            return currentPrice.compareTo(alert.getTargetPrice()) <= 0;
        }
        return false;
    }

    /**
     * 재활성화 조건: 현재가가 목표가 반대편으로 복귀.
     * ABOVE: 현재가 < 목표가 (목표가 아래로 떨어짐)
     * BELOW: 현재가 > 목표가 (목표가 위로 올라감)
     */
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
        String conditionText = "ABOVE".equalsIgnoreCase(alert.getCondition()) ? "이상 돌파" : "이하 도달";
        String label = alert.getLabel() != null && !alert.getLabel().isBlank()
                ? " (" + alert.getLabel() + ")" : "";
        String symbolDisplay = alert.getSymbol() != null && !alert.getSymbol().isBlank()
                ? alert.getSymbol() : alert.getStockCode();

        String msg = String.format(
                "🇰🇷 <b>%s</b>%s\n목표가 %s %s\n현재가 <b>%s</b>",
                symbolDisplay,
                label,
                formatPrice(alert.getTargetPrice()),
                conditionText,
                formatPrice(currentPrice)
        );

        telegramService.broadcast(msg);
        log.info("ALERT TRIGGERED: {} {} target={} current={}",
                symbolDisplay, alert.getCondition(), alert.getTargetPrice(), currentPrice);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "-";
        if (price.scale() <= 0 || price.stripTrailingZeros().scale() <= 0) {
            return String.format("%,.0f", price);
        }
        return String.format("%,.2f", price);
    }

    /**
     * 한국 장 시간: 평일 08:50~15:35 KST
     */
    static boolean isMarketCheckTime(ZonedDateTime nowKst) {
        DayOfWeek dow = nowKst.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return false;
        LocalTime t = nowKst.toLocalTime();
        return !t.isBefore(LocalTime.of(8, 50)) && !t.isAfter(LocalTime.of(15, 35));
    }

    // ─── Supabase CRUD ───

    /** is_active=true 인 알람만 조회 (스케줄러용) */
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

    /** 전체 알람 조회 (활성 + 비활성) */
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
            log.info("ALERT REACTIVATED: {} target={} currentPrice={} (price dropped below target)",
                    symbolDisplay, alert.getTargetPrice(), currentPrice);
        } catch (Exception e) {
            log.error("Failed to reactivate alert {}: {}", id, e.getMessage());
        }
    }
}
