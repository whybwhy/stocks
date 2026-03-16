package com.example.stocks.alert;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 가격 알람 관리 REST API.
 * POST /api/alerts — 알람 생성
 * GET  /api/alerts — 전체 조회
 * DELETE /api/alerts/{id} — 삭제
 * POST /api/alerts/check — 수동 체크 트리거
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<PriceAlertDto> list() {
        return alertService.getAllAlerts();
    }

    @PostMapping
    public PriceAlertDto create(@RequestBody PriceAlertDto dto) {
        return alertService.createAlert(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, String>> manualCheck() {
        alertService.checkAlerts();
        return ResponseEntity.ok(Map.of("status", "checked"));
    }
}
