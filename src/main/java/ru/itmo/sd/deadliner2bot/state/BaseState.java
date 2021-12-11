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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BaseState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.BASE_STATE;
    private final TodoService todoService;
    private final MessageService messageService;

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        List<BotApiMethod<?>> response = new ArrayList<>();
        if (message.startsWith("/show_todos_for_today")) {
            response.add(messageService.createMessage(chat, "Todo's for today: \n Do the deadliner2."));
            sendTodos(chat,
                    todoService.findAllTodosByChatId(chat.getChatId(), LocalDateTime.now()),
                    true,
                    response);
            return response;
        } else if (message.startsWith("/select_todo")) {
            chat.setState(ChatStateEnum.SELECT_TODO);
            chatRepository.save(chat);
            response.add(messageService.createMessage(chat, "Your Todos:"));
            sendTodos(chat,
                    todoService.findAllTodosByChatId(chat.getChatId(), LocalDateTime.now()),
                    false,
                    response
            );
            response.add(messageService.createMessage(chat, "\nPlease enter todo number"));
            return response;
        } else if (message.startsWith("/create_new_todo")) {
            chat.setState(ChatStateEnum.ADD_TODO_NAME);
            chatRepository.save(chat);
            response.add(messageService.createMessage(chat, "Please enter name"));
            return response;
        } else if (message.startsWith("/help")) {
            response.add(messageService.createMessage(chat, "This is help!"));
            return response;
        } else if (message.startsWith("/change_notification_plan")) {
            chat.setState(ChatStateEnum.SELECT_DAYS);
            chatRepository.save(chat);
            response.add(messageService.createMessage(chat, "Please enter days of notifications"));
            return response;
        }
        return null;
    }

    private void sendTodos(Chat chat, List<Todo> todos, boolean withDescription, List<BotApiMethod<?>> response) {
        if (withDescription) {
            todos.forEach(todo -> response.add(messageService.createMessage(chat,
                    todo.getName() + (todo.getDescription() == null ? "" : '\n' + todo.getDescription()))));
        } else {
            todos.forEach(todo -> response.add(messageService.createMessage(chat,
                    todo.getName() + "\n with todoId: " + todo.getTodoId())));
        }
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }
}
