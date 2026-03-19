package com.example.stocks.kis;

import java.math.BigDecimal;

/**
 * 시장 업종지수 단건 데이터.
 */
public class MarketIndexDto {

    private final String name;         // 표시명 (코스피, 코스닥, 야간선물)
    private final BigDecimal current;  // 현재 지수
    private final BigDecimal change;   // 전일 대비
    private final BigDecimal changeRate; // 등락률 (%)
    private final String sign;         // "2"=상승, "3"=보합, "5"=하락

    public MarketIndexDto(String name, BigDecimal current, BigDecimal change,
                          BigDecimal changeRate, String sign) {
        this.name = name;
        this.current = current;
        this.change = change;
        this.changeRate = changeRate;
        this.sign = sign;
    }

    public String getName()         { return name; }
    public BigDecimal getCurrent()  { return current; }
    public BigDecimal getChange()   { return change; }
    public BigDecimal getChangeRate() { return changeRate; }
    public String getSign()         { return sign; }

    public boolean isUp()           { return "1".equals(sign) || "2".equals(sign); }
    public boolean isDown()         { return "4".equals(sign) || "5".equals(sign); }
}
