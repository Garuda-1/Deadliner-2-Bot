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
import ru.itmo.sd.deadliner2bot.ui.messages.MessageFormatter;
import ru.itmo.sd.deadliner2bot.ui.messages.StateMessages;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BaseState implements ChatState {

    private static final ChatStateEnum chatStateEnum = ChatStateEnum.BASE_STATE;

    private final ChatRepository chatRepository;
    private final TodoService todoService;
    private final MessageFormatter messageFormatter;
    private final MessageUtils messageUtils;
    private final Commands commands;
    private final StateMessages stateMessages;
    private Map<String, CommandInfo> commandsInfo;

    @PostConstruct
    public void postConstruct() {
        commandsInfo = commands.loadAllCommandsInfo(getChatStateEnum(), List.of(
                "show-todos-for-today",
                "select-todo",
                "create-new-todo",
                "change-notification-plan"
        ));
    }

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        List<BotApiMethod<?>> response = new ArrayList<>();
        if (commandsInfo.get("show-todos-for-today").testMessageForCommand(message)) {
            List<Todo> notCompletedTodos =
                    todoService.findNotCompletedTodosByChatId(chat.getChatId(), LocalDateTime.now());
            String responseMessage;
            if (notCompletedTodos.isEmpty()) {
                responseMessage = stateMessages.getMessageByKey(chatStateEnum, "no-active-todos");
            } else {
                responseMessage = messageFormatter.notCompletedTodos(notCompletedTodos,
                        stateMessages.getMessageByKey(chatStateEnum, "active-todos-header"), false);
            }
            response.add(messageUtils.createMessage(chat, responseMessage));
            return response;
        } else if (commandsInfo.get("select-todo").testMessageForCommand(message)) {
            List<Todo> notCompletedTodos =
                    todoService.findNotCompletedTodosByChatId(chat.getChatId(), LocalDateTime.now());
            String responseMessage;
            if (notCompletedTodos.isEmpty()) {
                responseMessage = stateMessages.getMessageByKey(chatStateEnum, "no-active-todos");
            } else {
                responseMessage = messageFormatter.notCompletedTodos(notCompletedTodos,
                        stateMessages.getMessageByKey(chatStateEnum, "select-todos-request"), true);
                chat.setState(ChatStateEnum.SELECT_TODO_STATE);
                chatRepository.save(chat);
            }
            response.add(messageUtils.createMessage(chat, responseMessage));
            return response;
        } else if (commandsInfo.get("create-new-todo").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.ADD_NAME_STATE);
            chatRepository.save(chat);
            response.add(messageUtils.createMessage(chat,
                    stateMessages.getMessageByKey(chatStateEnum, "enter-new-todo-name")));
            return response;
        } else if (commandsInfo.get("change-notification-plan").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.SELECT_DAYS_STATE);
            chatRepository.save(chat);
            response.add(messageUtils.createMessage(chat,
                    stateMessages.getMessageByKey(chatStateEnum, "enter-new-daily-notification-weekdays")));
            return response;
        }
        return null;
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
