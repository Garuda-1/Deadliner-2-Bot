package ru.itmo.sd.deadliner2bot.state;

import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;

public interface ChatState {

    String process(Chat chat, String message);

    ChatStateEnum getEnum();
}
