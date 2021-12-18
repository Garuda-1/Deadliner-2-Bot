package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoRepository;
import ru.itmo.sd.deadliner2bot.service.TodoService;
import ru.itmo.sd.deadliner2bot.ui.commands.Commands;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageFormatter;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageSourceUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EditTodoState implements ChatState {

    private static final ChatStateEnum chatStateEnum = ChatStateEnum.EDIT_TODO_STATE;

    private final ChatRepository chatRepository;
    private final TodoRepository todoRepository;
    private final MessageFormatter messageFormatter;
    private final MessageSourceUtils messageSourceUtils;
    private final TodoService todoService;
    private final Commands commands;
    private Map<String, Commands.CommandInfo> commandsInfo;

    @PostConstruct
    public void postConstruct() {
        commandsInfo = commands.loadAllCommandsInfo(getChatStateEnum(), List.of(
                "finish",
                "change-name",
                "add-description",
                "add-start-date",
                "add-end-date",
                "add-notification",
                "switch-daily-notifications",
                "view"
        ));
    }

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        Optional<Todo> todo = todoService.findSelectedTodoByChatId(chat.getChatId());
        if (todo.isEmpty()) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "no-todo-selected"));
        } else if (commandsInfo.get("finish").testMessageForCommand(message)) {
            todo.get().setSelected(false);
            todoService.save(todo.get());
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "finish"));
        } else if (commandsInfo.get("change-name").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.ADD_NAME_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "enter-new-todo-name"));
        } else if (commandsInfo.get("add-description").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.ADD_DESCRIPTION_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "enter-todo-description"));
        } else if (commandsInfo.get("add-start-date").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.ADD_START_DATE_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "enter-start-date"));
        } else if (commandsInfo.get("add-end-date").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.ADD_END_DATE_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "enter-end-date"));
        } else if (commandsInfo.get("add-notification").testMessageForCommand(message)) {
            chat.setState(ChatStateEnum.ADD_TODO_NOTIFICATION_STATE);
            chatRepository.save(chat);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "enter-notification-date-time"));
        } else if (commandsInfo.get("switch-daily-notifications").testMessageForCommand(message)) {
            Todo selected = todo.get();
            boolean dailyNotificationsEnabled = !selected.isDailyNotificationsEnabled();
            selected.setDailyNotificationsEnabled(dailyNotificationsEnabled);
            selected = todoRepository.save(selected);
            return List.of(messageSourceUtils.createMarkdownMessage(chat, chatStateEnum, "daily-notification-set",
                    selected.isDailyNotificationsEnabled()));
        } else if (commandsInfo.get("view").testMessageForCommand(message)) {
            Todo selected = todo.get();
            return List.of(messageFormatter.todoViewMessage(chat, selected));
        }
        return null;
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
