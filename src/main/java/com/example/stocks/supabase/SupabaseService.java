package com.example.stocks.supabase;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class SupabaseService {

    private final RestClient supabaseRestClient;

    public SupabaseService(RestClient supabaseRestClient) {
        this.supabaseRestClient = supabaseRestClient;
    }

    public String getTableRows(String table, String select) {
        return supabaseRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/" + table)
                        .queryParam("select", select)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    public List<StockDto> getStocks() {
        return supabaseRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/v1/stocks")
                        .queryParam("select", "*")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StockDto>>() {});
    }
}

