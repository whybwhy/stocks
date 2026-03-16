package com.example.stocks.supabase;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * service_permission 테이블 행.
 * 조회: service 기준 허용 닉네임 목록, nickname 기준 허용 서비스 목록.
 */
public class ServicePermissionDto {

    private String nickname;
    private String service;

    @JsonProperty("created_at")
    private String createdAt;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
