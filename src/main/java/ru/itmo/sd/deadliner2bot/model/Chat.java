package ru.itmo.sd.deadliner2bot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.itmo.sd.deadliner2bot.model.converter.LanguageCodeToLocaleConverter;

import javax.persistence.*;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "chats")
public class Chat {

    @Id
    @Column(name = "chat_id", nullable = false)
    private long chatId;

    @Column(name = "state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ChatStateEnum state;

    @Column(name = "language_code", nullable = false)
    @Convert(converter = LanguageCodeToLocaleConverter.class)
    private Locale languageCode = Locale.ROOT;

    @OneToMany(mappedBy = "chat")
    private Set<Todo> todos = new TreeSet<>();

    @OneToMany(mappedBy = "chat")
    private Set<DailyNotification> dailyNotifications = new TreeSet<>();
}
