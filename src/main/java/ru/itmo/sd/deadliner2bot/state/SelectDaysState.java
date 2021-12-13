package ru.itmo.sd.deadliner2bot.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.service.DailyNotificationService;
import ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils;
import ru.itmo.sd.deadliner2bot.utils.messages.MessageUtils;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SelectDaysState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.SELECT_DAYS;
    private final MessageUtils messageUtils;
    private final DateTimeUtils dateTimeUtils;
    private final DailyNotificationService dailyNotificationService;


    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, "Operation canceled."));
        } else {
            Set<DayOfWeek> days = dateTimeUtils.parseDaysOfWeek(message);
            if (days == null) {
                return List.of(messageUtils.createMessage(chat, "Write days separated with spaces"));
            }
            days.forEach(
                    dayOfWeek -> {
                        DailyNotification dailyNotification = new DailyNotification();
                        dailyNotification.setChat(chat);
                        dailyNotification.setNotificationTime(dateTimeUtils.getDateTimeFromDayUnconfirmed(dayOfWeek));
                        dailyNotificationService.save(dailyNotification);
                    }
            );
            chat.setState(ChatStateEnum.SELECT_TIME);
            chatRepository.save(chat);
            return List.of(messageUtils.createMessage(chat, "You selected days of notifications. " +
                                                            "\n Please select time."));
        }
    }

    @Override
    public ChatStateEnum getChatStateEnum() {
        return chatStateEnum;
    }
}
