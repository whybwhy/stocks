package com.example.stocks.theme;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 네이버 금융 테마 시세 페이지 크롤러.
 * https://finance.naver.com/sise/theme.naver?field=change_rate&ordering=desc&page=N
 *
 * 1페이지(~35개 테마)를 등락률 내림차순으로 조회하여 상위 테마 반환.
 */
@Component
public class NaverThemeFetcher {

    private static final Logger log = LoggerFactory.getLogger(NaverThemeFetcher.class);
    private static final String BASE_URL =
            "https://finance.naver.com/sise/theme.naver?field=change_rate&ordering=desc&page=1";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    /**
     * 상위 N개 테마를 등락률 내림차순으로 반환.
     *
     * @param topN 상위 몇 개 반환할지 (예: 10)
     */
    public List<ThemeDto> fetchTopThemes(int topN) {
        List<ThemeDto> result = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(BASE_URL)
                    .userAgent(USER_AGENT)
                    .timeout(10_000)
                    .get();

            // 테마 테이블: <table class="type_1">
            Element table = doc.selectFirst("table.type_1");
            if (table == null) {
                log.warn("[테마크롤링] table.type_1 을 찾을 수 없음");
                return result;
            }

            Elements rows = table.select("tr");
            for (Element row : rows) {
                Elements cols = row.select("td");
                // 유효 행: td가 7개 이상 (테마명, 등락률, 3일등락률, 상승, 보합, 하락, 주도주1, 주도주2)
                if (cols.size() < 7) continue;

                String name        = cols.get(0).text().trim();
                String changeRate  = cols.get(1).text().trim();
                String rate3days   = cols.get(2).text().trim();
                String upStr       = cols.get(3).text().trim();
                String steadyStr   = cols.get(4).text().trim();
                String downStr     = cols.get(5).text().trim();
                String leader1     = cols.get(6).text().trim();
                String leader2     = cols.size() > 7 ? cols.get(7).text().trim() : "";

                if (name.isBlank() || changeRate.isBlank()) continue;

                int up     = parseIntSafe(upStr);
                int steady = parseIntSafe(steadyStr);
                int down   = parseIntSafe(downStr);

                result.add(new ThemeDto(name, changeRate, rate3days, up, steady, down, leader1, leader2));

                if (result.size() >= topN) break;
            }

            log.info("[테마크롤링] 조회 완료: {}건", result.size());
        } catch (Exception e) {
            log.error("[테마크롤링] 실패: {}", e.getMessage(), e);
        }
        return result;
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
