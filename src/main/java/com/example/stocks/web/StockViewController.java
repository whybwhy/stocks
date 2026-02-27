package com.example.stocks.web;

import com.example.stocks.supabase.SupabaseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StockViewController {

    private final SupabaseService supabaseService;

    public StockViewController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping("/stocks")
    public String stocks(Model model) {
        model.addAttribute("stocks", supabaseService.getStocks());
        return "stocks/list";
    }
}

