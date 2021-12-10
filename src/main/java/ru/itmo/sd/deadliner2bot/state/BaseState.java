package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;

@Component
@RequiredArgsConstructor
public class BaseState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.BASE_STATE;

    @Override
    public String process(Chat chat, String message) {
        if (message.startsWith("/show_todos_for_today")) {
            return "Todo's for today: \n Do the deadliner2.";
        } else if (message.startsWith("/select_todo")) {
            chat.setState(ChatStateEnum.SELECT_TODO);
            chatRepository.save(chat);
            return "Please enter todo number";
        }
        return "unsupported command";
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }
}
