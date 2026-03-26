package com.example.stocks.holding;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Supabase holdings 테이블 행.
 * market: KR(국내), NAS(나스닥), NYS(뉴욕), AMS(아멕스)
 */
public class HoldingDto {

    private Long id;
    private String market;
    private String stockCode;
    private String symbol;
    private BigDecimal buyPrice;
    private String buyDate;
    private BigDecimal sellPrice;
    private String sellDate;
    private Boolean isSold;
    private BigDecimal profitRate;
    private Boolean notified5pct;
    private Boolean notified10pct;
    private Boolean notifiedLoss3pct;
    private Boolean notifiedLoss5pct;
    private Boolean notifiedLoss10pct;
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

    @JsonProperty("buy_price")
    public BigDecimal getBuyPrice() { return buyPrice; }
    public void setBuyPrice(BigDecimal buyPrice) { this.buyPrice = buyPrice; }

    @JsonProperty("buy_date")
    public String getBuyDate() { return buyDate; }
    public void setBuyDate(String buyDate) { this.buyDate = buyDate; }

    @JsonProperty("sell_price")
    public BigDecimal getSellPrice() { return sellPrice; }
    public void setSellPrice(BigDecimal sellPrice) { this.sellPrice = sellPrice; }

    @JsonProperty("sell_date")
    public String getSellDate() { return sellDate; }
    public void setSellDate(String sellDate) { this.sellDate = sellDate; }

    @JsonProperty("is_sold")
    public Boolean getIsSold() { return isSold; }
    public void setIsSold(Boolean isSold) { this.isSold = isSold; }

    @JsonProperty("profit_rate")
    public BigDecimal getProfitRate() { return profitRate; }
    public void setProfitRate(BigDecimal profitRate) { this.profitRate = profitRate; }

    @JsonProperty("notified_5pct")
    public Boolean getNotified5pct() { return notified5pct; }
    public void setNotified5pct(Boolean notified5pct) { this.notified5pct = notified5pct; }

    @JsonProperty("notified_10pct")
    public Boolean getNotified10pct() { return notified10pct; }
    public void setNotified10pct(Boolean notified10pct) { this.notified10pct = notified10pct; }

    @JsonProperty("notified_loss_3pct")
    public Boolean getNotifiedLoss3pct() { return notifiedLoss3pct; }
    public void setNotifiedLoss3pct(Boolean notifiedLoss3pct) { this.notifiedLoss3pct = notifiedLoss3pct; }

    @JsonProperty("notified_loss_5pct")
    public Boolean getNotifiedLoss5pct() { return notifiedLoss5pct; }
    public void setNotifiedLoss5pct(Boolean notifiedLoss5pct) { this.notifiedLoss5pct = notifiedLoss5pct; }

    @JsonProperty("notified_loss_10pct")
    public Boolean getNotifiedLoss10pct() { return notifiedLoss10pct; }
    public void setNotifiedLoss10pct(Boolean notifiedLoss10pct) { this.notifiedLoss10pct = notifiedLoss10pct; }

    @JsonProperty("created_at")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isKr() { return market == null || "KR".equalsIgnoreCase(market); }
    public boolean isUs() {
        return "NAS".equalsIgnoreCase(market) || "NYS".equalsIgnoreCase(market) || "AMS".equalsIgnoreCase(market);
    }
}
