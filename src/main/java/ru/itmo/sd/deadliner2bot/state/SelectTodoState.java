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
import ru.itmo.sd.deadliner2bot.ui.messages.MessageSourceUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SelectTodoState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.SELECT_TODO_STATE;
    private final TodoService todoService;
    private final MessageSourceUtils messageSourceUtils;
    private final Commands commands;
    private Map<String, Commands.CommandInfo> commandsInfo;

    @PostConstruct
    public void postConstruct() {
        commandsInfo = commands.loadAllCommandsInfo(getChatStateEnum(), List.of(
                "cancel"
        ));
    }

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        if (commandsInfo.get("cancel").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "cancel"));
        } else if (message.startsWith("/")) {
            return null;
        }

        Long todoId = parseNumber(message);
        if (todoId == null) {
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "invalid-id-format"));
        }

        Optional<Todo> todoOptional = todoService.findTodoByChatIdAndTodoId(chat.getChatId(), todoId);
        if (todoOptional.isEmpty()) {
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "not-found", todoId));
        }

        Todo todo = todoOptional.get();
        todo.setSelected(true);
        todoService.save(todo);
        chat.setState(ChatStateEnum.TODO_SELECTED_STATE);
        chatRepository.save(chat);
        return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "selected", todo.getName()));
    }

    private Long parseNumber(String numberString) {
        try {
            return Long.parseLong(numberString);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
