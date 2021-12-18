package ru.itmo.sd.deadliner2bot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.itmo.sd.deadliner2bot.ui.messages.ExposedResourceBundleMessageSource;

@Configuration
public class MessageSourceConfig {

    @Bean
    public ExposedResourceBundleMessageSource exposedResourceBundleMessageSource() {
        ExposedResourceBundleMessageSource messageSource = new ExposedResourceBundleMessageSource();
        messageSource.setBasenames("classpath:/messages/messages");
        return messageSource;
    }
}
