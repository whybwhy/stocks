package com.example.stocks.web;

/**
 * 목표가 알람 등록/수정 폼 (price_alerts).
 */
public class PriceAlertForm {

    private Long id;
    private String market = "KR";
    private String stockCode;
    private String symbol;
    /** 콤마 제거 후 BigDecimal 파싱 */
    private String targetPrice;
    private String condition = "ABOVE";
    private String label;
    private String source = "MY";
    /** "true" / "false" (select 바인딩용) */
    private String isActive = "true";
    /** true면 triggered_at 을 null 로 초기화 */
    private boolean clearTriggered;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getTargetPrice() { return targetPrice; }
    public void setTargetPrice(String targetPrice) { this.targetPrice = targetPrice; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getIsActive() { return isActive; }
    public void setIsActive(String isActive) { this.isActive = isActive; }

    public boolean isClearTriggered() { return clearTriggered; }
    public void setClearTriggered(boolean clearTriggered) { this.clearTriggered = clearTriggered; }
}
