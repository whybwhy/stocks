package com.example.stocks.alert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Supabase {@code price_alert_triggers} 행. */
public class PriceAlertTriggerDto {

    private Long id;
    private Long alertId;
    private String market;
    private String stockCode;
    private String symbol;
    private BigDecimal targetPrice;
    private String condition;
    private BigDecimal triggerPrice;
    private String label;
    private String source;
    private String triggeredAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("alert_id")
    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    @JsonProperty("stock_code")
    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @JsonProperty("target_price")
    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    @JsonProperty("condition")
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @JsonProperty("trigger_price")
    public BigDecimal getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(BigDecimal triggerPrice) {
        this.triggerPrice = triggerPrice;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("triggered_at")
    public String getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(String triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    /**
     * 화면용: {@code triggered_at} 을 한국 표준시(Asia/Seoul, UTC+9)로 맞춘 뒤 {@code yyyy-MM-dd HH:mm} 형식.
     * Supabase JSON(ISO-8601, Z/오프셋/naive)을 최대한 수용합니다.
     */
    @JsonIgnore
    public String getTriggeredAtKst() {
        if (triggeredAt == null || triggeredAt.isBlank()) {
            return "-";
        }
        String raw = triggeredAt.trim();
        try {
            ZonedDateTime kst = toSeoul(raw);
            return kst.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeException e) {
            return raw;
        }
    }

    private static ZonedDateTime toSeoul(String raw) {
        String s = raw.length() > 10 && raw.charAt(10) == ' ' ? raw.substring(0, 10) + 'T' + raw.substring(11) : raw;
        ZoneId seoul = ZoneId.of("Asia/Seoul");
        try {
            return OffsetDateTime.parse(s).atZoneSameInstant(seoul);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Instant.parse(s).atZone(seoul);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return ZonedDateTime.parse(s).withZoneSameInstant(seoul);
        } catch (DateTimeParseException ignored) {
        }
        LocalDateTime ldt = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return ldt.atZone(seoul);
    }

    public boolean isUs() {
        return "NAS".equalsIgnoreCase(market) || "NYS".equalsIgnoreCase(market) || "AMS".equalsIgnoreCase(market);
    }
}
