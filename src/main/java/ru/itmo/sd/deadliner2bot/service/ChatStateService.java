package ru.itmo.sd.deadliner2bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.state.ChatState;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatStateService {

    private final ChatRepository chatRepository;
    private final List<ChatState> chatStateList;
    private final MessageUtils messageUtils;
    private final static Map<ChatStateEnum, ChatState> chatStateMap = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        for (ChatStateEnum e : ChatStateEnum.values()) {
            ChatState state = chatStateList.stream()
                    .filter(s -> e.equals(s.getChatStateEnum()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unsupported chatStateEnum found" + e.name()));
            chatStateMap.put(e, state);
        }
    }

    public List<BotApiMethod<?>> processMessage(long chatId, String message) {
        Optional<Chat> chatOptional = chatRepository.findById(chatId);
        if (chatOptional.isEmpty() && "/start".equals(message)) {
            Chat chat = new Chat();
            chat.setChatId(chatId);
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, "User created."));
        }
        if (chatOptional.isEmpty()) {
            return List.of(messageUtils.createMessage(chatId, "Please enter command /start ."));
        }
        Chat chat = chatOptional.get();
        ChatStateEnum state = chat.getState();
        return chatStateMap.get(state).process(chat, message);
    }
}
