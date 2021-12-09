package ru.itmo.sd.deadliner2bot.repository;

import org.springframework.data.repository.CrudRepository;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;

public interface DailyNotificationRepository extends CrudRepository<DailyNotification, Long> {
}
