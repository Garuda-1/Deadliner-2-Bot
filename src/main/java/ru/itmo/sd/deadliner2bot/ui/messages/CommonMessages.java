package ru.itmo.sd.deadliner2bot.ui.messages;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

@Component
public class CommonMessages {

    private Properties commonMessagesProperties;

    @PostConstruct
    public void postConstruct() throws IOException {
        Resource resource = new ClassPathResource("messages/common-messages.properties");
        commonMessagesProperties = PropertiesLoaderUtils.loadProperties(resource);
    }

    public String getByKey(String key) {
        return commonMessagesProperties.getProperty(key);
    }

    public String getByKey(String key, Object... arguments) {
        return MessageFormat.format(commonMessagesProperties.getProperty(key), arguments);
    }
}
