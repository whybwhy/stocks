package com.example.stocks.alert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

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

    public boolean isUs() {
        return "NAS".equalsIgnoreCase(market) || "NYS".equalsIgnoreCase(market) || "AMS".equalsIgnoreCase(market);
    }
}
