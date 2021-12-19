package ru.itmo.sd.deadliner2bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.sd.deadliner2bot.model.TodoNotification;
import ru.itmo.sd.deadliner2bot.repository.TodoNotificationRepository;

@Service
@RequiredArgsConstructor
public class TodoNotificationService {

    private final TodoNotificationRepository todoNotificationRepository;

    public void save(TodoNotification todoNotification) {
        todoNotificationRepository.save(todoNotification);
    }
}
