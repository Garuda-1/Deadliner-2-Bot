package ru.itmo.sd.deadliner2bot.configuration;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.itmo.sd.deadliner2bot.bot.Bot;
import ru.itmo.sd.deadliner2bot.messages.MessageFormatter;
import ru.itmo.sd.deadliner2bot.repository.DailyNotificationRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoNotificationRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoRepository;
import ru.itmo.sd.deadliner2bot.service.TaskFetchingService;

@TestConfiguration
public class ServiceTestConfiguration {

    @Bean
    public Bot bot() {
        return Mockito.mock(Bot.class);
    }

    @Bean
    public MessageFormatter messageFormatter() {
        return new MessageFormatter();
    }

    @Bean
    public ThreadPoolTaskScheduler notificationThreadPoolTaskScheduler() {
        return Mockito.mock(ThreadPoolTaskScheduler.class);
    }

    @Bean
    public TaskFetchingService taskFetchingService(ThreadPoolTaskScheduler threadPoolTaskScheduler,
                                                   DailyNotificationRepository dailyNotificationRepository,
                                                   TodoNotificationRepository todoNotificationRepository,
                                                   TodoRepository todoRepository,
                                                   MessageFormatter messageFormatter,
                                                   Bot bot) {
        return new TaskFetchingService(
                threadPoolTaskScheduler,
                dailyNotificationRepository,
                todoNotificationRepository,
                todoRepository,
                messageFormatter,
                bot
        );
    }
}
