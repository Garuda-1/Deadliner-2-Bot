package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.*;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.TodoNotificationService;
import ru.itmo.sd.deadliner2bot.service.TodoService;
import ru.itmo.sd.deadliner2bot.ui.commands.Commands;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageSourceUtils;
import ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils.dateTimeFormat;

@Component
@RequiredArgsConstructor
public class AddTodoNotificationState implements ChatState {

    private static final ChatStateEnum chatStateEnum = ChatStateEnum.ADD_TODO_NOTIFICATION_STATE;

    private final ChatRepository chatRepository;
    private final MessageSourceUtils messageSourceUtils;
    private final DateTimeUtils dateTimeUtils;
    private final TodoService todoService;
    private final TodoNotificationService todoNotificationService;
    private final Commands commands;
    //    private final StateMessages stateMessages;
    private Map<String, Commands.CommandInfo> commandsInfo;

    @PostConstruct
    public void postConstruct() {
        commandsInfo = commands.loadAllCommandsInfo(getChatStateEnum(), List.of(
                "cancel"
        ));
    }

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        Optional<Todo> todo = todoService.findSelectedTodoByChatId(chat.getChatId());
        if (commandsInfo.get("cancel").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.EDIT_TODO_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "cancel"));
        } else if (message.startsWith("/")) {
            return null;
        }

        if (todo.isEmpty()) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "no-todo-selected"));
        }

        LocalDateTime dateTime = dateTimeUtils.parseDateTime(message);
        if (dateTime == null) {
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "invalid-date-time-format",
                    dateTimeFormat));
        }
        chat.setState(ChatStateEnum.EDIT_TODO_STATE);
        chatRepository.save(chat);
        TodoNotification todoNotification = new TodoNotification();
        todoNotification.setNotificationTime(dateTime);
        todoNotification.setTodo(todo.get());
        todoNotificationService.save(todoNotification);
        return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "todo-notification-time-set",
                dateTime));
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
