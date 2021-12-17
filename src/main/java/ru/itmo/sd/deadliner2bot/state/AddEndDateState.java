package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.TodoService;
import ru.itmo.sd.deadliner2bot.ui.commands.CommandInfo;
import ru.itmo.sd.deadliner2bot.ui.commands.Commands;
import ru.itmo.sd.deadliner2bot.ui.messages.StateMessages;
import ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils.dateFormat;

@Component
@RequiredArgsConstructor
public class AddEndDateState implements ChatState {

    private static final ChatStateEnum chatStateEnum = ChatStateEnum.ADD_END_DATE_STATE;

    private final ChatRepository chatRepository;
    private final MessageUtils messageUtils;
    private final DateTimeUtils dateTimeUtils;
    private final TodoService todoService;
    private final Commands commands;
    private final StateMessages stateMessages;
    private Map<String, CommandInfo> commandsInfo;

    @PostConstruct
    public void postConstruct() {
        commandsInfo = commands.loadAllCommandsInfo(getChatStateEnum(), List.of(
                "cancel"
        ));
    }

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        Optional<Todo> todo = todoService.findSelectedTodoByChatId(chat.getChatId());
        if (commandsInfo.get("cancel").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.EDIT_TODO_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "cancel")));
        } else if (message.startsWith("/")) {
            return null;
        }

        if (todo.isEmpty()) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "no-todo-selected")));
        }

        LocalDateTime date = dateTimeUtils.parseDate(message);
        if (date != null) {
            chat.setState(ChatStateEnum.EDIT_TODO_STATE);
            chatRepository.save(chat);
            todo.get().setEndTime(date);
            todoService.save(todo.get());
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "todo-end-date-set", date)));
        } else {
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "invalid-date-format", dateFormat)));
        }
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
