package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.TodoService;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.itmo.sd.deadliner2bot.utils.DateTimeUtils.dateFormat;
import static ru.itmo.sd.deadliner2bot.utils.DateTimeUtils.parseDate;

@Component
@RequiredArgsConstructor
public class AddEndDateState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.ADD_END_DATE;
    private final MessageUtils messageUtils;
    private final TodoService todoService;

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
            return List.of(messageUtils.createMessage(chat, "Operation canceled."));
        } else {
            if (todo.isEmpty()) {
                chat.setState(ChatStateEnum.BASE_STATE);
                chatRepository.save(chat);
                return List.of(messageUtils.createMessage(chat, "No todo selected, cancelled."));
            } else {
                LocalDateTime date = parseDate(message);
                if (date != null) {
                    chat.setState(ChatStateEnum.EDIT_TODO);
                    chatRepository.save(chat);
                    todo.get().setEndTime(date);
                    return List.of(messageUtils.createMessage(chat, "Todo end date set to " + date));
                } else {
                    return List.of(messageUtils.createMessage(chat, "Invalid date, format: " + dateFormat));
                }
            }
        }
    }


    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
