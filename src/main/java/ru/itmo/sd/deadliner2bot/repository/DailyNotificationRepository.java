package ru.itmo.sd.deadliner2bot.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;

import java.time.LocalDateTime;
import java.util.Set;

public interface DailyNotificationRepository extends CrudRepository<DailyNotification, Long> {

    @Query(value = "SELECT * FROM daily_notifications WHERE notification_time < ?", nativeQuery = true)
    Set<DailyNotification> findAllBeforeTimeLimit(LocalDateTime timeLimit);

    @Modifying
    @Query(value = "DELETE FROM daily_notifications WHERE notification_time < ?", nativeQuery = true)
    void removeAllBeforeTimeLimit(LocalDateTime timeLimit);
}
