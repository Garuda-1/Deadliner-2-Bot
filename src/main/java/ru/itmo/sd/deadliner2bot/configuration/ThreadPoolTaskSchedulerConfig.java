package ru.itmo.sd.deadliner2bot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadPoolTaskSchedulerConfig {

    @Value("${notifications.thread-pool-size}")
    private int notificationsThreadPoolSize;

    @Bean
    public ThreadPoolTaskScheduler notificationThreadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(notificationsThreadPoolSize);
        threadPoolTaskScheduler.setThreadNamePrefix(
                "notificationThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }
}
