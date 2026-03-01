package com.example.stocks.supabase;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.util.Locale;

public class StockDto {

    private Long id;

    @JsonProperty("stock_name")
    private String stockName;

    @JsonProperty("first_buy_price")
    private BigDecimal firstBuyPrice;

    @JsonProperty("second_buy_price")
    private BigDecimal secondBuyPrice;

    @JsonProperty("third_buy_price")
    private BigDecimal thirdBuyPrice;

    private String description;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public BigDecimal getFirstBuyPrice() {
        return firstBuyPrice;
    }

    public void setFirstBuyPrice(BigDecimal firstBuyPrice) {
        this.firstBuyPrice = firstBuyPrice;
    }

    public BigDecimal getSecondBuyPrice() {
        return secondBuyPrice;
    }

    public void setSecondBuyPrice(BigDecimal secondBuyPrice) {
        this.secondBuyPrice = secondBuyPrice;
    }

    public BigDecimal getThirdBuyPrice() {
        return thirdBuyPrice;
    }

    public void setThirdBuyPrice(BigDecimal thirdBuyPrice) {
        this.thirdBuyPrice = thirdBuyPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** 원화 기준: 천 단위 콤마(,) + '원' 표기. 노출용. */
    public String getFirstBuyPriceFormatted() {
        return formatPriceWon(firstBuyPrice);
    }

    public String getSecondBuyPriceFormatted() {
        return formatPriceWon(secondBuyPrice);
    }

    public String getThirdBuyPriceFormatted() {
        return formatPriceWon(thirdBuyPrice);
    }

    private static String formatPriceWon(BigDecimal price) {
        if (price == null) return "-";
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setGroupingUsed(true);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        //return nf.format(price) + "원";
        return nf.format(price);
    }
}

