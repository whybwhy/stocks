package com.example.stocks.holding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 보유종목 매수가 대비 +5%/+10% 수익 알림 스케줄러.
 * 15분 간격, 하루에 각 퍼센트별 한 번만 발송. 다음 날 자동 리셋.
 * kis.mode=rest 일 때만 활성화.
 */
@Component
@ConditionalOnProperty(name = "kis.mode", havingValue = "rest")
public class HoldingGainScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoldingGainScheduler.class);

    private final HoldingService holdingService;

    public HoldingGainScheduler(HoldingService holdingService) {
        this.holdingService = holdingService;
        log.info("HoldingGainScheduler activated, interval=15min");
    }

    @Scheduled(fixedDelay = 900_000, initialDelay = 20_000)
    public void run() {
        try {
            holdingService.checkDailyGains();
        } catch (Exception e) {
            log.error("Daily gain check failed: {}", e.getMessage(), e);
        }
    }
}
