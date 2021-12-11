package ru.itmo.sd.deadliner2bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.messages.MessageService;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.state.ChatState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatStateService {

    private final ChatRepository chatRepository;

    private final Map<ChatStateEnum, ChatState> chatStateMap;

    private final MessageService messageService;

    @Autowired
    public ChatStateService(ChatRepository chatRepository, List<ChatState> chatStateList, MessageService messageService) {
        this.chatRepository = chatRepository;
        this.messageService = messageService;
        chatStateMap = new HashMap<>();
        for (ChatStateEnum e : ChatStateEnum.values()) {
            ChatState state = chatStateList.stream()
                    .filter(s -> e.equals(s.getEnum()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported chatStateEnum found" + e.name()));
            chatStateMap.put(e, state);
        }
    }

    public List<BotApiMethod<?>> processMessage(long chatId, String message) {
        Optional<Chat> chatOptional = chatRepository.findById(chatId);
        if ("/start".equals(message)) {
            if (chatOptional.isPresent()) {
                return List.of(messageService.createMessage(chatOptional.get(), "You are already registered"));
            } else {
                Chat chat = new Chat();
                chat.setChatId(chatId);
                chat.setState(ChatStateEnum.BASE_STATE);
                chatRepository.save(chat);
                return List.of(messageService.createMessage(chat, "User created"));
            }
        } else if (chatOptional.isEmpty()) {
            return List.of(messageService.createMessage(chatId, "please enter /start"));
        } else {
            Chat chat = chatOptional.get();
            ChatStateEnum state = chat.getState();
            return chatStateMap.get(state).process(chat, message);
        }
    }
}
