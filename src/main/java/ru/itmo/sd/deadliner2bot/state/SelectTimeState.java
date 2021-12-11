package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.messages.MessageService;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.DailyNotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SelectTimeState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.SELECT_TIME;
    private final MessageService messageService;
    private final String dateFormat = "HH:mm";
    private final DailyNotificationService dailyNotificationService;

    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "Operation canceled"));
        } else {
            LocalDateTime time = parseTime(message);
            if (time != null) {
                Set<DailyNotification> toUpdate = dailyNotificationService.findDailyNotificationsByChat(chat.getChatId());
                toUpdate.forEach(
                        notification -> {
                            LocalDateTime date = notification.getNotificationTime();
                            date = date.plusHours(time.getHour());
                            date = date.plusMinutes(time.getMinute());
                            notification.setNotificationTime(date);
                            dailyNotificationService.save(notification);
                        }
                );
                return List.of(messageService.createMessage(chat, "Notification time is set to " + time));
            } else {
                return List.of(messageService.createMessage(chat, "Please enter time in format: " + dateFormat));
            }
        }
    }

    private LocalDateTime parseTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        try {
            return LocalDateTime.parse(time, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }
}
