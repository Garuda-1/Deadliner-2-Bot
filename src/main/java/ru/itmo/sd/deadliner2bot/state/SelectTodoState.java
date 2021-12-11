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
public class SelectTodoState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.SELECT_TODO;
    private final TodoService todoService;
    private final MessageService messageService;

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "Operation cancelled"));
        } else if (message.startsWith("/select")) {
            String number = message.substring(8);
            System.out.println(number);
            long todoId;
            try {
                todoId = Long.parseLong(number);
            } catch (NumberFormatException e) {
                return List.of(messageService.createMessage(chat, "Please enter /select [todo_id]"));
            }

            Optional<Todo> todoOptional = todoService.findTodoByChatIdAndTodoId(chat.getChatId(), todoId);
            if (todoOptional.isPresent()) {
                Todo todo = todoOptional.get();
                todo.setSelected(true);
                todoService.save(todo);
                chat.setState(ChatStateEnum.TODO_SELECTED);
                chatRepository.save(chat);
                return List.of(messageService.createMessage(chat, "Todo \"" + todo.getName() + "\" selected"));
            } else {
                return List.of(messageService.createMessage(chat, "Todo with id " + todoId + " not found"));
            }
        } else {
            return List.of(messageService.createMessage(chat, "Please enter /select [todo_id]"));
        }
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }
}