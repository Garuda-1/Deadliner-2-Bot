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
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TodoSelectedState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.TODO_SELECTED_STATE;
    private final TodoService todoService;
    private final MessageUtils messageUtils;
    private final Commands commands;
    private final StateMessages stateMessages;
    private Map<String, CommandInfo> commandsInfo;

    @PostConstruct
    public void postConstruct() {
        commandsInfo = commands.loadAllCommandsInfo(getChatStateEnum(), List.of(
                "cancel",
                "mark-todo-done",
                "edit-todo"
        ));
    }

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        Optional<Todo> todoOptional = todoService.findSelectedTodoByChatId(chat.getChatId());
        if (commandsInfo.get("cancel").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            if (todoOptional.isPresent()) {
                todoOptional.get().setSelected(false);
                todoService.save(todoOptional.get());
            }
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "cancel")));
        } else if (todoOptional.isEmpty()) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "no-todo-selected")));
        } else if (commandsInfo.get("mark-todo-done").testMessageForCommand(message)) {
            Todo todo = todoOptional.get();
            todo.setSelected(false);
            todo.setCompleted(true);
            todoService.save(todo);
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "todo-marked-done")));
        } else if (commandsInfo.get("edit-todo").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.EDIT_TODO_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, stateMessages.getMessageByKey(chatStateEnum, "start-editing")));
        }
        return null;
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
