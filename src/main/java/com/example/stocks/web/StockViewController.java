package com.example.stocks.web;

import com.example.stocks.supabase.SupabaseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class StockViewController {

    private final SupabaseService supabaseService;

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

    @GetMapping("/stocks")
    public String stocks(Model model) {
        model.addAttribute("stocks", supabaseService.getStocks());
        return "stocks/list";
    }
}

