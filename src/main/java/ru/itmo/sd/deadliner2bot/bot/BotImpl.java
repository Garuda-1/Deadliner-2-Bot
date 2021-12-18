package ru.itmo.sd.deadliner2bot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.itmo.sd.deadliner2bot.service.ChatStateService;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class BotImpl extends TelegramLongPollingBot implements Bot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private final ChatStateService chatStateService;
    private final MessageUtils messageUtils;

    @Override
    public void onUpdateReceived(Update update) {
        long chatId;
        String messageText;

        if (update.getMessage() == null) {
            log.debug("No message in update");
            return;
        }
        chatId = update.getMessage().getChatId();
        messageText = Objects.requireNonNullElse(update.getMessage().getText(), "").trim();
        log.info(update.getMessage().getChatId() + " sent: " + messageText);

        try {
            List<BotApiMethod<?>> response = chatStateService.processMessage(chatId, messageText);
            if (response != null && !response.isEmpty()) {
                for (BotApiMethod<?> message : response) {
                    execute(message);
                }
            } else {
                execute(messageUtils.createMessage(chatId, "Message is not recognised"));
                execute(messageUtils.getSticker(chatId));
            }
        } catch (TelegramApiException e) {
            log.debug("Failed to respond\n" + e);
        }
    }

    @Override
    public void sendMarkdownMessage(long chatId, String message) {
        try {
            execute(SendMessage.builder()
                    .chatId(Long.toString(chatId))
                    .text(message)
                    .parseMode("markdown")
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Failed to send message to chat_id = " + chatId + "\n", e);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
