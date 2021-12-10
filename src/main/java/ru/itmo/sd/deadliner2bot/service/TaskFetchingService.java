package ru.itmo.sd.deadliner2bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import ru.itmo.sd.deadliner2bot.bot.Bot;
import ru.itmo.sd.deadliner2bot.dto.TodoDto;
import ru.itmo.sd.deadliner2bot.messages.MessageFormatter;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.model.TodoNotification;
import ru.itmo.sd.deadliner2bot.repository.DailyNotificationRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoNotificationRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoRepository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskFetchingService {

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final DailyNotificationRepository dailyNotificationRepository;
    private final TodoNotificationRepository todoNotificationRepository;
    private final TodoRepository todoRepository;
    private final MessageFormatter messageFormatter;
    private final Bot bot;

    @Value("${notifications.stored-range-sec}")
    private int storedRangeSec;

    @Async
    @Transactional
    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void doScheduledJob() {
        fetchNotifications();
    }

    public void fetchNotifications() {
        log.info("Fetching notifications...");
        LocalDateTime timeLimit = LocalDateTime.now().plusSeconds(storedRangeSec);

        Set<DailyNotification> dailyNotifications = getDailyNotifications(timeLimit);
        dailyNotifications.forEach(dailyNotification ->
                threadPoolTaskScheduler.schedule(new DailyNotificationRunnable(
                                dailyNotification.getChat().getChatId(),
                                dailyNotification.getNotificationTime()),
                        Timestamp.valueOf(dailyNotification.getNotificationTime())));
        log.info("Scheduled {} daily notifications", dailyNotifications.size());

        Set<TodoNotification> todoNotifications = getTodoNotifications(timeLimit);
        todoNotifications.forEach(todoNotification ->
                threadPoolTaskScheduler.schedule(new TodoNotificationRunnable(
                                todoNotification.getTodo().getChat().getChatId(),
                                TodoDto.builder()
                                        .name(todoNotification.getTodo().getName())
                                        .endTime(todoNotification.getTodo().getEndTime())
                                        .build()),
                        Timestamp.valueOf(todoNotification.getNotificationTime())));
        log.info("Scheduled {} todo notifications", todoNotifications.size());

        dailyNotificationRepository.removeAllBeforeTimeLimit(timeLimit);
        todoNotificationRepository.removeAllBeforeTimeLimit(timeLimit);
        log.debug("Purged obsolete notifications");
    }

    Set<DailyNotification> getDailyNotifications(LocalDateTime timeLimit) {
        return dailyNotificationRepository.findAllBeforeTimeLimit(timeLimit);
    }

    Set<TodoNotification> getTodoNotifications(LocalDateTime timeLimit) {
        return todoNotificationRepository.findAllBeforeTimeLimit(timeLimit);
    }

    @RequiredArgsConstructor
    class DailyNotificationRunnable implements Runnable {

        private final long chatId;
        private final LocalDateTime notificationTime;

        @Override
        public void run() {
            log.debug("Sending daily notification for chat {}", chatId);
            Set<Todo> todos = todoRepository.findAllTodosForDailyNotificationByChatId(chatId, notificationTime);
            List<TodoDto> todoDtos = todos.stream()
                    .map(todo -> TodoDto.builder()
                            .name(todo.getName())
                            .startTime(todo.getStartTime())
                            .endTime(todo.getEndTime())
                            .build())
                    .collect(Collectors.toList());
            String message = messageFormatter.dailyNotificationMessage(todoDtos);
            bot.sendMessage(chatId, message);
        }
    }

    @RequiredArgsConstructor
    class TodoNotificationRunnable implements Runnable {

        private final long chatId;
        private final TodoDto todoDto;

        @Override
        public void run() {
            log.debug("Sending todo notification for chat {}", chatId);
            String message = messageFormatter.todoNotificationMessage(todoDto);
            bot.sendMessage(chatId, message);
        }
    }
}
