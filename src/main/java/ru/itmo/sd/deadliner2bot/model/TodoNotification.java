package ru.itmo.sd.deadliner2bot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
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
