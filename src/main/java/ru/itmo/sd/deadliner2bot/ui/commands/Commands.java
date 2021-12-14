package ru.itmo.sd.deadliner2bot.ui.commands;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class Commands {

    private Properties commandsProperties;

    @PostConstruct
    public void postConstruct() throws IOException {
        Resource resource = new ClassPathResource("messages/state-commands.properties");
        commandsProperties = PropertiesLoaderUtils.loadProperties(resource);
    }

    public Map<String, CommandInfo> loadAllCommandsInfo(ChatStateEnum chatStateEnum, List<String> commandKeys) {
        Map<String, CommandInfo> result = new HashMap<>();
        commandKeys.forEach(commandKey -> result.put(commandKey, getCommandInfoForChatState(chatStateEnum, commandKey)));
        return result;
    }

    private CommandInfo getCommandInfoForChatState(ChatStateEnum chatStateEnum, String commandKey) {
        String prefix = chatStateEnum.toString() + "." + commandKey + ".";
        return CommandInfo.builder()
                .command(commandsProperties.getProperty(prefix + "cmd"))
                .description(commandsProperties.getProperty(prefix + "description"))
                .build();
    }

    public String formHelpMessage(ChatStateEnum chatStateEnum) {
        List<String> commands = commandsProperties.keySet().stream()
                .filter(k -> k.toString().startsWith(chatStateEnum + ".") && k.toString().endsWith(".cmd"))
                .sorted()
                .map(k -> commandsProperties.get(k).toString())
                .collect(Collectors.toCollection(ArrayList::new));
        List<String> descriptions = commandsProperties.keySet().stream()
                .filter(k -> k.toString().startsWith(chatStateEnum + ".") && k.toString().endsWith(".description"))
                .sorted()
                .map(k -> commandsProperties.get(k).toString())
                .collect(Collectors.toCollection(ArrayList::new));
        String helpFormat = commandsProperties.getProperty("help-format");
        return IntStream.range(0, commands.size())
                .mapToObj(i -> MessageFormat.format(helpFormat, commands.get(i), descriptions.get(i)))
                .collect(Collectors.joining("\n"));
    }
}
