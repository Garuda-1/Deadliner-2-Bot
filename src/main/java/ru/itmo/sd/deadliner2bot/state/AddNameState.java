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
public class AddNameState implements ChatState {

    private static final ChatStateEnum chatStateEnum = ChatStateEnum.ADD_NAME_STATE;

    private final ChatRepository chatRepository;
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

        if (!validateName(message)) {
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "invalid-name"));
        }

        Optional<Todo> todoOptional = todoService.findSelectedTodoByChatId(chat.getChatId());
        Todo todo;
        if (todoOptional.isPresent()) {
            todo = todoOptional.get();
        } else {
            todo = new Todo();
            todo.setChat(chat);
            todo.setSelected(true);
        }
        todo.setName(message);
        todoService.save(todo);
        chat.setState(ChatStateEnum.EDIT_TODO_STATE);
        chatRepository.save(chat);
        if (todoOptional.isPresent()) {
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "name-changed", message));
        } else {
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "new-todo-name", message));
        }
    }

    private boolean validateName(String name) {
        //TODO: add more advanced validation
        return !name.isBlank() && name.length() < 256;
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
