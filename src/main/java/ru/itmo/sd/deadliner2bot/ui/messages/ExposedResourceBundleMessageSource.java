package ru.itmo.sd.deadliner2bot.ui.messages;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExposedResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {

    public Set<String> getCommonPropertiesCodes(Predicate<String> predicate) {
        return getMergedProperties(Locale.ROOT).getProperties().keySet().stream()
                .map(Object::toString)
                .filter(predicate)
                .collect(Collectors.toUnmodifiableSet());
    }
}
