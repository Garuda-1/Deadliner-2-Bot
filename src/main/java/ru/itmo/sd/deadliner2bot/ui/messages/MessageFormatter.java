package ru.itmo.sd.deadliner2bot.ui.messages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class MessageFormatter {

    private static final Set<Character> markdownEscapeCharacters = Set.of(
            '_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!'
    );

    private final ExposedResourceBundleMessageSource messageSource;
    private final MessageSourceUtils messageSourceUtils;

    public BotApiMethod<Message> stateHelpMessage(Chat chat, ChatStateEnum chatStateEnum) {
        List<String> commands = messageSource
                .getCommonPropertiesCodes(c -> c.startsWith(chatStateEnum + ".") && c.endsWith(".cmd"))
                .stream()
                .sorted()
                .map(messageSourceUtils::getCommonProperty)
                .map(this::escape)
                .collect(Collectors.toCollection(ArrayList::new));
        List<String> descriptions = messageSource
                .getCommonPropertiesCodes(c -> c.startsWith(chatStateEnum + ".") && c.endsWith(".description"))
                .stream()
                .sorted()
                .map(messageSourceUtils::getCommonProperty)
                .map(this::escape)
                .collect(Collectors.toCollection(ArrayList::new));
        return messageSourceUtils.createPlainMarkdownMessage(chat, IntStream.range(0, commands.size())
                .mapToObj(i -> messageSourceUtils.getCommonProperty("help-format", commands.get(i), descriptions.get(i)))
                .collect(Collectors.joining("\n")));
    }

    public BotApiMethod<Message> notCompletedTodosMessage(Chat chat, List<Todo> todos, ChatStateEnum chatStateEnum,
                                                          String headerCode, boolean showIds) {
        return notCompletedTodosMessage(chat, todos, messageSourceUtils.chatStateCode(chatStateEnum, headerCode), showIds);
    }

    public BotApiMethod<Message> notCompletedTodosMessage(Chat chat, List<Todo> todos, String headerCode, boolean showIds) {
        StringJoiner lines = new StringJoiner("\n");
        lines.add(messageSourceUtils.getLocalizedProperty(chat, headerCode));
        todos.stream()
                .sorted(Comparator.nullsLast(Comparator.comparing(Todo::getEndTime)))
                .forEachOrdered(todo -> lines.add(formatTodo(chat, todo, showIds)));
        return messageSourceUtils.createPlainMarkdownMessage(chat, lines.toString());
    }

    public BotApiMethod<Message> todoViewMessage(Chat chat, Todo todo) {
        String todoView = MessageFormat.format(messageSourceUtils.getLocalizedProperty(chat, "todo-view"),
                todo.getName(),
                Objects.requireNonNullElse(todo.getDescription(),
                        messageSourceUtils.getLocalizedProperty(chat, "todo-default-description")),
                Objects.requireNonNullElse(todo.getStartTime(),
                        messageSourceUtils.getLocalizedProperty(chat, "todo-default-start-time")),
                Objects.requireNonNullElse(todo.getEndTime(),
                        messageSourceUtils.getLocalizedProperty(chat, "todo-default-end-time")),
                todo.isDailyNotificationsEnabled());
        return messageSourceUtils.createPlainMarkdownMessage(chat, todoView);
    }

    public BotApiMethod<Message> todoNotificationMessage(Chat chat, Todo todo) {
        StringJoiner lines = new StringJoiner("\n");
        lines.add(messageSourceUtils.getLocalizedProperty(chat, "todo-notifications-header"));
        lines.add(formatTodo(chat, todo, false));
        return messageSourceUtils.createPlainMarkdownMessage(chat, lines.toString());
    }

    private String formatTodo(Chat chat, Todo todo, boolean showIds) {
        String range = messageSourceUtils.getLocalizedProperty(chat, "todo-range",
                Objects.requireNonNullElse(todo.getStartTime(),
                        messageSourceUtils.getLocalizedProperty(chat, "todo-default-start-time")),
                Objects.requireNonNullElse(todo.getEndTime(),
                        messageSourceUtils.getLocalizedProperty(chat, "todo-default-end-time")));
        if (showIds) {
            return messageSourceUtils.getLocalizedProperty(chat, "todo-format-with-ids",
                    range, todo.getName(),
                    Objects.requireNonNullElse(todo.getDescription(),
                            messageSourceUtils.getLocalizedProperty(chat, "todo-default-description")),
                    todo.getTodoId());
        } else {
            return messageSourceUtils.getLocalizedProperty(chat, "todo-format",
                    range, todo.getName(),
                    Objects.requireNonNullElse(todo.getDescription(),
                            messageSourceUtils.getLocalizedProperty(chat, "todo-default-description")));
        }
    }

    private String escape(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (markdownEscapeCharacters.contains(c)) {
                stringBuilder.append('\\');
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}
