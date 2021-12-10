package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;

@Component
@RequiredArgsConstructor
public class TodoSelected implements ChatState {

    private final ChatRepository chatRepository;

    private final ChatStateEnum chatStateEnum = ChatStateEnum.TODO_SELECTED;

    @Override
    public String process(Chat chat, String message) {
        return "this is TodoSelected state";
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }

}
