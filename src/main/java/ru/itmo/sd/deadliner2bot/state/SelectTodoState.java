package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;

@Component
@RequiredArgsConstructor
public class SelectTodoState implements ChatState {

    private final ChatRepository chatRepository;

    private final ChatStateEnum chatStateEnum = ChatStateEnum.SELECT_TODO;

    @Override
    public String process(Chat chat, String message) {
        int todoNumber;
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return "Operation cancelled";
        }
        try {
            todoNumber = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            return "Please enter /[number]";
        }
        if (validateTodoNumber(chat, todoNumber)) {
            chat.setState(ChatStateEnum.TODO_SELECTED);
            chatRepository.save(chat);
            return "You selected todo №" + todoNumber +
                   "\n what do you want to do?";
        } else {
            return "Please enter number between 0 and 10";
        }
    }

    private boolean validateTodoNumber(Chat chat, int todoNumber) {
        return (todoNumber > 0 && todoNumber < 10);
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }
}