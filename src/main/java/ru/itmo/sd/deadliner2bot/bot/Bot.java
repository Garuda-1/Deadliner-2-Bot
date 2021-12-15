package ru.itmo.sd.deadliner2bot.bot;

import org.telegram.telegrambots.meta.generics.LongPollingBot;

public interface Bot extends LongPollingBot {

    void sendMarkdownMessage(long chatId, String message);
}
