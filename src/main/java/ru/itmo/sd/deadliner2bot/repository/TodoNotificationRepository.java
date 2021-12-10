package ru.itmo.sd.deadliner2bot.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.itmo.sd.deadliner2bot.model.TodoNotification;

import java.time.LocalDateTime;
import java.util.Set;

public interface TodoNotificationRepository extends CrudRepository<TodoNotification, Long> {

    @Query(value = "SELECT * FROM todo_notifications WHERE notification_time < ?", nativeQuery = true)
    Set<TodoNotification> findAllBeforeTimeLimit(LocalDateTime timeLimit);

    @Modifying
    @Query(value = "DELETE FROM todo_notifications WHERE notification_time < ?", nativeQuery = true)
    void removeAllBeforeTimeLimit(LocalDateTime timeLimit);
}
