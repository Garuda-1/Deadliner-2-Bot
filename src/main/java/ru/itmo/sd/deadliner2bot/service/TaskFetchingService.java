package ru.itmo.sd.deadliner2bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import ru.itmo.sd.deadliner2bot.bot.Bot;
import ru.itmo.sd.deadliner2bot.model.DailyNotification;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.model.TodoNotification;
import ru.itmo.sd.deadliner2bot.repository.DailyNotificationRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoNotificationRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoRepository;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageFormatter;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

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
    @Scheduled(cron = "0 * * * * *")
    public void doScheduledJob() {
        fetchNotifications(LocalDateTime.now());
    }

    public void fetchNotifications(LocalDateTime now) {
        log.info("Fetching notifications...");
        LocalDateTime limit = now.plusSeconds(storedRangeSec);

        Set<DailyNotification> dailyNotifications = getDailyNotifications(now, limit);
        dailyNotifications.forEach(dailyNotification ->
                threadPoolTaskScheduler.schedule(new DailyNotificationRunnable(
                                dailyNotification.getChat().getChatId(),
                                dailyNotification.getNotificationTime()),
                        Timestamp.valueOf(dailyNotification.getNotificationTime())));
        log.info("Scheduled {} daily notifications", dailyNotifications.size());

        Set<TodoNotification> todoNotifications = getTodoNotifications(limit);
        todoNotifications.forEach(todoNotification ->
                threadPoolTaskScheduler.schedule(new TodoNotificationRunnable(todoNotification),
                        Timestamp.valueOf(todoNotification.getNotificationTime())));
        log.info("Scheduled {} todo notifications", todoNotifications.size());
    }

    Set<DailyNotification> getDailyNotifications(LocalDateTime now, LocalDateTime limit) {
        return dailyNotificationRepository.findAllBeforeTimeLimitOnCurrentWeekday(now, limit);
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
            String message = messageFormatter.notCompletedTodos(new ArrayList<>(todos));
            bot.sendMarkdownMessage(chatId, message);
        }
    }

    @RequiredArgsConstructor
    class TodoNotificationRunnable implements Runnable {

        private final TodoNotification todoNotification;

        @Override
        public void run() {
            Todo todo = todoNotification.getTodo();
            long chatId = todo.getChat().getChatId();
            log.debug("Sending todo notification for chat {}", chatId);
            String message = messageFormatter.todoNotificationMessage(todo);
            todoNotificationRepository.delete(todoNotification);
            bot.sendMarkdownMessage(chatId, message);
        }
    }
}
