package com.example.stocks.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * REST 폴링 방식 가격 알람 스케줄러.
 * kis.mode=rest 일 때만 활성화. (기본값: websocket → 비활성)
 * kis.rest-check-interval-seconds 간격으로 활성 알람 체크 후 조건 충족 시 텔레그램 발송.
 */
@Component
@ConditionalOnProperty(name = "kis.mode", havingValue = "rest")
public class AlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);

    private final AlertService alertService;

    public AlertScheduler(AlertService alertService,
                          @Value("${kis.rest-check-interval-seconds:30}") long intervalSeconds) {
        this.alertService = alertService;
        log.info("AlertScheduler (REST polling) activated, interval={}s", intervalSeconds);
    }

    @Scheduled(fixedDelayString = "${kis.rest-check-interval-seconds:30}000", initialDelay = 10_000)
    public void run() {
        try {
            alertService.checkAlerts();
        } catch (Exception e) {
            log.error("Alert check failed: {}", e.getMessage(), e);
        }
    }
}
