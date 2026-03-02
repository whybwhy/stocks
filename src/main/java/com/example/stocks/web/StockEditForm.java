package com.example.stocks.web;

import java.math.BigDecimal;

/**
 * 주식 등록/수정 폼 바인딩용.
 */
public class StockEditForm {

    private Long id;
    private String stockName;
    private BigDecimal firstBuyPrice;
    private BigDecimal secondBuyPrice;
    private BigDecimal thirdBuyPrice;
    private String description;
    private String account;
    private String type;
    private String ticker;
    private String sector;

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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }
}
