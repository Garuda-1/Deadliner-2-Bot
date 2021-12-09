package ru.itmo.sd.deadliner2bot.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "todos")
public class Todo implements Comparable<Todo> {

    @Id
    @GeneratedValue
    @Column(name = "todo_id")
    private long todoId;

    @Column(name = "name")
    private String name;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "completed")
    private boolean completed;

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
