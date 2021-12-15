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
    private Properties helpProperties;

    @PostConstruct
    public void postConstruct() throws IOException {
        Resource commandsPropertiesResource = new ClassPathResource("messages/state-commands.properties");
        commandsProperties = PropertiesLoaderUtils.loadProperties(commandsPropertiesResource);
        Resource helpPropertiesResource = new ClassPathResource("messages/help.properties");
        helpProperties = PropertiesLoaderUtils.loadProperties(helpPropertiesResource);
        Set<String> commandsWithCmd = new HashSet<>();
        Set<String> commandsWithDescription = new HashSet<>();
        for (Object key : commandsProperties.keySet()) {
            String keyString = key.toString();
            String[] keyParts = key.toString().split("\\.");
            if (keyParts.length != 3) {
                throw new RuntimeException("Invalid command property key: " + keyString);
            }
            try {
                ChatStateEnum.valueOf(keyParts[0]);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Unknown state of command: " + keyString);
            }
            if ("cmd".equals(keyParts[2])) {
                commandsWithCmd.add(keyParts[1]);
            } else if ("description".equals(keyParts[2])) {
                commandsWithDescription.add(keyParts[1]);
            } else {
                throw new RuntimeException("Unexpected property: " + keyString);
            }
        }
        if (commandsWithCmd.size() != commandsWithDescription.size() ||
                !commandsWithCmd.containsAll(commandsWithDescription)) {
            throw new RuntimeException("Some commands are missing '.cmd' or '.description' properties");
        }
    }

    public Map<String, CommandInfo> loadAllCommandsInfo(ChatStateEnum chatStateEnum, List<String> commandKeys) {
        Map<String, CommandInfo> result = new HashMap<>();
        commandKeys.forEach(commandKey ->
                result.put(commandKey, getCommandInfoForChatState(chatStateEnum, commandKey)));
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
        String helpFormat = helpProperties.getProperty("help-format");
        return IntStream.range(0, commands.size())
                .mapToObj(i -> MessageFormat.format(helpFormat, commands.get(i), descriptions.get(i)))
                .collect(Collectors.joining("\n"));
    }
}
