package com.example.stocks.kis;

import com.example.stocks.alert.AlertService;
import com.example.stocks.alert.PriceAlertDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 한국투자증권 WebSocket 실시간 체결가 클라이언트.
 * kis.mode=websocket (기본값) 일 때만 활성화.
 *
 * 장 시간(08:50~15:35)에 WebSocket 연결 후 알람 대상 종목을 구독.
 * 체결가 수신 시 AlertService를 통해 목표가 도달 여부 체크.
 */
@Component
@ConditionalOnProperty(name = "kis.mode", havingValue = "websocket", matchIfMissing = true)
public class KisWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(KisWebSocketClient.class);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final KisApiProperties properties;
    private final KisWebSocketApprovalService approvalService;
    private final AlertService alertService;

    private volatile WebSocketSession session;
    private final Set<String> subscribedCodes = ConcurrentHashMap.newKeySet();

    public KisWebSocketClient(KisApiProperties properties,
                              KisWebSocketApprovalService approvalService,
                              AlertService alertService) {
        this.properties = properties;
        this.approvalService = approvalService;
        this.alertService = alertService;
        log.info("[KIS WS] WebSocket 클라이언트 활성화 (kis.mode=websocket)");
    }

    /**
     * 60초마다 실행: 장 시간이면 연결/구독 관리, 장 종료 후 연결 해제.
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 5_000)
    public void manage() {
        if (!properties.isConfigured()) {
            log.debug("[KIS WS] KIS 미설정 → 스킵");
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(KST);
        boolean marketOpen = isMarketTime(now);
        log.info("[KIS WS] manage() 시간={} 장중={} 연결={} 구독={}",
                now.toLocalTime(), marketOpen,
                session != null && session.isOpen(),
                subscribedCodes);

        if (marketOpen) {
            ensureConnected();
            refreshSubscriptions();
        } else {
            if (session != null && session.isOpen()) {
                disconnect();
                log.info("[KIS WS] 장 종료 → 연결 해제");
            }
        }
    }

    private void ensureConnected() {
        if (session != null && session.isOpen()) {
            log.info("[KIS WS] 이미 연결됨, 스킵");
            return;
        }

        log.info("[KIS WS] approval_key 요청 중...");
        String approvalKey = approvalService.getApprovalKey();
        if (approvalKey == null) {
            log.warn("[KIS WS] approval_key 없음, 연결 불가");
            return;
        }
        log.info("[KIS WS] approval_key 획득 완료 (길이={})", approvalKey.length());

        String wsUrl = resolveWebSocketUrl();
        log.info("[KIS WS] 연결 시도: {}", wsUrl);
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            KisWebSocketHandler handler = new KisWebSocketHandler(this::onPriceReceived);
            session = client.execute(handler, wsUrl).get();
            subscribedCodes.clear();
            log.info("[KIS WS] 연결 성공: {} sessionOpen={}", wsUrl, session != null && session.isOpen());
        } catch (Exception e) {
            log.error("[KIS WS] 연결 실패: {} ({})", e.getMessage(), e.getClass().getSimpleName(), e);
            session = null;
        }
    }

    private void refreshSubscriptions() {
        if (session == null || !session.isOpen()) {
            log.warn("[KIS WS] refreshSubscriptions 스킵 - 세션 없음");
            return;
        }

        List<PriceAlertDto> alerts = alertService.getActiveAlerts();
        Set<String> needed = alerts.stream()
                .map(PriceAlertDto::getStockCode)
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.toSet());
        log.info("[KIS WS] 활성 알람 {}건, 필요 종목={}, 현재 구독={}", alerts.size(), needed, subscribedCodes);

        String approvalKey = approvalService.getApprovalKey();
        if (approvalKey == null) return;

        for (String code : needed) {
            if (!subscribedCodes.contains(code)) {
                subscribe(approvalKey, code);
            }
        }

        for (String code : new ArrayList<>(subscribedCodes)) {
            if (!needed.contains(code)) {
                unsubscribe(approvalKey, code);
            }
        }
    }

    private void subscribe(String approvalKey, String stockCode) {
        String msg = buildSubscribeMessage(approvalKey, stockCode, "1");
        sendMessage(msg);
        subscribedCodes.add(stockCode);
        log.info("[KIS WS] 구독: {}", stockCode);
    }

    private void unsubscribe(String approvalKey, String stockCode) {
        String msg = buildSubscribeMessage(approvalKey, stockCode, "2");
        sendMessage(msg);
        subscribedCodes.remove(stockCode);
        log.info("[KIS WS] 구독해제: {}", stockCode);
    }

    private String buildSubscribeMessage(String approvalKey, String stockCode, String trType) {
        return """
                {"header":{"approval_key":"%s","custtype":"P","tr_type":"%s","content-type":"utf-8"},\
                "body":{"input":{"tr_id":"%s","tr_key":"%s"}}}"""
                .formatted(approvalKey, trType, KisApiConstants.WS_TR_ID_EXEC, stockCode);
    }

    private void sendMessage(String msg) {
        try {
            if (session != null && session.isOpen()) {
                log.info("[KIS WS] 송신: {}", msg.substring(0, Math.min(200, msg.length())));
                session.sendMessage(new TextMessage(msg));
                log.info("[KIS WS] 송신 완료");
            } else {
                log.warn("[KIS WS] 송신 불가 - 세션 닫힘");
            }
        } catch (Exception e) {
            log.error("[KIS WS] 메시지 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 체결가 수신 콜백. AlertService의 알람 체크 로직 호출.
     */
    private void onPriceReceived(String stockCode, BigDecimal price) {
        log.info("[KIS WS] 콜백 → 알람체크 stockCode={} price={}", stockCode, price);
        alertService.checkSingleAlert(stockCode, price);
    }

    private void disconnect() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            log.warn("[KIS WS] 연결 해제 오류: {}", e.getMessage());
        } finally {
            session = null;
            subscribedCodes.clear();
        }
    }

    @PreDestroy
    public void destroy() {
        disconnect();
    }

    private String resolveWebSocketUrl() {
        if (properties.getBaseUrl().contains("openapivts")) {
            return KisApiConstants.WS_MOCK_URL;
        }
        return KisApiConstants.WS_REAL_URL;
    }

    private static boolean isMarketTime(ZonedDateTime now) {
        DayOfWeek dow = now.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return false;
        LocalTime t = now.toLocalTime();
        return !t.isBefore(LocalTime.of(8, 50)) && !t.isAfter(LocalTime.of(15, 35));
    }
}
