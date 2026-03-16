package com.example.stocks.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 텔레그램 Bot API로 메시지 전송.
 * POST + JSON body 사용으로 한글/이모지 등 UTF-8이 깨지지 않음.
 * 환경변수: TELEGRAM_BOT_TOKEN
 */
@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);
    private static final String TELEGRAM_API = "https://api.telegram.org";

    private final String botToken;
    private final RestClient restClient;

    public TelegramService(@Value("${telegram.bot-token:}") String botToken) {
        this.botToken = botToken;
        this.restClient = RestClient.builder().baseUrl(TELEGRAM_API).build();
    }

    public boolean isConfigured() {
        return botToken != null && !botToken.isBlank();
    }

    public void sendMessage(String chatId, String text) {
        if (!isConfigured()) {
            log.warn("Telegram bot token not configured. Message not sent: {}", text);
            return;
        }
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
