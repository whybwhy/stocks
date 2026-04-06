package com.example.stocks.holding;

import com.example.stocks.alert.TelegramService;
import com.example.stocks.kis.DomesticStockQuote;
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
 * 보유종목 수익·손실 알림.
 * 수익: 매수가 대비 +5% / +10% — {@link #HOLDING_GAIN_ALERTS_ENABLED} 가 false 이면 비활성.
 * 손실: 매수가 대비 -3% / -5% / -10% — {@link #HOLDING_LOSS_ALERTS_ENABLED} 가 false 이면 비활성.
 * 국내: 당일 고가 &gt; 전일 고가 이고 당일 고가 &gt; 현재가면 메시지에 [뚜껑] 추가 (KIS 전일고가 필드 있을 때).
 */
@Service
public class HoldingService {

    /** true: +5% / +10% 수익 텔레그램 알림 활성. */
    private static final boolean HOLDING_GAIN_ALERTS_ENABLED = true;
    /** true: -3% / -5% / -10% 손실 텔레그램 알림 활성. */
    private static final boolean HOLDING_LOSS_ALERTS_ENABLED = true;

    private static final Logger log = LoggerFactory.getLogger(HoldingService.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final BigDecimal PCT_5  = new BigDecimal("1.05");
    private static final BigDecimal PCT_10 = new BigDecimal("1.10");
    private static final BigDecimal LOSS_3  = new BigDecimal("0.97");
    private static final BigDecimal LOSS_5  = new BigDecimal("0.95");
    private static final BigDecimal LOSS_10 = new BigDecimal("0.90");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /** 보유종목 알림 전용 텔레그램 채널(또는 그룹) ID */
    private static final String HOLDING_TELEGRAM_CHAT_ID = "8066272092";

    /** 일별 수익/손실 알림 추적: "holdingId_pct" 형태로 당일 발송 여부 기록. 날짜 변경 시 자동 초기화. */
    private final Set<String> dailyNotified = new HashSet<>();
    private LocalDate dailyNotifiedDate;

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
     * 매수가 대비 +5%/+10% 일별 알림. HoldingGainScheduler(15분 간격)에서 호출.
     * 하루에 각 종목·퍼센트별 한 번만 발송, 다음 날 자동 리셋.
     */
    public void checkDailyGains() {
        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        LocalDate today = nowKst.toLocalDate();

        // 날짜가 바뀌면 추적 셋 초기화
        if (!today.equals(dailyNotifiedDate)) {
            dailyNotified.clear();
            dailyNotifiedDate = today;
        }

        if (!isKrMarketTime(nowKst) && !isUsMarketTime(nowKst)) return;

        List<HoldingDto> holdings = getActiveHoldings();
        if (holdings.isEmpty()) return;

        if (isKrMarketTime(nowKst) && kisPriceFetcher != null && kisPriceFetcher.isConfigured()) {
            List<HoldingDto> krHoldings = holdings.stream()
                    .filter(HoldingDto::isKr).collect(Collectors.toList());
            if (!krHoldings.isEmpty()) {
                Set<String> codes = krHoldings.stream()
                        .map(HoldingDto::getStockCode)
                        .filter(s -> s != null && !s.isBlank())
                        .collect(Collectors.toSet());
                Map<String, DomesticStockQuote> quotes = kisPriceFetcher.fetchQuotes(codes);
                if (!quotes.isEmpty()) {
                    for (HoldingDto h : krHoldings) {
                        DomesticStockQuote q = quotes.get(h.getStockCode());
                        if (q == null || q.currentPrice() == null) continue;
                        evaluateDailyGain(h, q.currentPrice(), q);
                    }
                }
            }
        }

        if (isUsMarketTime(nowKst) && overseasPriceFetcher != null && overseasPriceFetcher.isConfigured()) {
            List<HoldingDto> usHoldings = holdings.stream()
                    .filter(HoldingDto::isUs).collect(Collectors.toList());
            if (usHoldings.isEmpty()) return;

            Map<String, List<HoldingDto>> byExchange = usHoldings.stream()
                    .collect(Collectors.groupingBy(h -> h.getMarket().toUpperCase()));
            Map<String, BigDecimal> allPrices = new HashMap<>();
            for (Map.Entry<String, List<HoldingDto>> entry : byExchange.entrySet()) {
                Set<String> symbols = entry.getValue().stream()
                        .map(h -> h.getStockCode().toUpperCase())
                        .collect(Collectors.toSet());
                allPrices.putAll(overseasPriceFetcher.fetchPrices(entry.getKey(), symbols));
            }
            for (HoldingDto h : usHoldings) {
                BigDecimal price = allPrices.get(h.getStockCode().toUpperCase());
                if (price != null) evaluateDailyGain(h, price, null);
            }
        }
    }

    private void evaluateDailyGain(HoldingDto h, BigDecimal currentPrice, DomesticStockQuote quote) {
        if (h.getBuyPrice() == null || h.getBuyPrice().compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal buy = h.getBuyPrice();

        if (currentPrice.compareTo(buy) >= 0) {
            // 수익 체크
            String key10 = h.getId() + "_g10";
            String key5 = h.getId() + "_g5";

            if (!dailyNotified.contains(key10) && currentPrice.compareTo(buy.multiply(PCT_10)) >= 0) {
                sendHoldingGainAlert(h, currentPrice, 10, quote);
                dailyNotified.add(key10);
                dailyNotified.add(key5);
            } else if (!dailyNotified.contains(key5) && currentPrice.compareTo(buy.multiply(PCT_5)) >= 0) {
                sendHoldingGainAlert(h, currentPrice, 5, quote);
                dailyNotified.add(key5);
            }
        } else {
            // 손실 체크
            String keyL10 = h.getId() + "_l10";
            String keyL5 = h.getId() + "_l5";
            String keyL3 = h.getId() + "_l3";

            if (!dailyNotified.contains(keyL10) && currentPrice.compareTo(buy.multiply(LOSS_10)) <= 0) {
                sendHoldingLossAlert(h, currentPrice, 10, quote);
                dailyNotified.add(keyL10);
                dailyNotified.add(keyL5);
                dailyNotified.add(keyL3);
            } else if (!dailyNotified.contains(keyL5) && currentPrice.compareTo(buy.multiply(LOSS_5)) <= 0) {
                sendHoldingLossAlert(h, currentPrice, 5, quote);
                dailyNotified.add(keyL5);
                dailyNotified.add(keyL3);
            } else if (!dailyNotified.contains(keyL3) && currentPrice.compareTo(buy.multiply(LOSS_3)) <= 0) {
                sendHoldingLossAlert(h, currentPrice, 3, quote);
                dailyNotified.add(keyL3);
            }
        }
    }

    /**
     * 보유종목 수익/손실 알림 체크 (레거시). checkDailyGains()로 대체됨.
     * AlertScheduler에서 호출되나, 일별 알림은 checkDailyGains()에서 처리.
     */
    public void checkHoldings() {
        // checkDailyGains()에서 일별 수익/손실 알림을 처리하므로 여기서는 스킵
        return;
        /*
        if (!HOLDING_GAIN_ALERTS_ENABLED && !HOLDING_LOSS_ALERTS_ENABLED) {
            return;
        }

        ZonedDateTime nowKst = ZonedDateTime.now(KST);
        if (!isKrMarketTime(nowKst) && !isUsMarketTime(nowKst)) {
            return;
        }

        List<HoldingDto> holdings = getActiveHoldings();
        if (holdings.isEmpty()) return;

        if (isKrMarketTime(nowKst) && kisPriceFetcher != null && kisPriceFetcher.isConfigured()) {
            List<HoldingDto> krHoldings = holdings.stream()
                    .filter(HoldingDto::isKr).collect(Collectors.toList());
            if (!krHoldings.isEmpty()) {
                Set<String> codes = krHoldings.stream()
                        .map(HoldingDto::getStockCode)
                        .filter(s -> s != null && !s.isBlank())
                        .collect(Collectors.toSet());
                Map<String, DomesticStockQuote> quotes = kisPriceFetcher.fetchQuotes(codes);
                if (!quotes.isEmpty()) processHoldings(krHoldings, quotes);
            }
        }

        if (isUsMarketTime(nowKst) && overseasPriceFetcher != null && overseasPriceFetcher.isConfigured()) {
            checkUsHoldings(holdings);
        }
        */
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
                if (price != null) evaluateAndNotify(h, price, null);
            }
        }
    }

    private void processHoldings(List<HoldingDto> holdings, Map<String, DomesticStockQuote> quotes) {
        for (HoldingDto h : holdings) {
            DomesticStockQuote q = quotes.get(h.getStockCode());
            if (q == null || q.currentPrice() == null) continue;
            evaluateAndNotify(h, q.currentPrice(), q);
        }
    }

    private void evaluateAndNotify(HoldingDto h, BigDecimal currentPrice, DomesticStockQuote quote) {
        if (h.getBuyPrice() == null || h.getBuyPrice().compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal buy = h.getBuyPrice();
        if (currentPrice.compareTo(buy) >= 0) {
            evaluateGain(h, currentPrice, buy, quote);
        } else {
            evaluateLoss(h, currentPrice, buy, quote);
        }
    }

    private void evaluateGain(HoldingDto h, BigDecimal currentPrice, BigDecimal buy, DomesticStockQuote quote) {
        if (!HOLDING_GAIN_ALERTS_ENABLED) {
            return;
        }
        BigDecimal target10 = buy.multiply(PCT_10);
        BigDecimal target5 = buy.multiply(PCT_5);

        boolean already10 = Boolean.TRUE.equals(h.getNotified10pct());
        boolean already5 = Boolean.TRUE.equals(h.getNotified5pct());

        if (!already10 && currentPrice.compareTo(target10) >= 0) {
            sendHoldingGainAlert(h, currentPrice, 10, quote);
            markGainNotified(h.getId(), true, true);
        } else if (!already5 && currentPrice.compareTo(target5) >= 0) {
            sendHoldingGainAlert(h, currentPrice, 5, quote);
            markGainNotified(h.getId(), true, false);
        }
    }

    private void evaluateLoss(HoldingDto h, BigDecimal currentPrice, BigDecimal buy, DomesticStockQuote quote) {
        if (!HOLDING_LOSS_ALERTS_ENABLED) {
            return;
        }
        BigDecimal t10 = buy.multiply(LOSS_10);
        BigDecimal t5 = buy.multiply(LOSS_5);
        BigDecimal t3 = buy.multiply(LOSS_3);

        boolean already10 = Boolean.TRUE.equals(h.getNotifiedLoss10pct());
        boolean already5 = Boolean.TRUE.equals(h.getNotifiedLoss5pct());
        boolean already3 = Boolean.TRUE.equals(h.getNotifiedLoss3pct());

        if (!already10 && currentPrice.compareTo(t10) <= 0) {
            sendHoldingLossAlert(h, currentPrice, 10, quote);
            markLossNotified(h.getId(), 10);
        } else if (!already5 && currentPrice.compareTo(t5) <= 0) {
            sendHoldingLossAlert(h, currentPrice, 5, quote);
            markLossNotified(h.getId(), 5);
        } else if (!already3 && currentPrice.compareTo(t3) <= 0) {
            sendHoldingLossAlert(h, currentPrice, 3, quote);
            markLossNotified(h.getId(), 3);
        }
    }

    private void sendHoldingGainAlert(HoldingDto h, BigDecimal currentPrice, int pct, DomesticStockQuote quote) {
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
        appendDdokkeutMemo(sb, quote, currentPrice);

        telegramService.sendToChat(HOLDING_TELEGRAM_CHAT_ID, sb.toString());
        log.info("HOLDING ALERT +{}%: {} buy={} current={} gain={}%",
                pct, symbolDisplay, h.getBuyPrice(), currentPrice, gainRate);
    }

    private void sendHoldingLossAlert(HoldingDto h, BigDecimal currentPrice, int pct, DomesticStockQuote quote) {
        String symbolDisplay = h.getSymbol() != null && !h.getSymbol().isBlank()
                ? h.getSymbol() : h.getStockCode();
        String flag = h.isUs() ? "🇺🇸" : "🇰🇷";
        String currency = h.isUs() ? "$" : "";
        String unit = h.isUs() ? "" : "원";

        BigDecimal gainRate = currentPrice.subtract(h.getBuyPrice())
                .divide(h.getBuyPrice(), 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);

        StringBuilder sb = new StringBuilder();
        sb.append(flag).append(" <b>").append(symbolDisplay).append("</b> 📉 -").append(pct).append("%\n");
        sb.append("매수 : ").append(currency).append(formatPrice(h.getBuyPrice(), h.isUs())).append(unit).append("\n");
        sb.append("현재 : <b>").append(currency).append(formatPrice(currentPrice, h.isUs())).append(unit).append("</b>\n");
        sb.append("수익 : <b>").append(gainRate).append("%</b>");
        appendDdokkeutMemo(sb, quote, currentPrice);

        telegramService.sendToChat(HOLDING_TELEGRAM_CHAT_ID, sb.toString());
        log.info("HOLDING ALERT -{}%: {} buy={} current={} gain={}%",
                pct, symbolDisplay, h.getBuyPrice(), currentPrice, gainRate);
    }

    /**
     * 당일 고가 &gt; 전일 고가 이고, 당일 고가 &gt; 현재가 일 때 [뚜껑] (국내·시세 필드 있을 때만).
     */
    private static void appendDdokkeutMemo(StringBuilder sb, DomesticStockQuote quote, BigDecimal currentPrice) {
        if (quote == null || currentPrice == null) return;
        if (quote.dayHigh() == null || quote.prevDayHigh() == null) return;
        if (quote.dayHigh().compareTo(quote.prevDayHigh()) <= 0) return;
        if (quote.dayHigh().compareTo(currentPrice) <= 0) return;
        sb.append("\n[뚜껑]");
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

    private void markGainNotified(Long id, boolean notified5, boolean notified10) {
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
            log.error("Failed to mark holding {} gain notified: {}", id, e.getMessage());
        }
    }

    /** level 3 → loss_3만, 5 → 3+5, 10 → 3+5+10 (한 번에 깊게 빠진 경우 상위 구간만 알림). */
    private void markLossNotified(Long id, int level) {
        try {
            StringBuilder body = new StringBuilder("{\"notified_loss_3pct\": true");
            if (level >= 5) {
                body.append(", \"notified_loss_5pct\": true");
            }
            if (level >= 10) {
                body.append(", \"notified_loss_10pct\": true");
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
            log.error("Failed to mark holding {} loss notified: {}", id, e.getMessage());
        }
    }
}
