package ru.itmo.sd.deadliner2bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.repository.TodoRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    public List<Todo> findNotCompletedTodosByChatId(long chatId, LocalDateTime now) {
        return todoRepository.findNotCompletedTodosByChatId(chatId, now)
                .stream()
                .sorted(Comparator.comparing(Todo::getEndTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public Optional<Todo> findSelectedTodoByChatId(long chatId) {
        return todoRepository.findSelectedTodoByChatId(chatId);
    }

    public Optional<Todo> findTodoByChatIdAndTodoId(long chatId, long todoId) {
        return todoRepository.findTodoByChatIdAndTodoId(chatId, todoId);
    }

    public void save(Todo todo) {
        todoRepository.save(todo);
    }
}
