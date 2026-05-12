package com.example.stocks.web;

import com.example.stocks.alert.AlertService;
import com.example.stocks.alert.PriceAlertDto;
import com.example.stocks.config.AppProperties;
import com.example.stocks.supabase.PriceAlertPageResult;
import com.example.stocks.supabase.PriceAlertTriggerPageResult;
import com.example.stocks.supabase.SupabaseService;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Supabase {@code price_alerts} 테이블 조회·등록·수정·삭제 및
 * {@code price_alert_triggers} 돌파 로그 공개 화면({@code /{app.price-alert-public-slug}/log}).
 * 스키마: {@code src/main/resources/price_alerts.sql}, {@code price_alert_triggers_ddl.sql}
 */
@Controller
public class PriceAlertViewController {

    private static final int DEFAULT_PAGE_SIZE = 50;
    /** 공개 페이지 응답 캐시 (초). 브라우저·중간 캐시 모두에서 30초 동안 재사용 → Supabase egress 절감. */
    private static final String PUBLIC_CACHE_CONTROL = "public, max-age=30";

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
                search, rejected);
    }

    /**
     * 카카오 로그인 없이 목표가 알람 목록만 조회 (등록·수정·삭제 URL은 제공하지 않음).
     */
    @GetMapping("${app.price-alert-public-slug}/list")
    public String listChartboyPublic(@RequestParam(name = "page", defaultValue = "1") int page,
                                     @RequestParam(name = "size", defaultValue = "50") int size,
                                     @RequestParam(name = "q", required = false) String q,
                                     HttpServletResponse response,
                                     Model model) {
        Optional<String> search = PriceAlertSearchSanitizer.validated(q);
        boolean rejected = PriceAlertSearchSanitizer.wasRejected(q, search);
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        return renderList(model, page, size, "CHARTBOY", null, false, publicListPathPrefix(), true, search, rejected);
    }

    /**
     * {@code /stocker} 별칭 공개 경로. {@code /{slug}/list} 와 동일하게 CHARTBOY 한정 읽기 전용 목록.
     */
    @GetMapping("/stocker")
    public String listStockerPublic(@RequestParam(name = "page", defaultValue = "1") int page,
                                    @RequestParam(name = "size", defaultValue = "50") int size,
                                    @RequestParam(name = "q", required = false) String q,
                                    HttpServletResponse response,
                                    Model model) {
        Optional<String> search = PriceAlertSearchSanitizer.validated(q);
        boolean rejected = PriceAlertSearchSanitizer.wasRejected(q, search);
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        return renderList(model, page, size, "CHARTBOY", null, false, "stocker", true, search, rejected);
    }

    /** 오타 URL → 정식 경로 */
    @GetMapping("/chartbody/log")
    public String chartbodyTypoRedirect() {
        return "redirect:" + appProperties.publicPriceAlertLogPath();
    }

    /**
     * 차트보이(CHARTBOY) 목표가 돌파 로그 공개 조회. 종목명·코드 부분 일치 필터·자동완성·모바일 레이아웃.
     */
    @GetMapping("${app.price-alert-public-slug}/log")
    public String listChartboyTriggerLog(@RequestParam(name = "page", defaultValue = "1") int page,
                                         @RequestParam(name = "size", defaultValue = "50") int size,
                                         @RequestParam(name = "q", required = false) String q,
                                         HttpServletResponse response,
                                         Model model) {
        Optional<String> search = PriceAlertSearchSanitizer.validated(q);
        boolean rejected = PriceAlertSearchSanitizer.wasRejected(q, search);
        response.setHeader(HttpHeaders.CACHE_CONTROL, PUBLIC_CACHE_CONTROL);
        return renderTriggerLog(model, page, size, search, rejected);
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
                search, rejected);
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

    /** 자동완성: 공개(/chartboy/list) — 항상 CHARTBOY 한정 */
    @GetMapping("${app.price-alert-public-slug}/list/suggest")
    @ResponseBody
    public List<Map<String, String>> suggestChartboyPublic(@RequestParam(name = "q", required = false) String q,
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
                              Optional<String> searchFilter, boolean searchRejected) {
        int safeSize = size > 0 && size <= 200 ? size : DEFAULT_PAGE_SIZE;
        int safePage = page < 1 ? 1 : page;
        int offset = (safePage - 1) * safeSize;

        PriceAlertPageResult result = supabaseService.getPriceAlerts(safeSize, offset, source, searchFilter);

        model.addAttribute("alerts", result.list());
        model.addAttribute("totalCount", result.totalCount());
        model.addAttribute("totalPages", Math.max(1, (int) Math.ceil(result.totalCount() / (double) safeSize)));
        model.addAttribute("currentPage", safePage);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("sourceFilter", source != null && !source.isBlank() ? source : "ALL");
        model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")));
        model.addAttribute("userNickname", userNickname);
        model.addAttribute("showLogout", showLogout);
        model.addAttribute("listPathPrefix", listPathPrefix);
        model.addAttribute("readOnly", readOnly);
        model.addAttribute("searchQuery", searchFilter.orElse(""));
        model.addAttribute("searchRejected", searchRejected);
        model.addAttribute("listQueryExtra", buildListQueryExtra(source, searchFilter, readOnly));
        model.addAttribute("suggestUrl", "/" + listPathPrefix + "/suggest");
        if (readOnly) {
            model.addAttribute("publicPriceAlertListPath", appProperties.publicPriceAlertListPath());
            model.addAttribute("publicPriceAlertLogPath", appProperties.publicPriceAlertLogPath());
        }
        return "price-alerts/list";
    }

    /**
     * 페이징·검색 링크용 쿼리 조각 ({@code &} 로 시작하는 연결).
     * 공개 목록({@code readOnly})은 API에서 항상 CHARTBOY만 조회하므로 URL에 {@code source} 를 넣지 않음.
     */
    private static String buildListQueryExtra(String source, Optional<String> searchFilter, boolean chartboyPublicList) {
        StringBuilder sb = new StringBuilder();
        if (!chartboyPublicList) {
            String s = source != null ? source.trim() : "";
            if (!s.isEmpty() && !"ALL".equalsIgnoreCase(s)) {
                sb.append("&source=").append(URLEncoder.encode(s, StandardCharsets.UTF_8));
            }
        }
        searchFilter.ifPresent(v -> sb.append("&q=").append(URLEncoder.encode(v, StandardCharsets.UTF_8)));
        return sb.toString();
    }

    private String renderTriggerLog(Model model, int page, int size,
                                     Optional<String> searchFilter, boolean searchRejected) {
        int safeSize = size > 0 && size <= 200 ? size : DEFAULT_PAGE_SIZE;
        int safePage = page < 1 ? 1 : page;
        int offset = (safePage - 1) * safeSize;

        PriceAlertTriggerPageResult result = supabaseService.getPriceAlertTriggers(safeSize, offset, "CHARTBOY", searchFilter);

        model.addAttribute("triggers", result.list());
        model.addAttribute("totalCount", result.totalCount());
        model.addAttribute("totalPages", Math.max(1, (int) Math.ceil(result.totalCount() / (double) safeSize)));
        model.addAttribute("currentPage", safePage);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")));
        model.addAttribute("listPathPrefix", publicLogPathPrefix());
        model.addAttribute("searchQuery", searchFilter.orElse(""));
        model.addAttribute("searchRejected", searchRejected);
        model.addAttribute("listQueryExtra", buildTriggerLogQueryExtra(searchFilter));
        model.addAttribute("suggestUrl", appProperties.publicPriceAlertLogPath() + "/suggest");
        model.addAttribute("publicPriceAlertListPath", appProperties.publicPriceAlertListPath());
        model.addAttribute("publicPriceAlertLogPath", appProperties.publicPriceAlertLogPath());
        return "price-alerts/trigger-log";
    }

    private String publicListPathPrefix() {
        return appProperties.normalizedPriceAlertPublicSlug() + "/list";
    }

    private String publicLogPathPrefix() {
        return appProperties.normalizedPriceAlertPublicSlug() + "/log";
    }

    private static String buildTriggerLogQueryExtra(Optional<String> searchFilter) {
        StringBuilder sb = new StringBuilder();
        searchFilter.ifPresent(v -> sb.append("&q=").append(URLEncoder.encode(v, StandardCharsets.UTF_8)));
        return sb.toString();
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
