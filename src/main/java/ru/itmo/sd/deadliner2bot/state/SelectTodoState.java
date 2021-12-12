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

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SelectTodoState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.SELECT_TODO;
    private final TodoService todoService;
    private final MessageUtils messageUtils;

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, "Operation cancelled"));
        }
        Long todoId = parseNumber(message);
        if (todoId == null) {
            return List.of(messageUtils.createMessage(chat, "Please enter number of todo you want to select."));
        }
        Optional<Todo> todoOptional = todoService.findTodoByChatIdAndTodoId(chat.getChatId(), todoId);
        if (todoOptional.isEmpty()) {
            return List.of(messageUtils.createMessage(chat, "Todo with id " + todoId + " not found"));
        }
        Todo todo = todoOptional.get();
        todo.setSelected(true);
        todoService.save(todo);
        chat.setState(ChatStateEnum.TODO_SELECTED);
        chatRepository.save(chat);
        return List.of(messageUtils.createMessage(chat, "Todo \"" + todo.getName() + "\" selected"));
    }

    private Long parseNumber(String numberString) {
        try {
            return Long.parseLong(numberString);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
