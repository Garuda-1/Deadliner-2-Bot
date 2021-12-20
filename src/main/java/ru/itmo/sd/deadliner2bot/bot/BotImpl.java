package ru.itmo.sd.deadliner2bot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.itmo.sd.deadliner2bot.service.ChatStateService;

import java.util.List;
import java.util.Locale;
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

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() == null) {
            log.debug("No message in update");
            return;
        }

        long chatId = update.getMessage().getChatId();
        String messageText = Objects.requireNonNullElse(update.getMessage().getText(), "").trim();
        Locale chatLocale = Locale.forLanguageTag(Objects.requireNonNullElse(
                update.getMessage().getFrom().getLanguageCode(), ""));
        log.info(update.getMessage().getChatId() + " sent: " + messageText);

        try {
            List<? extends PartialBotApiMethod<?>> response =
                    chatStateService.processMessage(chatId, messageText, chatLocale);
            for (PartialBotApiMethod<?> message : response) {
                if (message instanceof SendMessage) {
                    execute((SendMessage) message);
                } else if (message instanceof SendSticker) {
                    execute((SendSticker) message);
                } else {
                    log.debug("Unrecognized bot APU method: {}", message.toString());
                }
            }
        } catch (TelegramApiException e) {
            log.debug("Failed to respond\n" + e);
        }
    }

    @Override
    public void sendMarkdownMessage(BotApiMethod<Message> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.warn("Failed to send message:" + message.toString() + "\n", e);
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
