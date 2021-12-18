package ru.itmo.sd.deadliner2bot.ui.messages;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.model.Todo;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

@Component
public class MessageFormatter {

    private Properties templatesProperties;

    @PostConstruct
    public void postConstruct() throws IOException {
        Resource resource = new ClassPathResource("messages/message-templates.properties");
        templatesProperties = PropertiesLoaderUtils.loadProperties(resource);
    }

    public String notCompletedTodos(List<Todo> todos) {
        return notCompletedTodos(todos, templatesProperties.getProperty("daily-notification-header"), false);
    }

    public String notCompletedTodos(List<Todo> todos, @Nullable String header, boolean showIds) {
        StringJoiner lines = new StringJoiner("\n");
        if (header != null) {
            lines.add(header);
        }
        todos.stream()
                .sorted(Comparator.nullsLast(Comparator.comparing(Todo::getEndTime)))
                .forEachOrdered(todo -> {
                    String range = MessageFormat.format(templatesProperties.get("todo-range").toString(),
                            Objects.requireNonNullElse(todo.getStartTime(),
                                    templatesProperties.get("todo-default-start-time")),
                            Objects.requireNonNullElse(todo.getEndTime(),
                                    templatesProperties.get("todo-default-end-time")));
                    if (showIds) {
                        lines.add(MessageFormat.format(templatesProperties.getProperty("todo-format-with-ids"),
                                range, todo.getName(),
                                Objects.requireNonNullElse(todo.getDescription(),
                                        templatesProperties.get("todo-default-description")),
                                todo.getTodoId()));
                    } else {
                        lines.add(MessageFormat.format(templatesProperties.getProperty("todo-format"),
                                range, todo.getName(),
                                Objects.requireNonNullElse(todo.getDescription(),
                                        templatesProperties.get("todo-default-description"))));
                    }
                });
        return lines.toString();
    }

    public String todoNotificationMessage(Todo todo) {
        StringJoiner lines = new StringJoiner("\n");
        lines.add(templatesProperties.getProperty("todo-notifications-header"));
        if (todo.getEndTime() != null) {
            lines.add(String.format(templatesProperties.getProperty("todo-notifications-todo-fmt-with-deadline"),
                    todo.getEndTime().toString(), todo.getName()));
        } else {
            lines.add(String.format(templatesProperties.getProperty("todo-notifications-todo-fmt"),
                    todo.getName()));
        }
        return lines.toString();
    }

    public String todoView(Todo todo) {
        return MessageFormat.format(templatesProperties.getProperty("todo-view"),
                todo.getName(),
                Objects.requireNonNullElse(todo.getDescription(),
                        templatesProperties.getProperty("todo-default-description")),
                Objects.requireNonNullElse(todo.getStartTime(),
                        templatesProperties.getProperty("todo-default-start-time")),
                Objects.requireNonNullElse(todo.getEndTime(),
                        templatesProperties.getProperty("todo-default-end-time")),
                todo.isDailyNotificationsEnabled());
    }
}
