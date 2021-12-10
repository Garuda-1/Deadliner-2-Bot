package ru.itmo.sd.deadliner2bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Deadliner2BotApplication {

    public static void main(String[] args) {
        SpringApplication.run(Deadliner2BotApplication.class, args);
    }
}
