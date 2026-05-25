package com.example.stocks.web;

import com.example.stocks.alert.PriceAlertLogDto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * {@code price_alerts_log} 행 목록을 화면용 메모 블록(마크다운 코드 블록에 넣을 원문 문자열)으로 변환.
 */
final class PriceAlertLogMarkdownComposer {

    private PriceAlertLogMarkdownComposer() {
    }

    static String composeChartboyMarkdown(List<PriceAlertLogDto> sortedByIdAscending) {
        if (sortedByIdAscending == null || sortedByIdAscending.isEmpty()) {
            return "(해당 작성자 최신 적재일에 행이 없습니다.)";
        }
        LinkedHashMap<String, List<PriceAlertLogDto>> byCode = bucketByStockCodeInsertionOrder(sortedByIdAscending);

        StringBuilder sb = new StringBuilder();
        sb.append("✅ 차트보이 영상 요약").append('\n');

        for (List<PriceAlertLogDto> group : byCode.values()) {
            group.sort(Comparator.comparing(PriceAlertLogDto::getId));
            List<PriceAlertLogDto> above = group.stream()
                    .filter(r -> !isBelowCondition(r))
                    .toList();
            List<PriceAlertLogDto> below = group.stream()
                    .filter(PriceAlertLogMarkdownComposer::isBelowCondition)
                    .toList();

            String sym = displaySymbol(group);

            if (!above.isEmpty()) {
                above = new ArrayList<>(above);
                above.sort(Comparator.comparing(r -> nz(r.getTargetPrice())));
                String merged = above.stream()
                        .map(r -> formatWonDotThousands(nz(r.getTargetPrice())) + "원")
                        .collect(Collectors.joining("/"));
                sb.append("✔️").append(sym).append(' ').append(merged).append(" 돌파시").append('\n');
            }
            for (PriceAlertLogDto r : below) {
                sb.append("✔️").append(sym).append(' ')
                        .append(formatWonDotThousands(nz(r.getTargetPrice()))).append("원")
                        .append(" 손절·이탈 시")
                        .append('\n');
            }
        }

        trimTrailingNewline(sb);
        return sb.toString();
    }

    static String composeHyonyMarkdown(LocalDate batchSeoulDay, List<PriceAlertLogDto> sortedByIdAscending) {
        StringBuilder sb = new StringBuilder();
        if (batchSeoulDay != null) {
            DayOfWeek dow = batchSeoulDay.getDayOfWeek();
            String shortDow = dow.getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            sb.append("🌈")
                    .append(batchSeoulDay.getMonthValue())
                    .append("월 ")
                    .append(batchSeoulDay.getDayOfMonth())
                    .append("일(").append(shortDow).append(") 멤버쉽영상정리☀️")
                    .append('\n');
        } else {
            sb.append("🌈 멤버쉽 영상 정리 ☀️").append('\n');
        }

        if (sortedByIdAscending == null || sortedByIdAscending.isEmpty()) {
            sb.append("(해당 작성자 최신 적재일에 행이 없습니다.)");
            return sb.toString();
        }

        LinkedHashMap<String, List<PriceAlertLogDto>> byCode = bucketByStockCodeInsertionOrder(sortedByIdAscending);
        for (List<PriceAlertLogDto> group : byCode.values()) {
            group.sort(Comparator.comparing(PriceAlertLogDto::getId));
            String sym = displaySymbol(group);
            String inner = group.stream()
                    .map(r -> memoFragment(r.getLabel(), r))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining("/"));
            sb.append(sym).append(" (").append(inner).append(')').append('\n');
        }

        trimTrailingNewline(sb);
        return sb.toString();
    }

    /** 라벨 {@code … / …} 접미부가 있으면 사용, 없으면 목표가·조건 문자열 생성. */
    private static String memoFragment(String label, PriceAlertLogDto fallback) {
        String fromLabel = slashSuffix(label);
        if (!fromLabel.isBlank()) {
            return fromLabel;
        }
        String cond = fallback.getCondition() != null ? fallback.getCondition().trim().toUpperCase(Locale.ROOT) : "ABOVE";
        String memo = formatWonCommaThousands(nz(fallback.getTargetPrice())) + "원";
        if ("BELOW".equals(cond)) {
            memo += " 손절라인";
        }
        return memo;
    }

    private static LinkedHashMap<String, List<PriceAlertLogDto>> bucketByStockCodeInsertionOrder(List<PriceAlertLogDto> rows) {
        LinkedHashMap<String, List<PriceAlertLogDto>> map = new LinkedHashMap<>();
        for (PriceAlertLogDto r : rows) {
            String code = r.getStockCode() != null ? r.getStockCode().trim() : "";
            map.computeIfAbsent(code.isEmpty() ? "_" : code, k -> new ArrayList<>()).add(r);
        }
        return map;
    }

    private static String displaySymbol(List<PriceAlertLogDto> group) {
        for (PriceAlertLogDto r : group) {
            if (r.getSymbol() != null && !r.getSymbol().trim().isEmpty()) {
                return r.getSymbol().trim();
            }
        }
        return "?";
    }

    private static String slashSuffix(String label) {
        if (label == null) {
            return "";
        }
        int i = label.indexOf(" / ");
        if (i < 0) {
            return "";
        }
        return label.substring(i + 3).trim();
    }

    private static boolean isBelowCondition(PriceAlertLogDto r) {
        String c = r.getCondition();
        return c != null && "BELOW".equalsIgnoreCase(c.trim());
    }

    /** 천 단위 마침표(예: {@code 18.750}), 소수는 정수 부만 그룹핑 후 소수 접미 부착. */
    private static String formatWonDotThousands(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        BigDecimal v = value.stripTrailingZeros();
        if (v.scale() > 0) {
            String plain = v.toPlainString();
            int dotIdx = plain.indexOf('.');
            if (dotIdx >= 0) {
                BigInteger ip = new BigInteger(plain.substring(0, dotIdx));
                String frac = plain.substring(dotIdx);
                boolean neg = ip.signum() < 0;
                String digits = ip.abs().toString();
                return (neg ? "-" : "") + rewriteGroupThousands(digits, '.') + frac;
            }
        }
        boolean neg = v.signum() < 0;
        String digits = v.toBigIntegerExact().abs().toString();
        return (neg ? "-" : "") + rewriteGroupThousands(digits, '.');
    }

    /** 멤버쉽 본문 줄용: 세 자리마다 콤마. */
    private static String formatWonCommaThousands(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        BigDecimal v = value.stripTrailingZeros();
        if (v.scale() > 0) {
            String plain = v.toPlainString();
            int dotIdx = plain.indexOf('.');
            if (dotIdx >= 0) {
                BigInteger ip = new BigInteger(plain.substring(0, dotIdx));
                String frac = plain.substring(dotIdx);
                boolean neg = ip.signum() < 0;
                String digits = ip.abs().toString();
                return (neg ? "-" : "") + rewriteGroupThousands(digits, ',') + frac;
            }
        }
        boolean neg = v.signum() < 0;
        String digits = v.toBigIntegerExact().abs().toString();
        return (neg ? "-" : "") + rewriteGroupThousands(digits, ',');
    }

    /**
     * 양의 정수 자릿수 문자열에 천 단위 구분자 삽입(첫 덩어리는 1~3자).
     */
    private static String rewriteGroupThousands(String digitsAbs, char sep) {
        int len = digitsAbs.length();
        StringBuilder sb = new StringBuilder(len + len / 3 + 2);
        int head = len % 3;
        if (head == 0) {
            head = 3;
        }
        for (int i = 0; i < len; i++) {
            if (i != 0 && (i == head || (i - head) % 3 == 0)) {
                sb.append(sep);
            }
            sb.append(digitsAbs.charAt(i));
        }
        return sb.toString();
    }

    private static void trimTrailingNewline(StringBuilder sb) {
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
    }

    private static BigDecimal nz(BigDecimal bd) {
        return bd != null ? bd : BigDecimal.ZERO;
    }
}
