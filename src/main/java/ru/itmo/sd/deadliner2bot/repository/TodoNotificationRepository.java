package ru.itmo.sd.deadliner2bot.repository;

import org.springframework.data.repository.CrudRepository;
import ru.itmo.sd.deadliner2bot.model.TodoNotification;

public interface TodoNotificationRepository extends CrudRepository<TodoNotification, Long> {
}
