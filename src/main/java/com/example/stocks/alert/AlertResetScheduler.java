package com.example.stocks.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 08:00(KST)에 발송 완료된 알람(is_active=false)을 재활성화.
 */
@Component
public class AlertResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertResetScheduler.class);

    private final AlertService alertService;

    public AlertResetScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Seoul")
    public void resetTriggeredAlerts() {
        log.info("[AlertReset] 08:00 KST - triggered alerts 재활성화 실행");
        try {
            alertService.resetTriggeredAlerts();
        } catch (Exception e) {
            log.error("[AlertReset] 재활성화 실패: {}", e.getMessage(), e);
        }
    }
}
