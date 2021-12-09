package ru.itmo.sd.deadliner2bot.repository;

import org.springframework.data.repository.CrudRepository;
import ru.itmo.sd.deadliner2bot.model.Todo;

public interface TodoRepository extends CrudRepository<Todo, Long> {
}
