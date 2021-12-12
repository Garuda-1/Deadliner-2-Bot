package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.model.TodoNotification;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoNotificationRepository;
import ru.itmo.sd.deadliner2bot.service.TodoService;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.itmo.sd.deadliner2bot.utils.DateTimeUtils.dateTimeFormat;
import static ru.itmo.sd.deadliner2bot.utils.DateTimeUtils.parseDateTime;

@Component
@RequiredArgsConstructor
public class AddTodoNotificationState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.ADD_TODO_NOTIFICATION;
    private final MessageUtils messageUtils;
    private final TodoService todoService;
    //Todo: change to service
    private final TodoNotificationRepository repository;

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        Optional<Todo> todo = todoService.findSelectedTodoByChatId(chat.getChatId());
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            if (todo.isPresent()) {
                todo.get().setSelected(false);
                todoService.save(todo.get());
            }
            return List.of(messageUtils.createMessage(chat, "Operation canceled."));
        } else {
            if (todo.isEmpty()) {
                chat.setState(ChatStateEnum.BASE_STATE);
                chatRepository.save(chat);
                return List.of(messageUtils.createMessage(chat, "No todo selected, cancelled."));
            } else {
                LocalDateTime dateTime = parseDateTime(message);
                if (dateTime == null) {
                    return List.of(messageUtils.createMessage(chat, "Invalid date or time, use format: " + dateTimeFormat));
                }
                chat.setState(ChatStateEnum.EDIT_TODO);
                chatRepository.save(chat);
                TodoNotification todoNotification = new TodoNotification();
                todoNotification.setNotificationTime(dateTime);
                todoNotification.setTodo(todo.get());
                repository.save(todoNotification);
                return List.of(messageUtils.createMessage(chat, "Todo notification date and time is set to " + dateTime));
            }
        }
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
