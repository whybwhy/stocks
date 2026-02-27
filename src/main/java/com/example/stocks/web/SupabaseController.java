package com.example.stocks.web;

import com.example.stocks.supabase.SupabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SupabaseController {

    private final SupabaseService supabaseService;

    public SupabaseController(SupabaseService supabaseService) {
        this.supabaseService = supabaseService;
    }

    @GetMapping("/api/supabase/rows")
    public String getRows(
            @RequestParam("table") String table,
            @RequestParam(name = "select", defaultValue = "*") String select
    ) {
        return supabaseService.getTableRows(table, select);
    }
}

