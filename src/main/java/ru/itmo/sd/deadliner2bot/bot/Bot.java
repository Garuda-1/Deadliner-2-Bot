package ru.itmo.sd.deadliner2bot.bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

public interface Bot extends LongPollingBot {

    void sendMarkdownMessage(BotApiMethod<Message> message);
}
