package ru.itmo.sd.deadliner2bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern(dateFormat)
            .optionalStart()
            .appendPattern(" " + timeFormat)
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();
    public static final LocalDateTime startDateUnconfirmed = LocalDateTime.of(2028, 12, 31, 23, 59);
    public static final LocalDateTime startDateConfirmed = LocalDateTime.of(1995, 12, 31, 23, 59);

    public static LocalDateTime parseDateTime(String dateTimeString) {
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

    public static LocalDateTime getDateTimeFromDayUnconfirmed(DayOfWeek dayOfWeek) {
        return startDateUnconfirmed.with(TemporalAdjusters.next(dayOfWeek));
    }

    public static LocalDateTime setTimeAndConfirmed(LocalDateTime date, LocalTime time) {
        DayOfWeek day = date.getDayOfWeek();
        date = startDateConfirmed.with(TemporalAdjusters.next(day));
        date = date.plusHours(time.getHour());
        date = date.plusMinutes(time.getMinute());
        return date;
    }

    public static LocalDateTime parseDate(String dateString) {
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

    public static LocalTime parseTime(String timeString) {
        timeString = deleteSpaces(timeString);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
        try {
            return LocalTime.parse(timeString, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static Set<DayOfWeek> parseDaysOfWeek(String message) {
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

    private static String deleteSpaces(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isWhitespace(input.charAt(i))) {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }
}
