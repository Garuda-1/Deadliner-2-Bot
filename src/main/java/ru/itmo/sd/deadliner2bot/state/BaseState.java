package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.TodoService;
import ru.itmo.sd.deadliner2bot.ui.commands.Commands;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageFormatter;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageSourceUtils;
import ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils;

import javax.annotation.PostConstruct;
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
    private final MessageSourceUtils messageSourceUtils;
    private final Commands commands;
    private final DateTimeUtils dateTimeUtils;
    private Map<String, Commands.CommandInfo> commandsInfo;

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
                    todoService.findNotCompletedTodosByChatId(chat.getChatId(), dateTimeUtils.now());
            if (notCompletedTodos.isEmpty()) {
                response.add(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "no-active-todos", chat));
            } else {
                response.add(messageFormatter.notCompletedTodosMessage(chat, notCompletedTodos, chatStateEnum,
                        "active-todos-header", false));
            }
            return response;
        } else if (commandsInfo.get("select-todo").testMessageForCommand(message)) {
            List<Todo> notCompletedTodos =
                    todoService.findNotCompletedTodosByChatId(chat.getChatId(), dateTimeUtils.now());
            if (notCompletedTodos.isEmpty()) {
                response.add(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "no-active-todos"));
            } else {
                response.add(messageFormatter.notCompletedTodosMessage(chat, notCompletedTodos, chatStateEnum,
                        "select-todos-request", true));
                chat.setState(ChatStateEnum.SELECT_TODO_STATE);
                chatRepository.save(chat);
            }
            return response;
        } else if (commandsInfo.get("create-new-todo").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.ADD_NAME_STATE);
            chatRepository.save(chat);
            response.add(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "enter-new-todo-name"));
            return response;
        } else if (commandsInfo.get("change-notification-plan").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.SELECT_DAYS_STATE);
            chatRepository.save(chat);
            response.add(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum,
                    "enter-new-daily-notification-weekdays"));
            return response;
        }
        return null;
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
