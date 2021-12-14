package ru.itmo.sd.deadliner2bot.ui.messages;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

@Component
public class StateMessages {

    private Properties stateMessagesProperties;

    @PostConstruct
    public void postConstruct() throws IOException {
        Resource resource = new ClassPathResource("messages/state-messages.properties");
        stateMessagesProperties = PropertiesLoaderUtils.loadProperties(resource);
    }

    public String getMessageByKey(ChatStateEnum chatStateEnum, String messageKey) {
        return stateMessagesProperties.getProperty(chatStateEnum.toString() + "." + messageKey);
    }

    public String getMessageByKey(ChatStateEnum chatStateEnum, String messageKey, Object... arguments) {
        return MessageFormat.format(stateMessagesProperties.getProperty(chatStateEnum.toString() + "." + messageKey), arguments);
    }
}
