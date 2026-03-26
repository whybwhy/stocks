package com.example.stocks.kis;

import java.math.BigDecimal;

/**
 * 국내주식 현재가 시세 (KIS inquire-price 응답).
 *
 * @param currentPrice  현재가 (stck_prpr)
 * @param openPrice     시가 (stck_oprc), 없으면 null
 * @param dayHigh       당일 고가 (stck_hgpr), 없으면 null
 * @param prevDayHigh   전일 고가 (prdy_hgpr 등), 없으면 null
 */
public record DomesticStockQuote(
        BigDecimal currentPrice,
        BigDecimal openPrice,
        BigDecimal dayHigh,
        BigDecimal prevDayHigh) {}
