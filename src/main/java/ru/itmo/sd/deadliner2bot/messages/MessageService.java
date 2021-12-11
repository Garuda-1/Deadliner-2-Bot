package ru.itmo.sd.deadliner2bot.messages;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.itmo.sd.deadliner2bot.model.Chat;

@Service
public class MessageService {

    public BotApiMethod<Message> createMessage(Chat chat, String response) {
        return createMessage(chat.getChatId(), response);
    }

    public BotApiMethod<Message> createMessage(long chatId, String response) {
        return SendMessage.builder()
                .chatId(Long.toString(chatId))
                .text(response)
                .build();
    }


}
