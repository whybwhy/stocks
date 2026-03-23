package com.example.stocks.web;

import com.example.stocks.alert.PriceAlertDto;
import com.example.stocks.supabase.PriceAlertPageResult;
import com.example.stocks.supabase.SupabaseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

/**
 * Supabase {@code price_alerts} 테이블 조회·등록·수정·삭제.
 * 스키마: {@code src/main/resources/price_alerts.sql}
 */
@Controller
public class PriceAlertViewController {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final SupabaseService supabaseService;

    public PriceAlertViewController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping("/price-alerts")
    public String list(@AuthenticationPrincipal OAuth2User user,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "50") int size,
                       @RequestParam(name = "source", required = false) String source,
                       Model model) {
        return renderList(model, page, size, source, resolveKakaoNickname(user), true, "price-alerts");
    }

    @GetMapping("/admin/price-alerts")
    public String listAdmin(@AuthenticationPrincipal OAuth2User user,
                            @RequestParam(name = "page", defaultValue = "1") int page,
                            @RequestParam(name = "size", defaultValue = "50") int size,
                            @RequestParam(name = "source", required = false) String source,
                            Model model) {
        return renderList(model, page, size, source,
                user != null ? resolveKakaoNickname(user) : null, user != null, "admin/price-alerts");
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
        return "redirect:/price-alerts";
    }

    @PostMapping("/admin/price-alerts/delete")
    public String deleteAdmin(@RequestParam("id") Long id) {
        supabaseService.deletePriceAlert(id);
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
                              String userNickname, boolean showLogout, String listPathPrefix) {
        int safeSize = size > 0 && size <= 200 ? size : DEFAULT_PAGE_SIZE;
        int safePage = page < 1 ? 1 : page;
        int offset = (safePage - 1) * safeSize;

        PriceAlertPageResult result = supabaseService.getPriceAlerts(safeSize, offset, source);

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
        return "price-alerts/list";
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
