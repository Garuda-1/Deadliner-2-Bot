package ru.itmo.sd.deadliner2bot.utils.messages;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.dto.TodoDto;
import ru.itmo.sd.deadliner2bot.model.Todo;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Component
public class MessageFormatter {

    private Properties messageTemplatesProperties;

    @PostConstruct
    public void postConstruct() throws IOException {
        Resource resource = new ClassPathResource("message-templates/message-templates.properties");
        messageTemplatesProperties = PropertiesLoaderUtils.loadProperties(resource);
    }

    public String dailyNotificationMessage(List<TodoDto> todoDtos) {
        StringJoiner lines = new StringJoiner("\n");
        lines.add(messageTemplatesProperties.getProperty("daily-notifications-header"));
        todoDtos.stream()
                .sorted(Comparator.nullsLast(Comparator.comparing(TodoDto::getEndTime)))
                .forEachOrdered(todoDto -> lines.add(String.format(
                        messageTemplatesProperties.getProperty("daily-notifications-todo-fmt"),
                        todoDto.getName())));
        return lines.toString();
    }

    public String todoNotificationMessage(Todo todo) {
        StringJoiner lines = new StringJoiner("\n");
        lines.add(messageTemplatesProperties.getProperty("todo-notifications-header"));
        if (todo.getEndTime() != null) {
            lines.add(String.format(messageTemplatesProperties.getProperty("todo-notifications-todo-fmt-with-deadline"),
                    todo.getEndTime().toString(), todo.getName()));
        } else {
            lines.add(String.format(messageTemplatesProperties.getProperty("todo-notifications-todo-fmt"),
                    todo.getName()));
        }
        return lines.toString();
    }
}
