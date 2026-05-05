package com.example.stocks.supabase;

import com.example.stocks.alert.PriceAlertTriggerDto;

import java.util.List;

/** {@code price_alert_triggers} 목록 + 페이징 */
public record PriceAlertTriggerPageResult(List<PriceAlertTriggerDto> list, long totalCount) {
}
