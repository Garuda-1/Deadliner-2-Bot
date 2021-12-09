package ru.itmo.sd.deadliner2bot.dto;

import lombok.Builder;
import lombok.Data;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Data
@Builder
public class TodoDto {

    private String name;
    @Nullable
    private LocalDateTime startTime;
    @Nullable
    private LocalDateTime endTime;
}
