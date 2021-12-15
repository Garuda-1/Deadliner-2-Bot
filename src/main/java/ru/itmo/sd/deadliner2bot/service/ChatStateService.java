package ru.itmo.sd.deadliner2bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.state.ChatState;
import ru.itmo.sd.deadliner2bot.ui.commands.Commands;
import ru.itmo.sd.deadliner2bot.ui.messages.CommonMessages;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatStateService {

    private final static Map<ChatStateEnum, ChatState> chatStateMap = new HashMap<>();

    private final ChatRepository chatRepository;
    private final List<ChatState> chatStateList;
    private final MessageUtils messageUtils;
    private final CommonMessages commonMessages;
    private final Commands commands;

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
        String startCommand = commonMessages.getByKey("start-command");
        String helpCommand = commonMessages.getByKey("help-command");
        if (chatOptional.isEmpty() && startCommand.equals(message)) {
            Chat chat = new Chat();
            chat.setChatId(chatId);
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, commonMessages.getByKey("new-user-welcome")));
        }
        if (chatOptional.isEmpty()) {
            return List.of(messageUtils.createMessage(chatId, commonMessages.getByKey("chat-not-found", startCommand)));
        }

        Chat chat = chatOptional.get();
        ChatStateEnum state = chat.getState();
        if (helpCommand.equals(message)) {
            String helpMessageText = commands.formHelpMessage(state);
            return List.of(messageUtils.createMessage(chatId, helpMessageText));
        }
        return chatStateMap.get(state).process(chat, message);
    }
}
