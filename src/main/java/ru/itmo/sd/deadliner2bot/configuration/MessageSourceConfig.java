package ru.itmo.sd.deadliner2bot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import ru.itmo.sd.deadliner2bot.ui.messages.ExposedResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class MessageSourceConfig {

    @Bean
    public ExposedResourceBundleMessageSource exposedResourceBundleMessageSource() {
        ExposedResourceBundleMessageSource messageSource = new ExposedResourceBundleMessageSource();
        messageSource.setBasenames("classpath:/messages/messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.US);
    }
}
