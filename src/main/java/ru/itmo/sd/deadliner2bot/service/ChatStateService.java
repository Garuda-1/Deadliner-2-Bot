package ru.itmo.sd.deadliner2bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.state.ChatState;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageFormatter;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageSourceUtils;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatStateService {

    private final static Map<ChatStateEnum, ChatState> chatStateMap = new HashMap<>();

    private final ChatRepository chatRepository;
    private final List<ChatState> chatStateList;
    private final MessageSourceUtils messageSourceUtils;
    private final MessageFormatter messageFormatter;

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

    public List<BotApiMethod<?>> processMessage(long chatId, String message, Locale chatLocale) {
        Optional<Chat> chatOptional = chatRepository.findById(chatId);
        String startCommand = messageSourceUtils.getCommonProperty("start-command");
        String helpCommand = messageSourceUtils.getCommonProperty("help-command");
        if (chatOptional.isEmpty() && startCommand.equals(message)) {
            Chat chat = new Chat();
            chat.setChatId(chatId);
            chat.setLanguageCode(chatLocale);
            chat.setState(ChatStateEnum.BASE_STATE);
            chat = chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, "new-user-welcome"));
        }
        if (chatOptional.isEmpty()) {
            return List.of(messageSourceUtils.createMarkdownMessage(chatId, "chat-not-found", chatLocale));
        }

        Chat chat = chatOptional.get();
        if (!chat.getLanguageCode().equals(chatLocale)) {
            chat.setLanguageCode(chatLocale);
            chat = chatRepository.save(chat);
        }
        ChatStateEnum state = chat.getState();
        if (helpCommand.equals(message)) {
            return List.of(messageFormatter.stateHelpMessage(chat, state));
        }
        return chatStateMap.get(state).process(chat, message);
    }
}
