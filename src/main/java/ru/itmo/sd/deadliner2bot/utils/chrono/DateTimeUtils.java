package ru.itmo.sd.deadliner2bot.utils.chrono;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
@Slf4j
public class DateTimeUtils {

    public static final String dateFormat = "dd-MM-yyyy";
    public static final String timeFormat = "HH:mm";
    public static final String dateTimeFormat = "dd-MM-yyyy HH:mm";
    private static final ZoneId botTimeZone = ZoneId.of("UTC+3");
    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern(dateFormat)
            .optionalStart()
            .appendPattern(" " + timeFormat)
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    private LocalDateTime stagingWeekStartDateTime;
    private LocalDateTime stagingWeekAuxStartDateTime;

    @Value("${notifications.daily.staging-week}")
    private void setStagingWeekStartDateTime(String stagingWeek) {
        if (stagingWeek != null && !stagingWeek.isEmpty()) {
            stagingWeekStartDateTime = LocalDate.parse(stagingWeek).atStartOfDay().minusDays(1);
        }
    }

    @Value("${notifications.daily.staging-week-aux}")
    private void setStagingWeekAuxStartDateTime(String stagingWeekAux) {
        if (stagingWeekAux != null && !stagingWeekAux.isEmpty()) {
            stagingWeekAuxStartDateTime = LocalDate.parse(stagingWeekAux).atStartOfDay().minusDays(1);
        }
    }

    public LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString.length() > dateTimeFormat.length()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        try {
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public LocalDateTime now() {
        return LocalDateTime.now(botTimeZone);
    }

    public LocalDateTime getDateTimeFromDayUnconfirmed(DayOfWeek dayOfWeek) {
        return stagingWeekAuxStartDateTime.with(TemporalAdjusters.next(dayOfWeek));
    }

    public LocalDateTime setTimeAndConfirmed(LocalDateTime date, LocalTime time) {
        DayOfWeek day = date.getDayOfWeek();
        date = stagingWeekStartDateTime.with(TemporalAdjusters.next(day));
        date = date.plusHours(time.getHour());
        date = date.plusMinutes(time.getMinute());
        return date;
    }

    public LocalDateTime parseDate(String dateString) {
        dateString = deleteSpaces(dateString);
        if (dateString.length() > dateFormat.length()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public LocalTime parseTime(String timeString) {
        timeString = deleteSpaces(timeString);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
        try {
            return LocalTime.parse(timeString, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public Set<DayOfWeek> parseDaysOfWeek(String message) {
        String[] daysString = message.split(" +");
        DateTimeFormatter formatterShort = DateTimeFormatter.ofPattern("EEE", Locale.US);
        DateTimeFormatter formatterFull = DateTimeFormatter.ofPattern("EEEE", Locale.US);
        Set<DayOfWeek> days = new HashSet<>();
        for (String day : daysString) {
            try {
                TemporalAccessor accessor = formatterShort.parse(day);
                days.add(DayOfWeek.from(accessor));
            } catch (DateTimeParseException e) {
                try {
                    TemporalAccessor accessor = formatterFull.parse(day);
                    days.add(DayOfWeek.from(accessor));
                } catch (DateTimeParseException e1) {
                    log.debug("String: " + day + "unparsed.");
                    return null;
                }
            }
        }
        return days;
    }

    private String deleteSpaces(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isWhitespace(input.charAt(i))) {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }

    public LocalDateTime getStagingWeekAuxStartDateTime() {
        return stagingWeekAuxStartDateTime;
    }
}
