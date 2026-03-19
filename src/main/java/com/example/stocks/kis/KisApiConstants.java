package com.example.stocks.kis;

/**
 * 한국투자증권 Open API 상수.
 */
public final class KisApiConstants {

    private KisApiConstants() {}

    // ─── 주식 현재가 ───
    /** 주식현재가 REST (국내) */
    public static final String TR_ID_PRICE = "FHKST01010100";
    /** 시장구분: J = 주식전체(코스피+코스닥) */
    public static final String FID_COND_MRKT_DIV_CODE = "J";
    /** 종목코드 6자리 패턴 */
    public static final String STOCK_CODE_PATTERN = "\\d{6}";

    // ─── 업종(시장) 지수 ───
    /** 업종지수 조회 TR_ID */
    public static final String TR_ID_INDEX = "FHPUP02100000";
    /** 업종 시장구분 코드 */
    public static final String FID_COND_MRKT_DIV_CODE_INDEX = "U";
    /** 업종코드: 코스피 */
    public static final String INDEX_CODE_KOSPI   = "0001";
    /** 업종코드: 코스닥 */
    public static final String INDEX_CODE_KOSDAQ  = "1001";
    /**
     * 업종코드: 코스피200 야간선물 지수.
     * KIS 업종지수 API에서 야간선물 지수를 지원하지 않을 경우 빈 결과를 반환하므로 로그를 확인할 것.
     */
    public static final String INDEX_CODE_NIGHT_FUTURES = "3003";

    // ─── WebSocket ───
    /** WebSocket 실시간 체결가 TR ID */
    public static final String WS_TR_ID_EXEC = "H0STCNT0";
    /** WebSocket 실전 URL */
    public static final String WS_REAL_URL = "ws://ops.koreainvestment.com:21000";
    /** WebSocket 모의 URL */
    public static final String WS_MOCK_URL = "ws://ops.koreainvestment.com:31000";
}
