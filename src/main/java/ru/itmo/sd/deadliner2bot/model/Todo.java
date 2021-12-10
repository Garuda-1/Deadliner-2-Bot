package ru.itmo.sd.deadliner2bot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "todos")
public class Todo implements Comparable<Todo> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id", nullable = false)
    private long todoId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "daily_notifications_enabled", nullable = false)
    private boolean dailyNotificationsEnabled = false;

    @OneToMany(mappedBy = "todo")
    private Set<TodoNotification> todoNotifications = new TreeSet<>();

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Override
    public int compareTo(Todo other) {
        return endTime.compareTo(other.endTime);
    }
}
