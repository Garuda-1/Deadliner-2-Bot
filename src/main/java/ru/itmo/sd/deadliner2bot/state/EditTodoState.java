package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.messages.MessageService;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.TodoService;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EditTodoState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.EDIT_TODO;
    private final MessageService messageService;
    private final TodoService todoService;


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
            return List.of(messageService.createMessage(chat, "Operation canceled."));
        } else if (todo.isEmpty()) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "No todo selected, cancelled."));
        } else if (message.startsWith("/save_todo")) {
            todo.get().setSelected(false);
            todoService.save(todo.get());
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "Todo saved."));
        } else if (message.startsWith("/change_name")) {
            chat.setState(ChatStateEnum.ADD_TODO_NAME);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "Please enter new name"));
        } else if (message.startsWith("/add_description")) {
            chat.setState(ChatStateEnum.ADD_DESCRIPTION);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "Please enter description"));
        } else if (message.startsWith("/add_start_date")) {
            chat.setState(ChatStateEnum.ADD_START_DATE);
            chatRepository.save(chat);
            //TODO
            return List.of(messageService.createMessage(chat, "Please enter start date"));
        } else if (message.startsWith("/add_end_date")) {
            chat.setState(ChatStateEnum.ADD_END_DATE);
            chatRepository.save(chat);
            //TODO
            return List.of(messageService.createMessage(chat, "Please enter end date"));
        } else if (message.startsWith("/switch_daily_notifications")) {
            Todo selected = todo.get();
            selected.setDailyNotificationsEnabled(!selected.isDailyNotificationsEnabled());
            return List.of(messageService.createMessage(chat, "Unsupported operation"));
        }
        return null;
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }
}
