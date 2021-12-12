package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.TodoService;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BaseState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.BASE_STATE;
    private final TodoService todoService;
    private final MessageUtils messageUtils;

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        List<BotApiMethod<?>> response = new ArrayList<>();
        if (message.startsWith("/show_todos_for_today")) {
            String todos = formatTodos(todoService.findNotCompletedTodosByChatId(chat.getChatId(), LocalDateTime.now()), true);
            response.add(messageUtils.createMessage(chat, "Todos for today: \n" + todos));
            return response;
        } else if (message.startsWith("/select_todo")) {
            chat.setState(ChatStateEnum.SELECT_TODO);
            chatRepository.save(chat);
            response.add(messageUtils.createMessage(chat, "Your Todos:"));
            response.add(messageUtils.createMessage(chat,
                    formatTodos(todoService.findNotCompletedTodosByChatId(chat.getChatId(), LocalDateTime.now()), false)));
            response.add(messageUtils.createMessage(chat, "\nPlease enter todo number"));
            return response;
        } else if (message.startsWith("/create_new_todo")) {
            chat.setState(ChatStateEnum.ADD_NAME);
            chatRepository.save(chat);
            response.add(messageUtils.createMessage(chat, "Please enter name"));
            return response;
        } else if (message.startsWith("/help")) {
            response.add(messageUtils.createMessage(chat, "This is help!"));
            return response;
        } else if (message.startsWith("/change_notification_plan")) {
            chat.setState(ChatStateEnum.SELECT_DAYS);
            chatRepository.save(chat);
            response.add(messageUtils.createMessage(chat, "Please enter days of notifications"));
            return response;
        }
        return null;
    }

    private String formatTodos(List<Todo> todos, boolean withDescription) {
        StringBuilder stringBuilder = new StringBuilder();
        if (withDescription) {
            todos.forEach(todo -> stringBuilder.append(todo.getTodoId())
                    .append(") ")
                    .append("**")
                    .append(todo.getName())
                    .append("**")
                    .append(todo.getDescription() == null ? "" : '\n' + todo.getDescription())
                    .append('\n'));
        } else {
            todos.forEach(todo -> stringBuilder.append(todo.getTodoId()).append(") ").append(todo.getName()).append('\n'));
        }
        return stringBuilder.toString();
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
