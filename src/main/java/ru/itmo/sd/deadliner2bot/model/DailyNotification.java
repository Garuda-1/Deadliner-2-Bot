package ru.itmo.sd.deadliner2bot.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_notifications")
public class DailyNotification implements Comparable<DailyNotification> {

    @Id
    @GeneratedValue
    @Column(name = "daily_notification_id")
    private long dailyNotificationId;

    @Column(name = "notification_time")
    private LocalDateTime notificationTime;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Override
    public int compareTo(DailyNotification other) {
        return notificationTime.compareTo(other.notificationTime);
    }
}
