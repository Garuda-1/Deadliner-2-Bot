package ru.itmo.sd.deadliner2bot.ui.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommandInfo {

    private String command;
    private String description;

    public boolean testMessageForCommand(String message) {
        return message.startsWith(command);
    }
}
