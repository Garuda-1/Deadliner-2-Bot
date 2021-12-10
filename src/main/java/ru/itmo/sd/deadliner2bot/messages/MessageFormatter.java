package ru.itmo.sd.deadliner2bot.messages;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.dto.TodoDto;

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

    public String todoNotificationMessage(TodoDto todoDto) {
        StringJoiner lines = new StringJoiner("\n");
        lines.add(messageTemplatesProperties.getProperty("todo-notifications-header"));
        if (todoDto.getEndTime() != null) {
            lines.add(String.format(messageTemplatesProperties.getProperty("todo-notifications-todo-fmt-with-deadline"),
                    todoDto.getEndTime().toString(), todoDto.getName()));
        } else {
            lines.add(String.format(messageTemplatesProperties.getProperty("todo-notifications-todo-fmt"),
                    todoDto.getName()));
        }
        return lines.toString();
    }
}
