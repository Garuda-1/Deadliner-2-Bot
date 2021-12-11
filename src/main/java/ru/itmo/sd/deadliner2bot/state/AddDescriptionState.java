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
public class AddDescriptionState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.ADD_DESCRIPTION;
    private final TodoService todoService;
    private final MessageService messageService;

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
        } else {
            if (todo.isEmpty()) {
                chat.setState(ChatStateEnum.BASE_STATE);
                chatRepository.save(chat);
                return List.of(messageService.createMessage(chat, "No todo selected, cancelled."));
            } else {
                if (validateDescription(message)) {
                    todo.get().setDescription(message);
                    todoService.save(todo.get());
                    chat.setState(ChatStateEnum.EDIT_TODO);
                    chatRepository.save(chat);
                    return List.of(messageService.createMessage(chat, "Todo description added: " + message));
                } else {
                    return List.of(messageService.createMessage(chat, "Invalid description"));
                }
            }
        }
    }

    private boolean validateDescription(String text) {
        //TODO
        return text.length() < 1024;
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }
}
