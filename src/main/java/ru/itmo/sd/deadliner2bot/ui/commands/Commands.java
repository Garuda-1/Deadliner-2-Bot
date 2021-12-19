package ru.itmo.sd.deadliner2bot.ui.commands;

import lombok.*;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageSourceUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Commands {

    private final MessageSourceUtils messageSourceUtils;

    public Map<String, CommandInfo> loadAllCommandsInfo(ChatStateEnum chatStateEnum, List<String> commandKeys) {
        Map<String, CommandInfo> result = new HashMap<>();
        commandKeys.forEach(commandKey ->
                result.put(commandKey, getCommandInfoForChatState(chatStateEnum, commandKey)));
        return result;
    }

    private CommandInfo getCommandInfoForChatState(ChatStateEnum chatStateEnum, String commandKey) {
        String prefix = chatStateEnum.toString() + "." + commandKey + ".";
        return CommandInfo.builder()
                .command(messageSourceUtils.getCommonProperty(prefix + "cmd"))
                .description(messageSourceUtils.getCommonProperty(prefix + "description"))
                .build();
    }

    @Getter
    @Setter
    @Builder
    public static class CommandInfo {

        private String command;
        private String description;

        public boolean testMessageForCommand(String message) {
            return message.startsWith(command);
        }
    }
}
