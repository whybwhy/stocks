package com.example.stocks.kis;

/**
 * 한국투자증권 Open API 상수.
 */
public final class KisApiConstants {

    private KisApiConstants() {}

    /** 주식현재가 REST (국내) */
    public static final String TR_ID_PRICE = "FHKST01010100";
    /** 시장구분: J = 주식전체(코스피+코스닥) */
    public static final String FID_COND_MRKT_DIV_CODE = "J";
    /** 종목코드 6자리 패턴 */
    public static final String STOCK_CODE_PATTERN = "\\d{6}";

    /** WebSocket 실시간 체결가 TR ID */
    public static final String WS_TR_ID_EXEC = "H0STCNT0";
    /** WebSocket 실전 URL */
    public static final String WS_REAL_URL = "ws://ops.koreainvestment.com:21000";
    /** WebSocket 모의 URL */
    public static final String WS_MOCK_URL = "ws://ops.koreainvestment.com:31000";
}
