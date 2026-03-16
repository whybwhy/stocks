package com.example.stocks.web;

import com.example.stocks.supabase.SupabaseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Map;

@Controller
public class MainController {

    private final SupabaseService supabaseService;

    public MainController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping("/main")
    public String main(Principal principal,
                      @AuthenticationPrincipal OAuth2User user,
                      Model model) {
        model.addAttribute("showLogout", principal != null);
        String nickname = resolveKakaoNickname(user);
        boolean showChartboyLink = supabaseService.hasAccess(nickname, "chartboy");
        model.addAttribute("showChartboyLink", showChartboyLink);
        model.addAttribute("userNickname", nickname);
        return "main";
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
