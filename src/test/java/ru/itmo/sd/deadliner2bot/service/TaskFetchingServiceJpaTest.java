package ru.itmo.sd.deadliner2bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.itmo.sd.deadliner2bot.bot.Bot;
import ru.itmo.sd.deadliner2bot.configuration.ServiceTestConfiguration;
import ru.itmo.sd.deadliner2bot.model.*;
import ru.itmo.sd.deadliner2bot.repository.*;
import ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.boot.jdbc.EmbeddedDatabaseConnection.H2;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.ANY;

@DataJpaTest
@AutoConfigureTestDatabase(replace = ANY, connection = H2)
@ContextConfiguration(classes = ServiceTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskFetchingServiceJpaTest {

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private TodoNotificationRepository todoNotificationRepository;
    @Autowired
    private DailyNotificationRepository dailyNotificationRepository;
    @Autowired
    private TaskFetchingService taskFetchingService;
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private DateTimeUtils dateTimeUtils;
    @Autowired
    private Bot bot;

    @Value("${notifications.stored-range-sec}")
    private int storedRangeSec;

    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        now = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
    }

    @Test
    @DisplayName(value = "Daily notifications in range fetched")
    public void dailyNotificationInRangeFetched() {
        Chat chat = createChat();
        DailyNotification dailyNotification = createDailyNotification(chat, now.plusSeconds(storedRangeSec / 2));

        Set<DailyNotification> dailyNotifications = taskFetchingService
                .getDailyNotifications(now, now.plusSeconds(storedRangeSec));
        assertThat(dailyNotifications).contains(dailyNotification);
        Set<TodoNotification> todoNotifications = taskFetchingService
                .getTodoNotifications(now.plusSeconds(storedRangeSec));
        assertThat(todoNotifications).isEmpty();
    }

    @Test
    @DisplayName(value = "Daily notifications in range week later fetched")
    public void dailyNotificationInRangeWeekLaterFetched() {
        Chat chat = createChat();
        DailyNotification dailyNotification = createDailyNotification(chat, now.plusSeconds(storedRangeSec / 2));

        now = now.plusWeeks(1);
        Set<DailyNotification> dailyNotifications = taskFetchingService
                .getDailyNotifications(now, now.plusSeconds(storedRangeSec));
        assertThat(dailyNotifications).contains(dailyNotification);
        Set<TodoNotification> todoNotifications = taskFetchingService
                .getTodoNotifications(now.plusSeconds(storedRangeSec));
        assertThat(todoNotifications).isEmpty();
    }

    @Test
    @DisplayName(value = "Daily notifications out of range not fetched")
    public void dailyNotificationOutOfRangeNotFetched() {
        Chat chat = createChat();
        createDailyNotification(chat, now.plusSeconds(2L * storedRangeSec));

        Set<DailyNotification> dailyNotifications = taskFetchingService
                .getDailyNotifications(now, now.plusSeconds(storedRangeSec));
        assertThat(dailyNotifications).isEmpty();
        Set<TodoNotification> todoNotifications = taskFetchingService
                .getTodoNotifications(now.plusSeconds(storedRangeSec));
        assertThat(todoNotifications).isEmpty();
    }

    @Test
    @DisplayName(value = "Daily notifications on other day of week not fetched")
    public void dailyNotificationOnOtherDayOfWeekNotFetched() {
        Chat chat = createChat();
        createDailyNotification(chat, now.plusDays(1));

        Set<DailyNotification> dailyNotifications = taskFetchingService
                .getDailyNotifications(now, now.plusSeconds(storedRangeSec));
        assertThat(dailyNotifications).isEmpty();
        Set<TodoNotification> todoNotifications = taskFetchingService
                .getTodoNotifications(now.plusSeconds(storedRangeSec));
        assertThat(todoNotifications).isEmpty();
    }

    @Test
    @DisplayName(value = "TODO notifications in range fetched")
    public void todoNotificationInRangeNotCompletedFetched() {
        Chat chat = createChat();
        TodoNotification todoNotification = createTodoWithNotification(chat, now.plusSeconds(storedRangeSec / 2));

        Set<DailyNotification> dailyNotifications = taskFetchingService
                .getDailyNotifications(now, now.plusSeconds(storedRangeSec));
        assertThat(dailyNotifications).isEmpty();
        Set<TodoNotification> todoNotifications = taskFetchingService
                .getTodoNotifications(now.plusSeconds(storedRangeSec));
        assertThat(todoNotifications).contains(todoNotification);
    }

    @Test
    @DisplayName(value = "TODO notifications out of range not fetched")
    public void todoNotificationNotInRangeNotFetched() {
        Chat chat = createChat();
        createTodoWithNotification(chat, now.plusSeconds(2L * storedRangeSec));

        Set<DailyNotification> dailyNotifications = taskFetchingService
                .getDailyNotifications(now, now.plusSeconds(storedRangeSec));
        assertThat(dailyNotifications).isEmpty();
        Set<TodoNotification> todoNotifications = taskFetchingService
                .getTodoNotifications(now.plusSeconds(storedRangeSec));
        assertThat(todoNotifications).isEmpty();
    }

    @Test
    @DisplayName(value = "Daily notification added in scheduled thread pool")
    public void dailyNotificationAddedInScheduledThreadPool() {
        Chat chat = createChat();
        LocalDateTime notificationTime = now.plusSeconds(storedRangeSec / 2);
        createDailyNotification(chat, notificationTime);

        taskFetchingService.fetchNotifications(now);

        ArgumentCaptor<TaskFetchingService.DailyNotificationRunnable> argumentCaptor =
                ArgumentCaptor.forClass(TaskFetchingService.DailyNotificationRunnable.class);
        verify(threadPoolTaskScheduler, times(1))
                .schedule(argumentCaptor.capture(),
                        eq(Timestamp.valueOf(notificationTime)));

        TaskFetchingService.DailyNotificationRunnable runnable = argumentCaptor.getValue();
        runnable.run();
        verify(bot, times(1)).sendMarkdownMessage(any());
    }

    @Test
    @DisplayName(value = "TODO notification added in scheduled thread pool")
    public void todoNotificationAddedInScheduledThreadPool() {
        Chat chat = createChat();
        LocalDateTime notificationTime = now.plusSeconds(storedRangeSec / 2);
        createTodoWithNotification(chat, notificationTime);

        taskFetchingService.fetchNotifications(now);

        ArgumentCaptor<TaskFetchingService.TodoNotificationRunnable> argumentCaptor =
                ArgumentCaptor.forClass(TaskFetchingService.TodoNotificationRunnable.class);
        verify(threadPoolTaskScheduler, times(1))
                .schedule(argumentCaptor.capture(),
                        eq(Timestamp.valueOf(dateTimeUtils.toRealDateTime(notificationTime))));

        TaskFetchingService.TodoNotificationRunnable runnable = argumentCaptor.getValue();
        runnable.run();
        verify(bot, times(1)).sendMarkdownMessage(any());
        assertThat(todoNotificationRepository.findAllBeforeTimeLimit(now.plusSeconds(storedRangeSec))).isEmpty();
    }

    private Chat createChat() {
        Chat chat = new Chat();
        chat.setChatId(1);
        chat.setState(ChatStateEnum.BASE_STATE);
        return chatRepository.save(chat);
    }

    private DailyNotification createDailyNotification(Chat chat, LocalDateTime notificationTime) {
        DailyNotification dailyNotification = new DailyNotification();
        dailyNotification.setDailyNotificationId(1);
        dailyNotification.setNotificationTime(notificationTime);
        dailyNotification.setChat(chat);
        return dailyNotificationRepository.save(dailyNotification);
    }

    private TodoNotification createTodoWithNotification(Chat chat, LocalDateTime notificationTime) {
        Todo todo = new Todo();
        todo.setTodoId(1);
        todo.setName("foo");
        todo.setChat(chat);
        todo = todoRepository.save(todo);

        TodoNotification todoNotification = new TodoNotification();
        todoNotification.setTodoNotificationId(1);
        todoNotification.setTodo(todo);
        todoNotification.setNotificationTime(notificationTime);
        return todoNotificationRepository.save(todoNotification);
    }
}
