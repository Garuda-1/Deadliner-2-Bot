package ru.itmo.sd.deadliner2bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;
import ru.itmo.sd.deadliner2bot.repository.DailyNotificationRepository;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DailyNotificationService {

    private final DailyNotificationRepository dailyNotificationRepository;

    public void save(DailyNotification notification) {
        dailyNotificationRepository.save(notification);
    }

    public Set<DailyNotification> findDailyNotificationsByChatStartingWithDate(long chatId, LocalDateTime startingDate) {
        return dailyNotificationRepository.findDailyNotificationsByChatStartingWithDate(chatId, startingDate);
    }

    public void delete(DailyNotification notification) {
        dailyNotificationRepository.delete(notification);
    }
}
