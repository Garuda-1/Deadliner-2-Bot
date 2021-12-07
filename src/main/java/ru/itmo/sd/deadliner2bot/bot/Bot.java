package ru.itmo.sd.deadliner2bot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private final InputFile defaultResponse = new InputFile(
            "CAACAgIAAxkBAAMcYa8znOHiYPmD3vEwdk7xgmNZ7gUAAqcAAxgdBg_zfF33TbN4HyIE");

    @Override
    public void onUpdateReceived(Update update) {
        String chatId;
        String messageText;

        if (update.getMessage() != null) {
            chatId = update.getMessage().getChatId().toString();
            messageText = update.getMessage().getText();
        } else {
            chatId = update.getChannelPost().getChatId().toString();
            messageText = update.getChannelPost().getText();
        }

        try {
            if (messageText.startsWith("/ping")) {
                execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("pong")
                        .build());
            } else {
                execute(SendSticker.builder()
                        .chatId(chatId)
                        .sticker(defaultResponse)
                        .build());
            }
        } catch (TelegramApiException e) {
            log.debug("Failed to respond\n" + e);
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
