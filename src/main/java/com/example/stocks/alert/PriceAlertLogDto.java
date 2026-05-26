package com.example.stocks.alert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Supabase {@code price_alerts_log} 행 — 목표가 메모 원장(중복 허용).
 */
public class PriceAlertLogDto {

    private Long id;

    private String postedBy;

    private String market;

    private String stockCode;

    private String symbol;

    private BigDecimal targetPrice;

    private String condition;

    private String label;

    /** 서울 달력일(YYYY-MM-DD). 컬럼 미도입 DB 에서는 null. */
    private String seoulLogDate;

    private String createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("posted_by")
    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("seoul_log_date")
    public String getSeoulLogDate() {
        return seoulLogDate;
    }

    public void setSeoulLogDate(String seoulLogDate) {
        this.seoulLogDate = seoulLogDate;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
