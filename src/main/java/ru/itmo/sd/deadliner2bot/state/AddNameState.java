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
public class AddNameState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.ADD_NAME;
    private final TodoService todoService;
    private final MessageUtils messageUtils;

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, "Operation canceled"));
        } else {
            if (validateName(message)) {
                Optional<Todo> todoOptional = todoService.findSelectedTodoByChatId(chat.getChatId());
                Todo todo;
                if (todoOptional.isPresent()) {
                    todo = todoOptional.get();
                } else {
                    todo = new Todo();
                    todo.setChat(chat);
                    todo.setSelected(true);
                }
                todo.setName(message);
                todoService.save(todo);
                chat.setState(ChatStateEnum.EDIT_TODO);
                chatRepository.save(chat);
                if (todoOptional.isPresent()) {
                    return List.of(messageUtils.createMessage(chat, "Todo name changed to " + message));
                } else {
                    return List.of(messageUtils.createMessage(chat, "New todo saved with name " + message));
                }
            } else {
                return List.of(messageUtils.createMessage(chat, "Invalid name"));
            }
        }
    }

    private boolean validateName(String name) {
        //TODO: add more advanced validation
        return !name.isBlank() && name.length() < 256;
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
