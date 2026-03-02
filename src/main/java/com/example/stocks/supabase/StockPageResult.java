package com.example.stocks.supabase;

import java.util.List;

/** 목록 조회 + 페이징 결과 */
public record StockPageResult(List<StockDto> list, long totalCount) {
}
