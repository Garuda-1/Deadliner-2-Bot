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
public class TodoSelectedState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.TODO_SELECTED;
    private final TodoService todoService;
    private final MessageService messageService;

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        Optional<Todo> todoOptional = todoService.findSelectedTodoByChatId(chat.getChatId());
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            if (todoOptional.isPresent()) {
                todoOptional.get().setSelected(false);
                todoService.save(todoOptional.get());
            }
            return List.of(messageService.createMessage(chat, "Operation canceled."));
        } else if (todoOptional.isEmpty()) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "No todo selected, cancelled."));
        } else if (message.startsWith("/mark_todo_done")) {
            Todo todo = todoOptional.get();
            todo.setSelected(false);
            todo.setCompleted(!todo.isCompleted());
            todoService.save(todo);
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "Operation not marked as done (unsupported)"));
        } else if (message.startsWith("/edit_todo")) {
            chat.setState(ChatStateEnum.EDIT_TODO);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "What do you want to edit?"));
        }
        return null;
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }

}
