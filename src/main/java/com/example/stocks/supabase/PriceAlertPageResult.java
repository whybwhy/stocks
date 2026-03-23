package com.example.stocks.supabase;

import com.example.stocks.alert.PriceAlertDto;

import java.util.List;

/** price_alerts 목록 조회 + 페이징 */
public record PriceAlertPageResult(List<PriceAlertDto> list, long totalCount) {
}
