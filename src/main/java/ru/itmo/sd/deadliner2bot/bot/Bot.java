package ru.itmo.sd.deadliner2bot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.itmo.sd.deadliner2bot.service.ChatStateService;

@Component
@Slf4j
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {

    @Autowired
    private final ChatStateService chatStateService;

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private final InputFile defaultResponse = new InputFile(
            "CAACAgIAAxkBAAMRYbJM3wABmFg_wMNH_vYufdrHebfXAAK4AAMTF8olDES8JhyUUCMjBA");

    @Override
    public void onUpdateReceived(Update update) {
        long chatId;
        String messageText;

        if (update.getMessage() != null) {
            chatId = update.getMessage().getChatId();
            messageText = update.getMessage().getText();
        } else {
            chatId = update.getChannelPost().getChatId();
            messageText = update.getChannelPost().getText();
        }

        try {
            if (messageText.startsWith("/ping")) {
                execute(SendMessage.builder()
                        .chatId(Long.toString(chatId))
                        .text("pong")
                        .build());
            } else if (messageText.startsWith("/")) {
                String response = chatStateService.processMessage(chatId, messageText);
                if (response != null) {
                    execute(SendMessage.builder()
                            .chatId(Long.toString(chatId))
                            .text(response)
                            .build());
                }
            } else {
                execute(SendSticker.builder()
                        .chatId(Long.toString(chatId))
                        .sticker(defaultResponse)
                        .build());
            }
        } catch (TelegramApiException e) {
            log.debug("Failed to respond\n" + e);
        }
    }

    public void sendMessage(long chatId, String message) {
        try {
            execute(SendMessage.builder()
                    .chatId(Long.toString(chatId))
                    .text(message)
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
