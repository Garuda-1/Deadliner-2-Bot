package ru.itmo.sd.deadliner2bot.repository;

import org.springframework.data.repository.CrudRepository;
import ru.itmo.sd.deadliner2bot.model.Chat;

public interface ChatRepository extends CrudRepository<Chat, Long> {
}
