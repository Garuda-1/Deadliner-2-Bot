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
import java.util.*;


@Component
@RequiredArgsConstructor
public class SelectDaysState implements ChatState {

    private final ChatRepository chatRepository;
    private final ChatStateEnum chatStateEnum = ChatStateEnum.SELECT_DAYS;
    private final MessageService messageService;
    private final DailyNotificationService dailyNotificationService;


    @Override
    public List<BotApiMethod<?>> process(Chat chat, String message) {
        if (message.startsWith("/cancel")) {
            chat.setState(ChatStateEnum.BASE_STATE);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "Operation canceled."));
        } else {
            //TODO
            List<LocalDateTime> response = validate(message);
            if (response == null) {
                return List.of(messageService.createMessage(chat, "Write days: [Mon, Tue, Wed, Thu, Fri, Sat, Sun]"));
            } else {
                response.forEach(
                        day -> {
                            DailyNotification newDay = new DailyNotification();
                            newDay.setChat(chat);
                            newDay.setNotificationTime(day);
                            dailyNotificationService.save(newDay);
                        }
                );
            }
            chat.setState(ChatStateEnum.SELECT_TIME);
            chatRepository.save(chat);
            return List.of(messageService.createMessage(chat, "Days selection is unsuported"));
        }
    }

    private List<LocalDateTime> validate(String message) {
        Set<String> days = new HashSet<>(Arrays.asList(message.toLowerCase().split(" ")));
        List<LocalDateTime> result = new ArrayList<>();
        for (String day : days) {
            switch (day) {
                case ("mon"):
                    result.add(LocalDateTime.parse("2029-01-01"));
                    break;
                case ("tue"):
                    result.add(LocalDateTime.parse("2029-01-02"));
                    break;
                case ("wed"):
                    result.add(LocalDateTime.parse("2029-01-03"));
                    break;
                case ("thu"):
                    result.add(LocalDateTime.parse("2029-01-04"));
                    break;
                case ("fri"):
                    result.add(LocalDateTime.parse("2029-01-05"));
                    break;
                case ("sat"):
                    result.add(LocalDateTime.parse("2029-01-06"));
                    break;
                case ("sun"):
                    result.add(LocalDateTime.parse("2029-01-07"));
                    break;
                default:
                    return null;
            }
        }
        return result;
    }

    @Override
    public ChatStateEnum getEnum() {
        return chatStateEnum;
    }
}
