package com.example.stocks.web;

import com.example.stocks.config.AppProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    private final AppProperties appProperties;

    public MainController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/main")
    public String main(Principal principal,
                      @AuthenticationPrincipal OAuth2User user,
                      Model model) {
        model.addAttribute("showLogout", principal != null);
        List<String> allowed = appProperties.getAllowedKakaoAccounts();
        String nickname = resolveKakaoNickname(user);
        boolean showChartboyLink = allowed.isEmpty()
                || (nickname != null && allowed.stream().anyMatch(a -> a != null && a.trim().equalsIgnoreCase(nickname.trim())));
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
