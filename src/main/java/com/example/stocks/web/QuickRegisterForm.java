package com.example.stocks.web;

import java.util.ArrayList;
import java.util.List;

/**
 * 간편등록 폼: 여러 행(종목, 타입, 섹터, 설명) + 사용자명.
 */
public class QuickRegisterForm {

    private List<QuickRegisterRow> rows = new ArrayList<>();
    private String account;

    public List<QuickRegisterRow> getRows() {
        return rows;
    }

    public void setRows(List<QuickRegisterRow> rows) {
        this.rows = rows != null ? rows : new ArrayList<>();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
