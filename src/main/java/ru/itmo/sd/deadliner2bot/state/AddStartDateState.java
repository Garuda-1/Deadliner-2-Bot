package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.messages.MessageService;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.TodoService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AddStartDateState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.ADD_START_DATE;
    private final MessageService messageService;
    private final TodoService todoService;
    //    private final String dateFormat = "yyyy-MM-dd HH:mm:ss z";
    private final String dateFormat = "dd-MM-yyyy";
    private final String timeFormat = "HH:mm";


    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        Optional<Todo> todo = todoService.findSelectedTodoByChatId(chat.getChatId());
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            if (todo.isPresent()) {
                todo.get().setSelected(false);
                todoService.save(todo.get());
            }
            return List.of(messageService.createMessage(chat, "Operation canceled."));
        } else {
            if (todo.isEmpty()) {
                chat.setState(ChatStateEnum.BASE_STATE);
                chatRepository.save(chat);
                return List.of(messageService.createMessage(chat, "No todo selected, cancelled."));
            } else {
                LocalDateTime date = parse(message);
                if (date != null) {
                    chat.setState(ChatStateEnum.EDIT_TODO);
                    chatRepository.save(chat);
                    todo.get().setStartTime(date);
                    return List.of(messageService.createMessage(chat, "Todo start date set to " + date));
                } else {
                    return List.of(messageService.createMessage(chat, "Invalid date, format: " + dateFormat));
                }
            }
        }
    }

    private LocalDateTime parse(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat + timeFormat);
        try {
            return LocalDateTime.parse(dateStr + "00:00", formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }
}
