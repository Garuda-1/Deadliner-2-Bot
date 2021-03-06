package ru.itmo.sd.deadliner2bot.ui.messages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils;

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
    private final DateTimeUtils dateTimeUtils;

    public BotApiMethod<Message> stateHelpMessage(Chat chat, ChatStateEnum chatStateEnum) {
        List<String> commands = findStatePropertiesWithPrefix(chat, chatStateEnum, ".cmd");
        List<String> descriptions = findStatePropertiesWithPrefix(chat, chatStateEnum, ".description");
        return messageSourceUtils.createPlainMarkdownMessage(chat, IntStream.range(0, commands.size())
                .mapToObj(i -> messageSourceUtils.getCommonProperty("help-format", commands.get(i),
                        descriptions.get(i)))
                .collect(Collectors.joining("\n")));
    }

    public BotApiMethod<Message> notCompletedTodosMessage(Chat chat, List<Todo> todos, ChatStateEnum chatStateEnum,
                                                          String headerCode, boolean showIds) {
        return notCompletedTodosMessage(chat, todos, messageSourceUtils.chatStateCode(chatStateEnum, headerCode),
                showIds);
    }

    public BotApiMethod<Message> notCompletedTodosMessage(Chat chat, List<Todo> todos, String headerCode,
                                                          boolean showIds) {
        StringJoiner lines = new StringJoiner("\n");
        lines.add(messageSourceUtils.getLocalizedProperty(chat, headerCode));
        todos.stream()
                .sorted(Comparator.comparing(Todo::getEndTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEachOrdered(todo -> lines.add(formatTodo(chat, todo, showIds)));
        return messageSourceUtils.createPlainMarkdownMessage(chat, lines.toString());
    }

    public BotApiMethod<Message> todoViewMessage(Chat chat, Todo todo) {
        String todoView = MessageFormat.format(messageSourceUtils.getLocalizedProperty(chat, "todo-view"),
                todo.getName(),
                Objects.requireNonNullElse(todo.getDescription(),
                        messageSourceUtils.getLocalizedProperty(chat, "todo-default-description")),
                Objects.requireNonNullElse(dateTimeUtils.formatDateTime(chat, todo.getStartTime()),
                        messageSourceUtils.getLocalizedProperty(chat, "todo-default-start-time")),
                Objects.requireNonNullElse(dateTimeUtils.formatDateTime(chat, todo.getEndTime()),
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

    private List<String> findStatePropertiesWithPrefix(Chat chat, ChatStateEnum chatStateEnum, String prefix) {
        return messageSource
                .getCommonPropertiesCodes(c -> c.startsWith(chatStateEnum + ".") && c.endsWith(prefix))
                .stream()
                .sorted()
                .map(c -> messageSourceUtils.getLocalizedProperty(chat, c))
                .map(this::escape)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String formatTodo(Chat chat, Todo todo, boolean showIds) {
        String range = messageSourceUtils.getLocalizedProperty(chat, "todo-range",
                Objects.requireNonNullElse(dateTimeUtils.formatDateTime(chat, todo.getStartTime()),
                        messageSourceUtils.getLocalizedProperty(chat, "todo-default-start-time")),
                Objects.requireNonNullElse(dateTimeUtils.formatDateTime(chat, todo.getEndTime()),
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
