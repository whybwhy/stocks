package com.example.stocks.web;

import com.example.stocks.alert.AlertService;
import com.example.stocks.alert.PriceAlertDto;
import com.example.stocks.config.AppProperties;
import com.example.stocks.supabase.PriceAlertPageResult;
import com.example.stocks.supabase.PriceAlertTriggerPageResult;
import com.example.stocks.supabase.SupabaseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Supabase {@code price_alerts} 테이블 조회·등록·수정·삭제 및
 * {@code price_alert_triggers} 돌파 로그 공개 화면 ({@code /{app.price-alert-public-slug}/log},
 * {@code triggered} 미지정 시 서울 달력 “오늘” {@code triggered_at} 로 필터·{@code triggered=all} 시 일자 무시),
 * 목록: {@code /private/{slug}/list} (전체), {@code /{slug}} (단축 — 검색 없으면 조회 안 함, 제출 후 CHARTBOY 필터).
 * 메모 원장 최신일: {@code /{slug}/new} — {@code price_alerts_log} 작성자별 최신 서울 적재일 행만.
 * 단축 경로 {@code /{slug}} 는 뷰 {@code price-alerts/slug} , 그 외 목록 공개·로그인·관리는 {@code price-alerts/list}.
 * 스키마: {@code src/main/resources/price_alerts.sql}, {@code price_alert_triggers_ddl.sql}, {@code price_alerts_log.sql}
 */
@Controller
public class PriceAlertViewController {

    private static final int DEFAULT_PAGE_SIZE = 50;
    /** 공개 페이지 응답 캐시 (초). 브라우저·중간 캐시 모두에서 30초 동안 재사용 → Supabase egress 절감. */
    private static final String PUBLIC_CACHE_CONTROL = "public, max-age=30";

    /** 일반 목표가 목록 템플릿. */
    private static final String VIEW_PRICE_ALERTS_LIST = "price-alerts/list";
    /** {@code GET /{app.price-alert-public-slug}} 단축 URL — 템플릿 {@code price-alerts/slug.html}. */
    private static final String VIEW_PRICE_ALERTS_SLUG_ROOT = "price-alerts/slug";

    private final SupabaseService supabaseService;
    private final AppProperties appProperties;
    private final AlertService alertService;

    public PriceAlertViewController(SupabaseService supabaseService, AppProperties appProperties,
                                    AlertService alertService) {
        this.supabaseService = supabaseService;
        this.appProperties = appProperties;
        this.alertService = alertService;
    }

    @GetMapping("/price-alerts")
    public String list(@AuthenticationPrincipal OAuth2User user,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "50") int size,
                       @RequestParam(name = "source", required = false) String source,
                       @RequestParam(name = "q", required = false) String q,
                       Model model) {
        Optional<String> search = PriceAlertSearchSanitizer.validated(q);
        boolean rejected = PriceAlertSearchSanitizer.wasRejected(q, search);
        return renderList(model, page, size, source, resolveKakaoNickname(user), true, "price-alerts", false,
                search, rejected, false, false, Optional.empty(), Optional.empty(), VIEW_PRICE_ALERTS_LIST);
    }

    /**
     * 카카오 로그인 없이 목표가 알람 목록만 조회 (등록·수정·삭제 URL은 제공하지 않음).
     * 공식 경로: {@code /private/{slug}/list}.
     */
    @GetMapping("private/${app.price-alert-public-slug}/list")
    public String listChartboyPublic(@RequestParam(name = "page", defaultValue = "1") int page,
                                     @RequestParam(name = "size", defaultValue = "50") int size,
                                     @RequestParam(name = "q", required = false) String q,
                                     @RequestParam(name = "registered", required = false) String registered,
                                     HttpServletResponse response,
                                     Model model) {
        Optional<String> search = PriceAlertSearchSanitizer.validated(q);
        boolean rejected = PriceAlertSearchSanitizer.wasRejected(q, search);
        Optional<LocalDate> registeredDay = parseRegisteredDateParam(registered);
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        return renderList(model, page, size, "CHARTBOY", null, false, publicListPathPrefix(), true, search, rejected,
                false, false, Optional.empty(), registeredDay, VIEW_PRICE_ALERTS_LIST);
    }

    /**
     * {@code /{slug}} 단축 루트 — 검색 조건(URL {@code q} 또는 {@code registered})이 없으면 목록 조회하지 않음(검색 제출 후 조회).
     * 조건 지정 후에는 CHARTBOY 페이지(id 내림차순)와 동일한 필터 규칙입니다.
     */
    @GetMapping("${app.price-alert-public-slug}")
    public String listChartboyPublicSlugRoot(@RequestParam(name = "page", defaultValue = "1") int page,
                                             @RequestParam(name = "size", defaultValue = "50") int size,
                                             @RequestParam(name = "q", required = false) String q,
                                             @RequestParam(name = "registered", required = false) String registered,
                                             HttpServletResponse response,
                                             Model model) {
        Optional<String> search = PriceAlertSearchSanitizer.validated(q);
        boolean rejected = PriceAlertSearchSanitizer.wasRejected(q, search);
        Optional<LocalDate> registeredDay = parseRegisteredDateParam(registered);
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        String prefix = appProperties.publicPriceAlertListSlugPathWithoutLeadingSlash();
        return renderList(model, page, size, "CHARTBOY", null, false, prefix, true, search, rejected,
                true, false, Optional.empty(), registeredDay, VIEW_PRICE_ALERTS_SLUG_ROOT);
    }

    /**
     * {@code /stocker} 별칭 공개 경로. {@code /private/{slug}/list} 와 동일하게 CHARTBOY 한정 읽기 전용 목록.
     */
    @GetMapping("/stocker")
    public String listStockerPublic(@RequestParam(name = "page", defaultValue = "1") int page,
                                    @RequestParam(name = "size", defaultValue = "50") int size,
                                    @RequestParam(name = "q", required = false) String q,
                                    @RequestParam(name = "registered", required = false) String registered,
                                    HttpServletResponse response,
                                    Model model) {
        Optional<String> search = PriceAlertSearchSanitizer.validated(q);
        boolean rejected = PriceAlertSearchSanitizer.wasRejected(q, search);
        Optional<LocalDate> registeredDay = parseRegisteredDateParam(registered);
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        return renderList(model, page, size, "CHARTBOY", null, false, "stocker", true, search, rejected,
                false, false, Optional.empty(), registeredDay, VIEW_PRICE_ALERTS_LIST);
    }

    /** 오타 URL → 정식 경로 */
    @GetMapping("/chartbody/log")
    public String chartbodyTypoRedirect() {
        return "redirect:" + appProperties.publicPriceAlertLogPath();
    }

    /**
     * 차트보이(CHARTBOY) 목표가 돌파 로그 공개 조회. 종목명·코드 전체 일치 필터·자동완성. 검색 실행은 제출(또는 목록에서 후보 선택 시).
     */
    @GetMapping("${app.price-alert-public-slug}/log")
    public String listChartboyTriggerLog(@RequestParam(name = "page", defaultValue = "1") int page,
                                         @RequestParam(name = "size", defaultValue = "50") int size,
                                         @RequestParam(name = "q", required = false) String q,
                                         @RequestParam(name = "triggered", required = false) String triggered,
                                         @RequestParam(name = "from", required = false) String from,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         Model model) {
        Optional<String> search = PriceAlertSearchSanitizer.validated(q);
        boolean rejected = PriceAlertSearchSanitizer.wasRejected(q, search);
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        return renderTriggerLog(model, page, size, search, rejected, request, from, triggered);
    }

    /**
     * {@code price_alerts_log} 공개 뷰 — 차트보이·효니효니 각각 <strong>최신 서울 적재일</strong>의 행만 마크다운 코드 블록 형태로 표시.
     */
    @GetMapping("${app.price-alert-public-slug}/new")
    public String priceAlertMemoLogNew(HttpServletResponse response, Model model) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        String chartboyKey = "CHARTBOY";
        String hyonyKey = "HYONYHYONY";
        LocalDate todaySeoul = LocalDate.now(ZoneId.of("Asia/Seoul"));

        Optional<LocalDate> chartboyDay = supabaseService.getLatestPriceAlertLogSeoulDay(chartboyKey);
        Optional<LocalDate> hyonyDay = supabaseService.getLatestPriceAlertLogSeoulDay(hyonyKey);

        var chartboyRows = chartboyDay
                .map(d -> supabaseService.getPriceAlertLogsOnSeoulDay(chartboyKey, d))
                .orElse(List.of());
        var hyonyRows = hyonyDay
                .map(d -> supabaseService.getPriceAlertLogsOnSeoulDay(hyonyKey, d))
                .orElse(List.of());

        model.addAttribute("today", todaySeoul);
        model.addAttribute("chartboyBatchDate", chartboyDay.map(LocalDate::toString).orElse(null));
        model.addAttribute("hyonyBatchDate", hyonyDay.map(LocalDate::toString).orElse(null));
        model.addAttribute("chartboyMarkdown", PriceAlertLogMarkdownComposer.composeChartboyMarkdown(chartboyRows));
        model.addAttribute("hyonyMarkdown", PriceAlertLogMarkdownComposer.composeHyonyMarkdown(
                hyonyDay.orElse(null), hyonyRows));
        model.addAttribute("triggerLogHref", appProperties.publicPriceAlertLogPath());
        model.addAttribute("slugListHref", appProperties.publicPriceAlertListSlugHref());
        return "price-alerts/new";
    }

    @GetMapping("/admin/price-alerts")
    public String listAdmin(@AuthenticationPrincipal OAuth2User user,
                            @RequestParam(name = "page", defaultValue = "1") int page,
                            @RequestParam(name = "size", defaultValue = "50") int size,
                            @RequestParam(name = "source", required = false) String source,
                            @RequestParam(name = "q", required = false) String q,
                            Model model) {
        Optional<String> search = PriceAlertSearchSanitizer.validated(q);
        boolean rejected = PriceAlertSearchSanitizer.wasRejected(q, search);
        return renderList(model, page, size, source,
                user != null ? resolveKakaoNickname(user) : null, user != null, "admin/price-alerts", false,
                search, rejected, false, false, Optional.empty(), Optional.empty(), VIEW_PRICE_ALERTS_LIST);
    }

    @GetMapping("/price-alerts/edit")
    public String editFormUser(@RequestParam(name = "id", required = false) Long id,
                               @AuthenticationPrincipal OAuth2User user,
                               Model model) {
        return renderEditForm(id, model, resolveKakaoNickname(user), true, "price-alerts");
    }

    @GetMapping("/admin/price-alerts/edit")
    public String editFormAdmin(@RequestParam(name = "id", required = false) Long id,
                                @AuthenticationPrincipal OAuth2User user,
                                Model model) {
        return renderEditForm(id, model, user != null ? resolveKakaoNickname(user) : null, user != null, "admin/price-alerts");
    }

    @PostMapping("/price-alerts/edit")
    public String saveUser(@ModelAttribute("form") PriceAlertForm form,
                           RedirectAttributes redirectAttributes) {
        return savePriceAlert(form, redirectAttributes, "price-alerts");
    }

    @PostMapping("/admin/price-alerts/edit")
    public String saveAdmin(@ModelAttribute("form") PriceAlertForm form,
                            RedirectAttributes redirectAttributes) {
        return savePriceAlert(form, redirectAttributes, "admin/price-alerts");
    }

    @PostMapping("/price-alerts/delete")
    public String deleteUser(@RequestParam("id") Long id) {
        supabaseService.deletePriceAlert(id);
        alertService.invalidateAlertsCache();
        return "redirect:/price-alerts";
    }

    /** 자동완성: 로그인 사용자(/price-alerts) — source 필터 함께 사용 가능 */
    @GetMapping("/price-alerts/suggest")
    @ResponseBody
    public List<Map<String, String>> suggestUser(@RequestParam(name = "q", required = false) String q,
                                                 @RequestParam(name = "source", required = false) String source) {
        return suggest(q, source);
    }

    /** 자동완성: 관리(/admin/price-alerts) */
    @GetMapping("/admin/price-alerts/suggest")
    @ResponseBody
    public List<Map<String, String>> suggestAdmin(@RequestParam(name = "q", required = false) String q,
                                                  @RequestParam(name = "source", required = false) String source) {
        return suggest(q, source);
    }

    /** 자동완성: 공개({@code /private/{slug}/list/suggest}) — 항상 CHARTBOY 한정 */
    @GetMapping("private/${app.price-alert-public-slug}/list/suggest")
    @ResponseBody
    public List<Map<String, String>> suggestChartboyPublic(@RequestParam(name = "q", required = false) String q,
                                                           HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        return suggest(q, "CHARTBOY");
    }

    /** 자동완성: {@code /{slug}/suggest} — {@code /{slug}} 목록 전용 · CHARTBOY 한정 */
    @GetMapping("${app.price-alert-public-slug}/suggest")
    @ResponseBody
    public List<Map<String, String>> suggestChartboySlugRoot(@RequestParam(name = "q", required = false) String q,
                                                             HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        return suggest(q, "CHARTBOY");
    }

    /** 자동완성: /stocker 별칭 — CHARTBOY 한정 */
    @GetMapping("/stocker/suggest")
    @ResponseBody
    public List<Map<String, String>> suggestStockerPublic(@RequestParam(name = "q", required = false) String q,
                                                          HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        return suggest(q, "CHARTBOY");
    }

    /** 자동완성: 돌파 로그 공개 URL — 트리거 테이블 기준·CHARTBOY 한정 */
    @GetMapping("${app.price-alert-public-slug}/log/suggest")
    @ResponseBody
    public List<Map<String, String>> suggestChartboyLog(@RequestParam(name = "q", required = false) String q,
                                                        HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        Optional<String> v = PriceAlertSearchSanitizer.validated(q);
        if (v.isEmpty()) {
            return List.of();
        }
        return supabaseService.suggestPriceAlertTriggers(v.get(), "CHARTBOY", 10);
    }

    private List<Map<String, String>> suggest(String q, String source) {
        Optional<String> v = PriceAlertSearchSanitizer.validated(q);
        if (v.isEmpty()) return List.of();
        return supabaseService.suggestPriceAlerts(v.get(), source, 10);
    }

    @PostMapping("/admin/price-alerts/delete")
    public String deleteAdmin(@RequestParam("id") Long id) {
        supabaseService.deletePriceAlert(id);
        alertService.invalidateAlertsCache();
        return "redirect:/admin/price-alerts";
    }

    private String savePriceAlert(PriceAlertForm form, RedirectAttributes redirectAttributes, String listPathPrefix) {
        try {
            validateForm(form);
            BigDecimal tp = parseTargetPrice(form.getTargetPrice());
            PriceAlertDto dto = toDto(form, tp);
            if (form.getId() != null) {
                supabaseService.updatePriceAlert(form.getId(), dto, form.isClearTriggered());
            } else {
                supabaseService.insertPriceAlert(dto);
            }
            alertService.invalidateAlertsCache();
            return "redirect:/" + listPathPrefix;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (form.getId() != null) {
                return "redirect:/" + listPathPrefix + "/edit?id=" + form.getId();
            }
            return "redirect:/" + listPathPrefix + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "저장 실패: " + e.getMessage());
            if (form.getId() != null) {
                return "redirect:/" + listPathPrefix + "/edit?id=" + form.getId();
            }
            return "redirect:/" + listPathPrefix + "/edit";
        }
    }

    private static void validateForm(PriceAlertForm f) {
        if (f.getStockCode() == null || f.getStockCode().isBlank()) {
            throw new IllegalArgumentException("종목코드(stock_code)는 필수입니다.");
        }
        if (f.getTargetPrice() == null || f.getTargetPrice().isBlank()) {
            throw new IllegalArgumentException("목표가는 필수입니다.");
        }
        String m = f.getMarket() != null ? f.getMarket().trim().toUpperCase() : "KR";
        if (!m.equals("KR") && !m.equals("NAS") && !m.equals("NYS") && !m.equals("AMS")) {
            throw new IllegalArgumentException("시장은 KR, NAS, NYS, AMS 중 하나여야 합니다.");
        }
        String c = f.getCondition() != null ? f.getCondition().trim().toUpperCase() : "ABOVE";
        if (!c.equals("ABOVE") && !c.equals("BELOW")) {
            throw new IllegalArgumentException("조건은 ABOVE 또는 BELOW 여야 합니다.");
        }
        String s = f.getSource() != null ? f.getSource().trim().toUpperCase() : "MY";
        if (!s.equals("CHARTBOY") && !s.equals("MY") && !s.equals("MANUAL")) {
            throw new IllegalArgumentException("출처는 CHARTBOY, MY, MANUAL 중 하나여야 합니다.");
        }
    }

    private static BigDecimal parseTargetPrice(String raw) {
        String n = raw.replace(",", "").trim();
        if (n.isEmpty()) throw new IllegalArgumentException("목표가를 입력하세요.");
        try {
            return new BigDecimal(n);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("목표가는 숫자만 입력하세요.");
        }
    }

    private static PriceAlertDto toDto(PriceAlertForm f, BigDecimal targetPrice) {
        PriceAlertDto d = new PriceAlertDto();
        d.setMarket(f.getMarket() != null ? f.getMarket().trim().toUpperCase() : "KR");
        d.setStockCode(f.getStockCode().trim());
        d.setSymbol(f.getSymbol() != null && !f.getSymbol().isBlank() ? f.getSymbol().trim() : null);
        d.setTargetPrice(targetPrice);
        d.setCondition(f.getCondition() != null ? f.getCondition().trim().toUpperCase() : "ABOVE");
        d.setLabel(f.getLabel() != null && !f.getLabel().isBlank() ? f.getLabel().trim() : null);
        d.setSource(f.getSource() != null ? f.getSource().trim().toUpperCase() : "MY");
        d.setIsActive(!"false".equalsIgnoreCase(f.getIsActive()));
        return d;
    }

    private String renderList(Model model, int page, int size, String source,
                              String userNickname, boolean showLogout, String listPathPrefix, boolean readOnly,
                              Optional<String> searchFilter, boolean searchRejected,
                              boolean listEmptyUnlessSearchOrRegistered,
                              boolean latestCreatedDayOnlyWhenNoSearch,
                              Optional<LocalDate> latestCreatedSeoulDay,
                              Optional<LocalDate> registeredSeoulDay,
                              String listViewName) {
        int safeSize = size > 0 && size <= 200 ? size : DEFAULT_PAGE_SIZE;
        int safePage = page < 1 ? 1 : page;
        int offset = (safePage - 1) * safeSize;

        boolean deferSlugLikeEmptyList = listEmptyUnlessSearchOrRegistered
                && searchFilter.isEmpty()
                && registeredSeoulDay.isEmpty();

        PriceAlertPageResult result;
        if (deferSlugLikeEmptyList) {
            result = new PriceAlertPageResult(List.of(), 0);
        } else if (latestCreatedDayOnlyWhenNoSearch && searchFilter.isEmpty() && registeredSeoulDay.isEmpty()) {
            if (latestCreatedSeoulDay.isEmpty()) {
                result = new PriceAlertPageResult(List.of(), 0);
            } else {
                result = supabaseService.getPriceAlerts(safeSize, offset, "CHARTBOY", Optional.empty(),
                        latestCreatedSeoulDay);
            }
        } else {
            result = supabaseService.getPriceAlerts(safeSize, offset, source, searchFilter, registeredSeoulDay);
        }

        model.addAttribute("alerts", result.list());
        model.addAttribute("totalCount", result.totalCount());
        model.addAttribute("totalPages", Math.max(1, (int) Math.ceil(result.totalCount() / (double) safeSize)));
        model.addAttribute("currentPage", safePage);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("sourceFilter", source != null && !source.isBlank() ? source : "ALL");
        LocalDate todaySeoul = LocalDate.now(ZoneId.of("Asia/Seoul"));
        model.addAttribute("today", todaySeoul);
        model.addAttribute("userNickname", userNickname);
        model.addAttribute("showLogout", showLogout);
        model.addAttribute("listPathPrefix", listPathPrefix);
        model.addAttribute("readOnly", readOnly);
        model.addAttribute("searchQuery", searchFilter.orElse(""));
        model.addAttribute("searchRejected", searchRejected);
        model.addAttribute("registeredDate", registeredSeoulDay.map(LocalDate::toString).orElse(""));
        model.addAttribute("registeredFilterActive", readOnly && registeredSeoulDay.isPresent());
        model.addAttribute("registeredFieldValue",
                readOnly ? registeredSeoulDay.map(LocalDate::toString).orElse("") : "");
        model.addAttribute("listQueryExtra", buildListQueryExtra(source, searchFilter, readOnly, registeredSeoulDay));
        model.addAttribute("suggestUrl", "/" + listPathPrefix + "/suggest");
        model.addAttribute("slugListShowsSearchHint", VIEW_PRICE_ALERTS_SLUG_ROOT.equals(listViewName)
                && searchFilter.isEmpty() && registeredSeoulDay.isEmpty() && !searchRejected);

        if (readOnly) {
            model.addAttribute("publicPriceAlertListSlugHref", appProperties.publicPriceAlertListSlugHref());
            model.addAttribute("publicPriceAlertLogPath", appProperties.publicPriceAlertLogPath());
            model.addAttribute("publicPriceAlertMemoLogNewPath", appProperties.publicPriceAlertMemoLogNewPath());
        }
        return listViewName;
    }

    /**
     * 페이징·검색 링크용 쿼리 조각 ({@code &} 로 시작하는 연결).
     * 공개 목록({@code readOnly})은 API에서 항상 CHARTBOY만 조회하므로 URL에 {@code source} 를 넣지 않음.
     */
    private static String buildListQueryExtra(String source, Optional<String> searchFilter, boolean chartboyPublicList,
                                              Optional<LocalDate> registeredSeoulDay) {
        StringBuilder sb = new StringBuilder();
        if (!chartboyPublicList) {
            String s = source != null ? source.trim() : "";
            if (!s.isEmpty() && !"ALL".equalsIgnoreCase(s)) {
                sb.append("&source=").append(URLEncoder.encode(s, StandardCharsets.UTF_8));
            }
        }
        searchFilter.ifPresent(v -> sb.append("&q=").append(URLEncoder.encode(v, StandardCharsets.UTF_8)));
        registeredSeoulDay.ifPresent(d -> sb.append("&registered=").append(URLEncoder.encode(d.toString(),
                StandardCharsets.UTF_8)));
        return sb.toString();
    }

    private String renderTriggerLog(Model model, int page, int size,
                                    Optional<String> searchFilter, boolean searchRejected,
                                    HttpServletRequest request, String fromQuery,
                                    String triggeredRaw) {
        int safeSize = size > 0 && size <= 200 ? size : DEFAULT_PAGE_SIZE;
        int safePage = page < 1 ? 1 : page;
        int offset = (safePage - 1) * safeSize;

        LocalDate todaySeoul = LocalDate.now(ZoneId.of("Asia/Seoul"));
        boolean logAllTriggerDates = triggeredRaw != null && "all".equalsIgnoreCase(triggeredRaw.trim());
        Optional<LocalDate> triggeredQueryDay = logAllTriggerDates ? Optional.empty()
                : parseRegisteredDateParam(triggeredRaw).or(() -> Optional.of(todaySeoul));

        PriceAlertTriggerPageResult result = supabaseService.getPriceAlertTriggers(safeSize, offset, "CHARTBOY",
                searchFilter, triggeredQueryDay);

        TriggerLogMainLink backNav = resolveTriggerLogMainLink(request, Optional.ofNullable(fromQuery));
        boolean triggerLogAllDates = logAllTriggerDates;
        String triggerLogDayField = triggerLogAllDates ? "" : triggeredQueryDay.map(LocalDate::toString).orElse("");

        model.addAttribute("triggers", result.list());
        model.addAttribute("totalCount", result.totalCount());
        model.addAttribute("totalPages", Math.max(1, (int) Math.ceil(result.totalCount() / (double) safeSize)));
        model.addAttribute("currentPage", safePage);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("today", todaySeoul);
        model.addAttribute("triggerLogAllDates", triggerLogAllDates);
        model.addAttribute("triggerLogDayField", triggerLogDayField);
        model.addAttribute("listPathPrefix", publicLogPathPrefix());
        model.addAttribute("searchQuery", searchFilter.orElse(""));
        model.addAttribute("searchRejected", searchRejected);
        model.addAttribute("listQueryExtra", buildTriggerLogQueryExtra(searchFilter, backNav.fromParam(),
                triggerLogAllDates, triggeredQueryDay));
        model.addAttribute("triggerLogStickyQueryExtra",
                buildTriggerLogQueryExtra(Optional.empty(), backNav.fromParam(),
                        triggerLogAllDates, triggeredQueryDay));
        model.addAttribute("triggerLogAllPeriodExtra",
                buildTriggerLogQueryExtra(Optional.empty(), backNav.fromParam(), true, Optional.empty()));
        model.addAttribute("triggerLogResetToTodayExtra",
                buildTriggerLogQueryExtra(Optional.empty(), backNav.fromParam(), false, Optional.of(todaySeoul)));
        model.addAttribute("suggestUrl", appProperties.publicPriceAlertLogPath() + "/suggest");
        model.addAttribute("publicPriceAlertMemoLogNewPath", appProperties.publicPriceAlertMemoLogNewPath());
        model.addAttribute("triggerLogMainListHref", backNav.href());
        model.addAttribute("triggerLogFromParam", backNav.fromParam());
        return "price-alerts/log";
    }

    private String publicListPathPrefix() {
        return "private/" + appProperties.normalizedPriceAlertPublicSlug() + "/list";
    }

    private String publicLogPathPrefix() {
        return appProperties.normalizedPriceAlertPublicSlug() + "/log";
    }

    /** {@link #renderTriggerLog} 페이징·검색 링크 — {@code &} 로 시작. */
    private static String buildTriggerLogQueryExtra(Optional<String> searchFilter, String fromParam,
                                                    boolean logAllTriggerDates, Optional<LocalDate> triggeredSeoulDay) {
        StringBuilder sb = new StringBuilder();
        searchFilter.ifPresent(v -> sb.append("&q=").append(URLEncoder.encode(v, StandardCharsets.UTF_8)));
        if (fromParam != null && !fromParam.isBlank()) {
            sb.append("&from=").append(URLEncoder.encode(fromParam.trim().toLowerCase(), StandardCharsets.UTF_8));
        }
        if (logAllTriggerDates) {
            sb.append("&triggered=all");
        } else {
            triggeredSeoulDay.ifPresent(d -> sb.append("&triggered=")
                    .append(URLEncoder.encode(d.toString(), StandardCharsets.UTF_8)));
        }
        return sb.toString();
    }

    /** 돌파 로그 화면 “메인 목록” 목적지 및 페이징·검색 시 연속되는 {@code &from=} 값. */
    private record TriggerLogMainLink(String href, String fromParam) {}

    private TriggerLogMainLink resolveTriggerLogMainLink(HttpServletRequest request,
                                                         Optional<String> fromQueryRaw) {
        Optional<String> fromCanonical = fromQueryRaw
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .filter(s -> s.equals("slug") || s.equals("list") || s.equals("private"));
        String from = fromCanonical.orElseGet(() -> inferTriggerLogFromReferer(request));
        String slug = appProperties.normalizedPriceAlertPublicSlug();
        return switch (from) {
            case "private" -> new TriggerLogMainLink(appProperties.publicPriceAlertListPath(), from);
            case "list" -> new TriggerLogMainLink("/" + slug, "slug");
            default -> new TriggerLogMainLink(appProperties.publicPriceAlertListSlugHref(), "slug");
        };
    }

    private String inferTriggerLogFromReferer(HttpServletRequest request) {
        String slug = appProperties.normalizedPriceAlertPublicSlug();
        String ref = request.getHeader("Referer");
        if (ref == null || ref.isBlank()) {
            return "slug";
        }
        try {
            String path = stripContextNormalizePath(request, URI.create(ref).normalize().getRawPath());
            String privateList = "/private/" + slug + "/list";
            String shortList = "/" + slug + "/list";
            String slugRoot = "/" + slug;
            if ("/stocker".equals(path)) {
                return "private";
            }
            if (privateList.equals(path)) {
                return "private";
            }
            if (shortList.equals(path) || slugRoot.equals(path)) {
                return "slug";
            }
        } catch (IllegalArgumentException ignored) {
            // malformed Referer
        }
        return "slug";
    }

    /** Referer 또는 URI 경로 문자열에서 context-path 제거 후 끝 슬래시 정리. */
    private static String stripContextNormalizePath(HttpServletRequest request, String rawPath) {
        String path = rawPath != null ? rawPath : "/";
        String ctx = request.getContextPath();
        if (!ctx.isEmpty() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        if (path.isBlank()) {
            path = "/";
        }
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private String renderEditForm(Long id, Model model, String userNickname, boolean showLogout, String listPathPrefix) {
        PriceAlertForm form = new PriceAlertForm();
        if (id != null) {
            PriceAlertDto ex = supabaseService.getPriceAlertById(id);
            if (ex == null) {
                return "redirect:/" + listPathPrefix;
            }
            form.setId(ex.getId());
            form.setMarket(ex.getMarket() != null ? ex.getMarket() : "KR");
            form.setStockCode(ex.getStockCode());
            form.setSymbol(ex.getSymbol());
            form.setTargetPrice(ex.getTargetPrice() != null ? ex.getTargetPrice().toPlainString() : "");
            form.setCondition(ex.getCondition() != null ? ex.getCondition() : "ABOVE");
            form.setLabel(ex.getLabel());
            form.setSource(ex.getSource() != null ? ex.getSource() : "MY");
            form.setIsActive(ex.getIsActive() == null || ex.getIsActive() ? "true" : "false");
        }
        model.addAttribute("form", form);
        model.addAttribute("listPathPrefix", listPathPrefix);
        model.addAttribute("userNickname", userNickname);
        model.addAttribute("showLogout", showLogout);
        return "price-alerts/edit";
    }

    /** 공개 목록 {@code registered} 쿼리 — {@code yyyy-MM-dd} 만 허용, 그 외 무시. */
    private static Optional<LocalDate> parseRegisteredDateParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(raw.trim()));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private static String resolveKakaoNickname(OAuth2User user) {
        if (user == null) return null;
        Object props = user.getAttributes().get("properties");
        if (props instanceof Map<?, ?> map) {
            Object nickname = map.get("nickname");
            return nickname != null && !nickname.toString().isBlank() ? nickname.toString() : null;
        }
        return null;
    }
}
