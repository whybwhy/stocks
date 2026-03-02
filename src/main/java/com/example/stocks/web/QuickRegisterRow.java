package com.example.stocks.web;

/**
 * 간편등록 한 행: 종목, 타입, 섹터, 설명.
 */
public class QuickRegisterRow {

    private String stockName;
    private String type;
    private String sector;
    private String description;

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
