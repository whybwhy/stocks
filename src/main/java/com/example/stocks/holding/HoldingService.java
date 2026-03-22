package com.example.stocks.holding;

import com.example.stocks.alert.TelegramService;
import com.example.stocks.kis.KisOverseasPriceFetcher;
import com.example.stocks.kis.KisPriceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 보유종목 5%/10% 수익 알림 서비스.
 * 매수가 대비 현재가가 5% 또는 10% 이상 상승하면 텔레그램 알림 발송.
 * 한번 발송된 알림은 notified_5pct / notified_10pct 플래그로 중복 방지.
 */
@Service
public class HoldingService {

    private static final Logger log = LoggerFactory.getLogger(HoldingService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final BigDecimal PCT_5  = new BigDecimal("1.05");
    private static final BigDecimal PCT_10 = new BigDecimal("1.10");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final RestClient supabaseRestClient;
    private final TelegramService telegramService;
    private final KisPriceFetcher kisPriceFetcher;
    private final KisOverseasPriceFetcher overseasPriceFetcher;

    public HoldingService(@Qualifier("supabaseRestClient") RestClient supabaseRestClient,
                          TelegramService telegramService,
                          KisPriceFetcher kisPriceFetcher,
                          KisOverseasPriceFetcher overseasPriceFetcher) {
        this.supabaseRestClient = supabaseRestClient;
        this.telegramService = telegramService;
        this.kisPriceFetcher = kisPriceFetcher;
        this.overseasPriceFetcher = overseasPriceFetcher;
    }

    /**
     * 보유종목 수익 알림 체크. AlertScheduler에서 호출됨.
     */
    public void checkHoldings() {
        List<HoldingDto> holdings = getActiveHoldings();
        if (holdings.isEmpty()) return;

        ZonedDateTime nowKst = ZonedDateTime.now(KST);

        if (isKrMarketTime(nowKst) && kisPriceFetcher != null && kisPriceFetcher.isConfigured()) {
            List<HoldingDto> krHoldings = holdings.stream()
                    .filter(HoldingDto::isKr).collect(Collectors.toList());
            if (!krHoldings.isEmpty()) {
                Set<String> codes = krHoldings.stream()
                        .map(HoldingDto::getStockCode)
                        .filter(s -> s != null && !s.isBlank())
                        .collect(Collectors.toSet());
                Map<String, BigDecimal> prices = kisPriceFetcher.fetchPrices(codes);
                if (!prices.isEmpty()) processHoldings(krHoldings, prices);
            }
        }

        if (isUsMarketTime(nowKst) && overseasPriceFetcher != null && overseasPriceFetcher.isConfigured()) {
            checkUsHoldings(holdings);
        }
    }

    private void checkUsHoldings(List<HoldingDto> holdings) {
        List<HoldingDto> usHoldings = holdings.stream()
                .filter(HoldingDto::isUs).collect(Collectors.toList());
        if (usHoldings.isEmpty()) return;

        Map<String, List<HoldingDto>> byExchange = usHoldings.stream()
                .collect(Collectors.groupingBy(h -> h.getMarket().toUpperCase()));

        Map<String, BigDecimal> allPrices = new HashMap<>();
        for (Map.Entry<String, List<HoldingDto>> entry : byExchange.entrySet()) {
            String excd = entry.getKey();
            Set<String> symbols = entry.getValue().stream()
                    .map(h -> h.getStockCode().toUpperCase())
                    .collect(Collectors.toSet());
            allPrices.putAll(overseasPriceFetcher.fetchPrices(excd, symbols));
        }

        if (!allPrices.isEmpty()) {
            for (HoldingDto h : usHoldings) {
                BigDecimal price = allPrices.get(h.getStockCode().toUpperCase());
                if (price != null) evaluateAndNotify(h, price);
            }
        }
    }

    private void processHoldings(List<HoldingDto> holdings, Map<String, BigDecimal> prices) {
        for (HoldingDto h : holdings) {
            BigDecimal price = prices.get(h.getStockCode());
            if (price != null) evaluateAndNotify(h, price);
        }
    }

    private void evaluateAndNotify(HoldingDto h, BigDecimal currentPrice) {
        if (h.getBuyPrice() == null || h.getBuyPrice().compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal target10 = h.getBuyPrice().multiply(PCT_10);
        BigDecimal target5  = h.getBuyPrice().multiply(PCT_5);

        boolean already10 = Boolean.TRUE.equals(h.getNotified10pct());
        boolean already5  = Boolean.TRUE.equals(h.getNotified5pct());

        if (!already10 && currentPrice.compareTo(target10) >= 0) {
            sendHoldingAlert(h, currentPrice, 10);
            markNotified(h.getId(), true, true);
        } else if (!already5 && currentPrice.compareTo(target5) >= 0) {
            sendHoldingAlert(h, currentPrice, 5);
            markNotified(h.getId(), true, false);
        }
    }

    private void sendHoldingAlert(HoldingDto h, BigDecimal currentPrice, int pct) {
        String symbolDisplay = h.getSymbol() != null && !h.getSymbol().isBlank()
                ? h.getSymbol() : h.getStockCode();
        String flag = h.isUs() ? "🇺🇸" : "🇰🇷";
        String currency = h.isUs() ? "$" : "";
        String unit = h.isUs() ? "" : "원";

        BigDecimal gainRate = currentPrice.subtract(h.getBuyPrice())
                .divide(h.getBuyPrice(), 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);

        StringBuilder sb = new StringBuilder();
        sb.append(flag).append(" <b>").append(symbolDisplay).append("</b> 📈 +").append(pct).append("%\n");
        sb.append("매수 : ").append(currency).append(formatPrice(h.getBuyPrice(), h.isUs())).append(unit).append("\n");
        sb.append("현재 : <b>").append(currency).append(formatPrice(currentPrice, h.isUs())).append(unit).append("</b>\n");
        sb.append("수익 : <b>+").append(gainRate).append("%</b>");

        telegramService.broadcast(sb.toString());
        log.info("HOLDING ALERT +{}%: {} buy={} current={} gain={}%",
                pct, symbolDisplay, h.getBuyPrice(), currentPrice, gainRate);
    }

    private String formatPrice(BigDecimal price, boolean isUs) {
        if (price == null) return "-";
        if (isUs) return String.format("%,.2f", price);
        if (price.scale() <= 0 || price.stripTrailingZeros().scale() <= 0) {
            return String.format("%,.0f", price);
        }
        return String.format("%,.2f", price);
    }

    // ─── 장 시간 판단 (AlertService 와 동일) ───

    static boolean isKrMarketTime(ZonedDateTime nowKst) {
        DayOfWeek dow = nowKst.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return false;
        LocalTime t = nowKst.toLocalTime();
        return !t.isBefore(LocalTime.of(8, 50)) && !t.isAfter(LocalTime.of(15, 35));
    }

    static boolean isUsMarketTime(ZonedDateTime nowKst) {
        DayOfWeek dow = nowKst.getDayOfWeek();
        LocalTime t = nowKst.toLocalTime();
        if (dow == DayOfWeek.SUNDAY) return false;
        if (dow == DayOfWeek.SATURDAY) return t.isBefore(LocalTime.of(7, 0));
        return !t.isBefore(LocalTime.of(22, 0)) || t.isBefore(LocalTime.of(7, 0));
    }

    // ─── Supabase CRUD ───

    public List<HoldingDto> getActiveHoldings() {
        try {
            List<HoldingDto> list = supabaseRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/holdings")
                            .queryParam("select", "*")
                            .queryParam("is_sold", "eq.false")
                            .queryParam("order", "id.asc")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<HoldingDto>>() {});
            return list != null ? list : List.of();
        } catch (Exception e) {
            log.error("Failed to fetch active holdings: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<HoldingDto> getAllHoldings() {
        try {
            List<HoldingDto> list = supabaseRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/holdings")
                            .queryParam("select", "*")
                            .queryParam("order", "id.asc")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<HoldingDto>>() {});
            return list != null ? list : List.of();
        } catch (Exception e) {
            log.error("Failed to fetch holdings: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private void markNotified(Long id, boolean notified5, boolean notified10) {
        try {
            StringBuilder body = new StringBuilder("{");
            body.append("\"notified_5pct\": ").append(notified5);
            if (notified10) {
                body.append(", \"notified_10pct\": true");
            }
            body.append("}");

            supabaseRestClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/v1/holdings")
                            .queryParam("id", "eq." + id)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body.toString())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to mark holding {} notified: {}", id, e.getMessage());
        }
    }
}
