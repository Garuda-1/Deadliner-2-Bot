package ru.itmo.sd.deadliner2bot.configuration;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.itmo.sd.deadliner2bot.bot.Bot;
import ru.itmo.sd.deadliner2bot.repository.DailyNotificationRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoNotificationRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoRepository;
import ru.itmo.sd.deadliner2bot.service.TaskFetchingService;
import ru.itmo.sd.deadliner2bot.ui.messages.ExposedResourceBundleMessageSource;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageFormatter;
import ru.itmo.sd.deadliner2bot.ui.messages.MessageSourceUtils;
import ru.itmo.sd.deadliner2bot.utils.chrono.DateTimeUtils;

@TestConfiguration
public class ServiceTestConfiguration {

    @Bean
    public Bot bot() {
        return Mockito.mock(Bot.class);
    }

    @Bean
    public DateTimeUtils dateTimeUtils() {
        return new DateTimeUtils();
    }

    @Bean
    public ExposedResourceBundleMessageSource exposedResourceBundleMessageSource() {
        ExposedResourceBundleMessageSource messageSource = new ExposedResourceBundleMessageSource();
        messageSource.setBasenames("classpath:/messages/messages");
        return messageSource;
    }

    @Bean
    public MessageSourceUtils messageUtils(ExposedResourceBundleMessageSource exposedResourceBundleMessageSource) {
        return new MessageSourceUtils(exposedResourceBundleMessageSource);
    }

    @Bean
    public MessageFormatter messageFormatter(ExposedResourceBundleMessageSource exposedResourceBundleMessageSource,
                                             MessageSourceUtils messageSourceUtils,
                                             DateTimeUtils dateTimeUtils) {
        return new MessageFormatter(exposedResourceBundleMessageSource, messageSourceUtils, dateTimeUtils);
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
                                                   DateTimeUtils dateTimeUtils,
                                                   Bot bot) {
        return new TaskFetchingService(
                threadPoolTaskScheduler,
                dailyNotificationRepository,
                todoNotificationRepository,
                todoRepository,
                messageFormatter,
                dateTimeUtils,
                bot
        );
    }
}
