package ru.itmo.sd.deadliner2bot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "chats")
public class Chat {

    @Id
    @Column(name = "chat_id")
    private long chatId;

    @OneToMany(mappedBy = "chat")
    private Set<Todo> todos = new TreeSet<>();

    @OneToMany(mappedBy = "chat")
    private Set<DailyNotification> dailyNotifications = new TreeSet<>();
}
