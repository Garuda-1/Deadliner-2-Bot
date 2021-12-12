package ru.itmo.sd.deadliner2bot.utils.messages;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.itmo.sd.deadliner2bot.model.Chat;

@Component
public class MessageUtils {

    private final InputFile defaultResponse = new InputFile(
            "CAACAgIAAxkBAAMRYbJM3wABmFg_wMNH_vYufdrHebfXAAK4AAMTF8olDES8JhyUUCMjBA");


    public BotApiMethod<Message> createMessage(Chat chat, String response) {
        return createMessage(chat.getChatId(), response);
    }

    public BotApiMethod<Message> createMessage(long chatId, String response) {
        return SendMessage.builder()
                .chatId(Long.toString(chatId))
                .text(response)
                .build();
    }

    public SendSticker getSticker(long chatId) {
        return SendSticker.builder()
                .chatId(Long.toString(chatId))
                .sticker(defaultResponse)
                .build();
    }
}
