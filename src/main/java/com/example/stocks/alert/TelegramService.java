package com.example.stocks.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 텔레그램 Bot API로 메시지 전송.
 * application.yml 의 telegram.chat-ids 목록에 있는 모든 채팅방에 발송.
 */
@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);
    private static final String TELEGRAM_API = "https://api.telegram.org";

    private final String botToken;
    private final List<String> chatIds;
    private final RestClient restClient;

    public TelegramService(@Value("${telegram.bot-token:}") String botToken,
                           @Value("${telegram.chat-ids:}") List<String> chatIds) {
        this.botToken = botToken;
        this.chatIds = chatIds != null ? chatIds : List.of();
        this.restClient = RestClient.builder().baseUrl(TELEGRAM_API).build();
        log.info("[텔레그램] 수신 대상 chat-ids={}", this.chatIds);
    }

    public boolean isConfigured() {
        return botToken != null && !botToken.isBlank() && !chatIds.isEmpty();
    }

    /**
     * 설정된 모든 chat-ids 에 메시지 발송.
     */
    public void broadcast(String text) {
        if (!isConfigured()) {
            log.warn("Telegram not configured (token or chat-ids empty). Message not sent.");
            return;
        }
        for (String chatId : chatIds) {
            if (chatId == null || chatId.isBlank()) continue;
            sendMessage(chatId.trim(), text);
        }
    }

    private void sendMessage(String chatId, String text) {
        try {
            log.info("[텔레그램 통신] request chat_id={} uri=/bot***/sendMessage", chatId);
            restClient.post()
                    .uri("/bot" + botToken + "/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "chat_id", chatId,
                            "text", text,
                            "parse_mode", "HTML"
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("[텔레그램 통신] response ok chat_id={} preview={}", chatId, text.substring(0, Math.min(80, text.length())));
        } catch (Exception e) {
            log.error("Telegram send failed to {}: {}", chatId, e.getMessage());
        }
    }
}
