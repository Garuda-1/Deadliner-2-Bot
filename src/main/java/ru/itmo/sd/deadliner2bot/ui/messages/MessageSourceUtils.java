package ru.itmo.sd.deadliner2bot.ui.messages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageSourceUtils {

    private static final InputFile defaultResponse = new InputFile(
            "CAACAgIAAxkBAAIDFGHAXCKqjW3KHizAyAw6aN0q-wg7AAJrEgACvrphSeDky9qx74EOIwQ");
    private final ExposedResourceBundleMessageSource messageSource;

    public BotApiMethod<Message> createMarkdownMessage(Chat chat, String code, Object... args) {
        return createPlainMarkdownMessage(chat.getChatId(), messageSource.getMessage(code, args, chat.getLanguageCode()));
    }

    public BotApiMethod<Message> createMarkdownMessage(Chat chat, ChatStateEnum chatStateEnum, String code,
                                                       Object... args) {
        return createPlainMarkdownMessage(chat.getChatId(),
                messageSource.getMessage(chatStateCode(chatStateEnum, code), args, chat.getLanguageCode()));
    }

    public BotApiMethod<Message> createMarkdownMessage(long chatId, String code, Object... args) {
        return createPlainMarkdownMessage(chatId, messageSource.getMessage(code, args, Locale.ROOT));
    }

    public BotApiMethod<Message> createMarkdownMessage(long chatId, String code, Locale locale, Object... args) {
        return createPlainMarkdownMessage(chatId, messageSource.getMessage(code, args, locale));
    }

    public String getCommonProperty(String code, Object... args) {
        return getLocalizedProperty(code, Locale.ROOT, args);
    }

    public String getLocalizedProperty(Chat chat, String code, Object... args) {
        return getLocalizedProperty(code, chat.getLanguageCode(), args);
    }

    public String getLocalizedProperty(Chat chat, ChatStateEnum chatStateEnum, String code, Object... args) {
        return getLocalizedProperty(chatStateCode(chatStateEnum, code), chat.getLanguageCode(), args);
    }

    public String getLocalizedProperty(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    BotApiMethod<Message> createPlainMarkdownMessage(Chat chat, String text) {
        return createPlainMarkdownMessage(chat.getChatId(), text);
    }

    BotApiMethod<Message> createPlainMarkdownMessage(long chatId, String text) {
        return SendMessage.builder()
                .chatId(Long.toString(chatId))
                .text(text)
                .parseMode("markdown")
                .build();
    }

    String chatStateCode(ChatStateEnum chatStateEnum, String code) {
        return chatStateEnum + "." + code;
    }

    public SendSticker getSticker(Chat chat) {
        return SendSticker.builder()
                .chatId(Long.toString(chat.getChatId()))
                .sticker(defaultResponse)
                .build();
    }
}
