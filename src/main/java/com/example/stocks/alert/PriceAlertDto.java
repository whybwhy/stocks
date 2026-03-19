package com.example.stocks.alert;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Supabase price_alerts 테이블 행 (한국투자증권 API 형식).
 */
public class PriceAlertDto {

    private Long id;
    private String stockCode;
    private String symbol;
    private java.math.BigDecimal targetPrice;
    private String condition; // ABOVE, BELOW
    private String label;
    private Boolean isActive;
    private String triggeredAt;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonProperty("stock_code")
    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    @JsonProperty("target_price")
    public java.math.BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(java.math.BigDecimal targetPrice) { this.targetPrice = targetPrice; }

    @JsonProperty("condition")
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @JsonProperty("is_active")
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @JsonProperty("triggered_at")
    public String getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(String triggeredAt) { this.triggeredAt = triggeredAt; }

    @JsonProperty("created_at")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
