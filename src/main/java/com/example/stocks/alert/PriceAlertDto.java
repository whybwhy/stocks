package com.example.stocks.alert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Supabase price_alerts 테이블 행.
 * market: KR(국내), NAS(나스닥), NYS(뉴욕), AMS(아멕스)
 */
public class PriceAlertDto {

    private Long id;
    private String market;
    private String stockCode;
    private String symbol;
    private BigDecimal targetPrice;
    private String condition;
    private String label;
    private String source;
    private Boolean isActive;
    private String triggeredAt;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    @JsonProperty("stock_code")
    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    @JsonProperty("target_price")
    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }

    @JsonProperty("condition")
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    @JsonProperty("is_active")
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @JsonProperty("triggered_at")
    public String getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(String triggeredAt) { this.triggeredAt = triggeredAt; }

    @JsonProperty("created_at")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isKr() { return market == null || "KR".equalsIgnoreCase(market); }
    public boolean isUs() { return "NAS".equalsIgnoreCase(market) || "NYS".equalsIgnoreCase(market) || "AMS".equalsIgnoreCase(market); }
}
