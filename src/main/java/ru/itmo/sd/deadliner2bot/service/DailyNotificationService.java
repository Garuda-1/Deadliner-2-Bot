package ru.itmo.sd.deadliner2bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;
import ru.itmo.sd.deadliner2bot.repository.DailyNotificationRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DailyNotificationService {

    private final DailyNotificationRepository dailyNotificationRepository;

    public void save(DailyNotification notification) {
        dailyNotificationRepository.save(notification);
    }

    public Set<DailyNotification> findDailyNotificationsByChat(long chatId) {
        return dailyNotificationRepository.findDailyNotificationsByChat(chatId);
    }

}
