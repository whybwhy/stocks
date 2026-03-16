package com.example.stocks.kis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

/**
 * 한국투자증권 실시간 WebSocket 메시지 핸들러.
 * 체결가(H0STCNT0) 수신 → 종목코드 + 현재가를 콜백으로 전달.
 *
 * KIS 실시간 데이터 포맷:
 *   [암호화]|[TR_ID]|[건수]|[데이터(^구분)]
 *   예: 0|H0STCNT0|001|005930^131308^186500^...
 *
 * 헤더: | (파이프) 구분
 * 데이터: ^ (캐럿) 구분
 */
public class KisWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(KisWebSocketHandler.class);

    private final BiConsumer<String, BigDecimal> priceCallback;

    public KisWebSocketHandler(BiConsumer<String, BigDecimal> priceCallback) {
        this.priceCallback = priceCallback;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("[KIS WS] 연결 성공 sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.info("[KIS WS] 수신 raw (길이={}) : {}", payload.length(), payload.substring(0, Math.min(500, payload.length())));

        if (payload.startsWith("{")) {
            return;
        }

        try {
            // 헤더: | 로 분리 → [0]=암호화, [1]=TR_ID, [2]=건수, [3]=데이터(^구분)
            String[] header = payload.split("\\|", 4);
            if (header.length < 4) {
                log.warn("[KIS WS] 헤더 파싱 불가 - 파트 수={}", header.length);
                return;
            }

            String trId = header[1];
            int dataCount = Integer.parseInt(header[2]);

            if (!KisApiConstants.WS_TR_ID_EXEC.equals(trId)) {
                log.info("[KIS WS] 다른 TR 수신: {}", trId);
                return;
            }

            // 데이터: ^ 로 분리 (건수가 여러 건일 수 있음)
            String[] fields = header[3].split("\\^");
            // 체결가 1건당 필드 수 (약 46개), fields[0]=종목코드, fields[2]=현재가
            int fieldsPerRecord = (dataCount > 0 && fields.length > 0) ? fields.length / dataCount : fields.length;

            for (int i = 0; i < dataCount; i++) {
                int offset = i * fieldsPerRecord;
                if (offset + 2 >= fields.length) break;

                String stockCode = fields[offset];
                String priceStr = fields[offset + 2].replace(",", "");
                BigDecimal price = new BigDecimal(priceStr);

                log.info("[KIS WS] 체결 수신: {} = {} (건 {}/{})", stockCode, price, i + 1, dataCount);
                priceCallback.accept(stockCode, price);
            }
        } catch (Exception e) {
            log.warn("[KIS WS] 메시지 파싱 실패: {}", e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("[KIS WS] 전송 오류: {}", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("[KIS WS] 연결 종료: status={}", status);
    }
}
