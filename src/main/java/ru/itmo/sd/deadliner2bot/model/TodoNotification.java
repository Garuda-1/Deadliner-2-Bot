package ru.itmo.sd.deadliner2bot.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_notifications")
public class TodoNotification implements Comparable<TodoNotification> {

    @Id
    @GeneratedValue
    @Column(name = "todo_notification_id")
    private long todoNotificationId;

    @Column(name = "notification_time")
    private LocalDateTime notificationTime;

    @ManyToOne
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @Override
    public int compareTo(TodoNotification other) {
        return notificationTime.compareTo(other.notificationTime);
    }
}
