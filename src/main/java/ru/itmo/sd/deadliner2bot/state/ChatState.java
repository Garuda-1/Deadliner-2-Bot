package ru.itmo.sd.deadliner2bot.state;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;

import java.util.List;

public interface ChatState {

    List<BotApiMethod<?>> process(Chat chat, String message);

    ChatStateEnum getEnum();
}
