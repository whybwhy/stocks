package com.example.stocks.domain;

/**
 * 주소(URL)에 들어온 서비스 구분값을 실제 사용할 값으로 매핑하는 enum.
 * 예: /stocks/stock → STOCK(실제값 "주식" 또는 DB용 코드 등)
 */
public enum StockServiceType {

    STOCK("chartboy", "chartboy"),
    FRONTIER("frontier", "frontier");

    /** URL 경로에 들어오는 값 (예: /stocks/stock 의 "stock") */
    private final String pathValue;
    /** 실제 사용할 값 (DB, 필터, 노출용 등) */
    private final String actualValue;

    StockServiceType(String pathValue, String actualValue) {
        this.pathValue = pathValue;
        this.actualValue = actualValue;
    }

    public String getPathValue() {
        return pathValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    /**
     * URL 경로 값으로 enum 조회. 대소문자 무시.
     * @return 매칭되는 enum, 없으면 null
     */
    public static StockServiceType fromPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalized = path.strip().toLowerCase();
        for (StockServiceType type : values()) {
            if (type.pathValue.equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
