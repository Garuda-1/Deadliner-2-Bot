package ru.itmo.sd.deadliner2bot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;

import java.time.LocalDateTime;
import java.util.Set;

public interface DailyNotificationRepository extends CrudRepository<DailyNotification, Long> {

    @Query(value = "SELECT * FROM daily_notifications " +
            "WHERE EXTRACT(HOUR FROM notification_time) = EXTRACT(HOUR FROM CAST(?1 AS TIMESTAMP))" +
            "AND EXTRACT(MINUTE FROM notification_time) >= EXTRACT(MINUTE FROM CAST(?1 AS TIMESTAMP))" +
            "AND EXTRACT(HOUR FROM notification_time) = EXTRACT(HOUR FROM CAST(?2 AS TIMESTAMP))" +
            "AND EXTRACT(MINUTE FROM notification_time) < EXTRACT(MINUTE FROM CAST(?2 AS TIMESTAMP))" +
            "AND EXTRACT(ISODOW FROM notification_time) = EXTRACT(ISODOW FROM CAST(?1 AS TIMESTAMP))",
            nativeQuery = true)
    Set<DailyNotification> findAllBeforeTimeLimitOnCurrentWeekday(LocalDateTime now, LocalDateTime limit);

    @Query(value = "SELECT D.* FROM daily_notifications D INNER JOIN chats C on C.chat_id = D.chat_id " +
            "WHERE C.chat_id = ?1 AND D.notification_time >= ?2",
            nativeQuery = true)
    Set<DailyNotification> findDailyNotificationsByChatStartingWithDate(long chatId, LocalDateTime start);
}
