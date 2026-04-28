package com.example.stocks.web;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 목표가 목록 검색어 검증 (PostgREST 필터 조합·XSS·주입 문자 차단).
 * <p>전체 일치 검색에 사용할 문자열만 통과시킵니다.</p>
 */
public final class PriceAlertSearchSanitizer {

    /** PostgREST or/eq 구문을 깨뜨리거나 SQL·스크립트에 쓰이기 쉬운 문자 제외 */
    private static final int MAX_LEN = 64;
    private static final Pattern ALLOWED = Pattern.compile(
            "^[\\p{IsHangul}\\p{IsLatin}0-9._·\\s-]{1," + MAX_LEN + "}$");

    private PriceAlertSearchSanitizer() {
    }

    /**
     * @param raw 요청 파라미터 {@code q}
     * @return 통과 시 정규화된 검색어( trim ), 빈 검색은 empty, 규칙 위반 시 empty
     */
    public static Optional<String> validated(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return Optional.empty();
        }
        if (t.length() > MAX_LEN || !ALLOWED.matcher(t).matches()) {
            return Optional.empty();
        }
        return Optional.of(t);
    }

    /** 사용자가 비어 있지 않게 입력했으나 {@link #validated(String)} 에 걸린 경우 */
    public static boolean wasRejected(String raw, Optional<String> validated) {
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        return validated.isEmpty();
    }
}
