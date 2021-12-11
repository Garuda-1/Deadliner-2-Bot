package ru.itmo.sd.deadliner2bot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.itmo.sd.deadliner2bot.model.Todo;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface TodoRepository extends CrudRepository<Todo, Long> {

    @Query(value = "SELECT T.* FROM todos T INNER JOIN chats C on C.chat_id = T.chat_id " +
                   "WHERE C.chat_id = ?1 AND NOT T.completed AND T.daily_notifications_enabled " +
                   "AND COALESCE(T.start_time <= ?2, TRUE) AND COALESCE(?2 < T.end_time, TRUE)", nativeQuery = true)
    Set<Todo> findAllTodosForDailyNotificationByChatId(long chatId, LocalDateTime now);

    @Query(value = "SELECT T.* FROM todos T INNER JOIN chats C on C.chat_id = T.chat_id " +
                   "WHERE C.chat_id = ?1 AND NOT T.completed " +
                   "AND COALESCE(T.start_time <= ?2, TRUE) AND COALESCE(?2 < T.end_time, TRUE)", nativeQuery = true)
    Set<Todo> findAllTodosByChatId(long chatId, LocalDateTime now);

    @Query(value = "SELECT T.* FROM todos T INNER JOIN chats C on C.chat_id = T.chat_id " +
                   "WHERE C.chat_id = ?1 AND T.selected", nativeQuery = true)
    Optional<Todo> findSelectedTodoByChatId(long chatId);

    @Query(value = "SELECT T.* FROM todos T INNER JOIN chats C on C.chat_id = T.chat_id " +
                   "WHERE C.chat_id = ?1 AND T.todo_id = ?2", nativeQuery = true)
    Optional<Todo> findTodoByChatIdAndTodoId(long chatId, long todoId);

}
