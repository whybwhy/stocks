package com.example.stocks.web;

import com.example.stocks.domain.StockServiceType;
import com.example.stocks.supabase.SupabaseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.stocks.supabase.StockDto;
import com.example.stocks.supabase.StockPageResult;
import org.springframework.web.client.HttpClientErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class StockViewController {

    private final SupabaseService supabaseService;
    private static final Logger log = LoggerFactory.getLogger(StockViewController.class);

    public StockViewController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping({"/", "/index"})
    public String home(Principal principal, Model model,
                       @RequestParam(name = "error", required = false) String error) {
        if (principal != null) {
            return "redirect:/stocks";
        }
        model.addAttribute("loginError", error);
        return "index";
    }

    private static final int DEFAULT_PAGE_SIZE = 25;

    @GetMapping("/stocks/{service}")
    public String stocks(@PathVariable("service") String service,
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        @RequestParam(name = "size", defaultValue = "25") int size,
                        @AuthenticationPrincipal OAuth2User user,
                        Model model) {
        String actual = resolveServiceActual(service);
        int safeSize = size > 0 && size <= 100 ? size : DEFAULT_PAGE_SIZE;
        int safePage = page < 1 ? 1 : page;
        int offset = (safePage - 1) * safeSize;
        if (actual != null) {
            StockPageResult result = supabaseService.getStocks(actual, safeSize, offset);
            model.addAttribute("stocks", result.list());
            model.addAttribute("totalCount", result.totalCount());
            model.addAttribute("totalPages", (result.totalCount() + safeSize - 1) / safeSize);
        } else {
            model.addAttribute("stocks", List.of());
            model.addAttribute("totalCount", 0L);
            model.addAttribute("totalPages", 0);
        }
        model.addAttribute("currentPage", safePage);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")));
        model.addAttribute("userNickname", resolveKakaoNickname(user));
        model.addAttribute("showLogout", true);
        model.addAttribute("service", service);
        model.addAttribute("editPathPrefix", "stocks");
        return "stocks/list";
    }

    /** 로그인 없이 주식 목록 조회용. 운영에서는 경로 변경 또는 제거 권장. */
    @GetMapping("/admin/stocks/{service}")
    public String stocksAdmin(@PathVariable("service") String service,
                              @RequestParam(name = "page", defaultValue = "1") int page,
                              @RequestParam(name = "size", defaultValue = "25") int size,
                              @AuthenticationPrincipal OAuth2User user,
                              Model model) {
        String actual = resolveServiceActual(service);
        log.info("service: {}, actual: {}", service, actual);
        int safeSize = size > 0 && size <= 100 ? size : DEFAULT_PAGE_SIZE;
        int safePage = page < 1 ? 1 : page;
        int offset = (safePage - 1) * safeSize;
        if (actual != null) {
            StockPageResult result = supabaseService.getStocks(actual, safeSize, offset);
            model.addAttribute("stocks", result.list());
            model.addAttribute("totalCount", result.totalCount());
            model.addAttribute("totalPages", (result.totalCount() + safeSize - 1) / safeSize);
        } else {
            model.addAttribute("stocks", List.of());
            model.addAttribute("totalCount", 0L);
            model.addAttribute("totalPages", 0);
        }
        model.addAttribute("currentPage", safePage);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")));
        model.addAttribute("userNickname", user != null ? resolveKakaoNickname(user) : null);
        model.addAttribute("showLogout", user != null);
        model.addAttribute("service", service);
        model.addAttribute("editPathPrefix", "admin/stocks");
        return "stocks/list";
    }

    @GetMapping("/stocks/{service}/edit")
    public String editForm(@PathVariable("service") String service,
                           @RequestParam(name = "id", required = false) Long id,
                           @AuthenticationPrincipal OAuth2User user,
                           Model model) {
        String actual = resolveServiceActual(service);
        if (actual == null) {
            model.addAttribute("stocks", List.of());
            model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")));
            model.addAttribute("userNickname", resolveKakaoNickname(user));
            model.addAttribute("showLogout", true);
            model.addAttribute("service", service);
            model.addAttribute("editPathPrefix", "stocks");
            return "stocks/list";
        }
        StockEditForm form = new StockEditForm();
        String nickname = resolveKakaoNickname(user);
        form.setAccount(nickname);
        if (id != null) {
            StockDto existing = supabaseService.getStockById(actual, id);
            if (existing != null) {
                form.setId(existing.getId());
                form.setStockName(existing.getStockName());
                form.setFirstBuyPrice(existing.getFirstBuyPrice());
                form.setSecondBuyPrice(existing.getSecondBuyPrice());
                form.setThirdBuyPrice(existing.getThirdBuyPrice());
                form.setDescription(existing.getDescription());
                form.setAccount(existing.getAccount() != null ? existing.getAccount() : nickname);
                form.setType(existing.getType());
                form.setTicker(existing.getTicker());
                form.setSector(existing.getSector());
            }
        }
        if (form.getType() == null || form.getType().isBlank()) {
            form.setType("KR");
        }
        List<String> sectorOptions = supabaseService.getStocksForExport(actual).stream()
                .map(StockDto::getSector)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        model.addAttribute("form", form);
        model.addAttribute("sectorOptions", sectorOptions);
        model.addAttribute("service", service);
        model.addAttribute("editPathPrefix", "stocks");
        model.addAttribute("userNickname", nickname);
        model.addAttribute("showLogout", true);
        model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")));
        return "stocks/edit";
    }

    @GetMapping("/admin/stocks/{service}/edit")
    public String editFormAdmin(@PathVariable("service") String service,
                                @RequestParam(name = "id", required = false) Long id,
                                @AuthenticationPrincipal OAuth2User user,
                                Model model) {
        String actual = resolveServiceActual(service);
        if (actual == null) {
            model.addAttribute("stocks", List.of());
            model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")));
            model.addAttribute("userNickname", user != null ? resolveKakaoNickname(user) : null);
            model.addAttribute("showLogout", user != null);
            model.addAttribute("service", service);
            model.addAttribute("editPathPrefix", "admin/stocks");
            return "stocks/list";
        }
        StockEditForm form = new StockEditForm();
        String nickname = user != null ? resolveKakaoNickname(user) : null;
        form.setAccount(nickname);
        if (id != null) {
            StockDto existing = supabaseService.getStockById(actual, id);
            if (existing != null) {
                form.setId(existing.getId());
                form.setStockName(existing.getStockName());
                form.setFirstBuyPrice(existing.getFirstBuyPrice());
                form.setSecondBuyPrice(existing.getSecondBuyPrice());
                form.setThirdBuyPrice(existing.getThirdBuyPrice());
                form.setDescription(existing.getDescription());
                form.setAccount(existing.getAccount() != null ? existing.getAccount() : nickname);
                form.setType(existing.getType());
                form.setTicker(existing.getTicker());
                form.setSector(existing.getSector());
            }
        }
        if (form.getType() == null || form.getType().isBlank()) {
            form.setType("KR");
        }
        List<String> sectorOptions = supabaseService.getStocksForExport(actual).stream()
                .map(StockDto::getSector)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        model.addAttribute("form", form);
        model.addAttribute("sectorOptions", sectorOptions);
        model.addAttribute("service", service);
        model.addAttribute("editPathPrefix", "admin/stocks");
        model.addAttribute("userNickname", nickname);
        model.addAttribute("showLogout", user != null);
        model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")));
        return "stocks/edit";
    }

    @PostMapping("/stocks/{service}/edit")
    public String saveEdit(@PathVariable("service") String service,
                           @ModelAttribute("form") StockEditForm form,
                           @AuthenticationPrincipal OAuth2User user) {
        String actual = resolveServiceActual(service);
        if (actual == null) {
            return "redirect:/stocks";
        }
        String account = form.getAccount() != null && !form.getAccount().isBlank()
                ? form.getAccount().trim()
                : (resolveKakaoNickname(user) != null ? resolveKakaoNickname(user) : "");
        StockDto dto = toDto(form, account);
        Long targetId = resolveTargetIdAndUpsert(actual, form, dto);
        supabaseService.insertHistory(account != null ? account : "", actual, targetId);
        return "redirect:/stocks/" + service;
    }

    @PostMapping("/admin/stocks/{service}/edit")
    public String saveEditAdmin(@PathVariable("service") String service,
                                @ModelAttribute("form") StockEditForm form,
                                @AuthenticationPrincipal OAuth2User user) {
        String actual = resolveServiceActual(service);
        if (actual == null) {
            return "redirect:/admin/stocks/" + service;
        }
        String account = form.getAccount() != null && !form.getAccount().isBlank()
                ? form.getAccount().trim()
                : (user != null ? resolveKakaoNickname(user) : "");
        StockDto dto = toDto(form, account);
        Long targetId = resolveTargetIdAndUpsert(actual, form, dto);
        supabaseService.insertHistory(account != null ? account : "", actual, targetId);
        return "redirect:/admin/stocks/" + service;
    }

    @GetMapping("/stocks/{service}/export")
    public ResponseEntity<byte[]> exportStocks(@PathVariable("service") String service) {
        String actual = resolveServiceActual(service);
        if (actual == null) {
            return ResponseEntity.badRequest().build();
        }
        List<StockDto> list = supabaseService.getStocksForExport(actual);
        byte[] csv = buildCsv(list);
        String filename = "stocks_" + service + "_" + LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/admin/stocks/{service}/export")
    public ResponseEntity<byte[]> exportStocksAdmin(@PathVariable("service") String service) {
        String actual = resolveServiceActual(service);
        if (actual == null) {
            return ResponseEntity.badRequest().build();
        }
        List<StockDto> list = supabaseService.getStocksForExport(actual);
        byte[] csv = buildCsv(list);
        String filename = "stocks_" + service + "_" + LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    private byte[] buildCsv(List<StockDto> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF"); // UTF-8 BOM for Excel. 순서: chartboy_rows.csv 와 동일
        sb.append("id,stock,ticker,sector,first_buy_price,second_buy_price,third_buy_price,description,type,account,recommended_at,created_at,updated_at\r\n");
        // +00:00, 밀리초 제외 (yyyy-MM-dd HH:mm:ss)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (StockDto s : list) {
            sb.append(s.getId() != null ? s.getId() : "").append(",");
            sb.append(escapeCsv(s.getStockName())).append(",");
            sb.append(escapeCsv(s.getTicker())).append(",");
            sb.append(escapeCsv(s.getSector())).append(",");
            sb.append(priceForCsv(s.getFirstBuyPrice())).append(",");
            sb.append(priceForCsv(s.getSecondBuyPrice())).append(",");
            sb.append(priceForCsv(s.getThirdBuyPrice())).append(",");
            sb.append(escapeCsv(s.getDescription())).append(",");
            sb.append(escapeCsv(s.getType())).append(",");
            sb.append(escapeCsv(s.getAccount())).append(",");
            // timestamp 컬럼: Supabase CSV import 가 "" 또는 \N 을 타임스탬프로 파싱해 22007 에러 → 항상 유효한 시각 출력
            String createdAtStr = s.getCreatedAt() != null ? s.getCreatedAt().atZoneSameInstant(ZoneOffset.UTC).format(formatter) : null;
            if (createdAtStr == null) createdAtStr = java.time.ZonedDateTime.now(ZoneOffset.UTC).format(formatter);
            sb.append(escapeCsv(createdAtStr)).append(",");  // recommended_at (created_at 과 동일)
            sb.append(escapeCsv(createdAtStr)).append(",");
            sb.append(escapeCsv(createdAtStr)).append("\r\n"); // updated_at
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\r") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** CSV용 가격 (쉼표 없이 숫자만) */
    private static String priceForCsv(java.math.BigDecimal price) {
        if (price == null) return "";
        return price.toPlainString();
    }

    /** id가 있으면 update, 없으면 stock(종목명)으로 기존 조회 후 있으면 update, 없으면 insert. 중복(409) 시 목록에서 조회 후 update. */
    private Long resolveTargetIdAndUpsert(String actual, StockEditForm form, StockDto dto) {
        if (form.getId() != null) {
            supabaseService.updateStock(actual, form.getId(), dto);
            return form.getId();
        }
        StockDto existing = supabaseService.getStockByStockName(actual, form.getStockName());
        if (existing != null) {
            supabaseService.updateStock(actual, existing.getId(), dto);
            return existing.getId();
        }
        try {
            StockDto created = supabaseService.insertStock(actual, dto);
            return created.getId();
        } catch (HttpClientErrorException.Conflict e) {
            // 중복 키(이미 동일 stock 존재) → 목록에서 찾아서 update
            java.util.List<StockDto> list = supabaseService.getStocksForExport(actual);
            String name = form.getStockName();
            StockDto found = list.stream()
                    .filter(s -> name != null && name.equals(s.getStockName()))
                    .findFirst()
                    .orElse(null);
            if (found != null) {
                supabaseService.updateStock(actual, found.getId(), dto);
                return found.getId();
            }
            throw e;
        }
    }

    private static StockDto toDto(StockEditForm form, String account) {
        StockDto dto = new StockDto();
        dto.setStockName(form.getStockName());
        dto.setFirstBuyPrice(form.getFirstBuyPrice());
        dto.setSecondBuyPrice(form.getSecondBuyPrice());
        dto.setThirdBuyPrice(form.getThirdBuyPrice());
        dto.setDescription(form.getDescription());
        dto.setAccount(account);
        dto.setType(form.getType());
        dto.setTicker(form.getTicker());
        dto.setSector(form.getSector());
        return dto;
    }

    /** 카카오 OAuth2 attributes 에서 닉네임 추출. 없으면 null. */
    private static String resolveKakaoNickname(OAuth2User user) {
        if (user == null) return null;
        Object props = user.getAttributes().get("properties");
        if (props instanceof Map<?, ?> map) {
            Object nickname = map.get("nickname");
            return nickname != null && !nickname.toString().isBlank() ? nickname.toString() : null;
        }
        return null;
    }

    /** URL 경로의 service 이름을 StockServiceType에서 찾아 실제 값으로 치환. 없으면 null 반환. */
    private static String resolveServiceActual(String service) {
        StockServiceType type = StockServiceType.fromPath(service);
        return type != null ? type.getActualValue() : null;
    }
}

