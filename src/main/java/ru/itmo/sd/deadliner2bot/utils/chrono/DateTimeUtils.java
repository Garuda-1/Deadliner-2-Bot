package ru.itmo.sd.deadliner2bot.utils.chrono;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.itmo.sd.deadliner2bot.model.Chat;

import javax.annotation.Nullable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
@Slf4j
public class DateTimeUtils {

    private static final ZoneId botTimeZone = ZoneId.of("UTC+3");

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

    public LocalTime parseTime(Chat chat, String timeString) {
        try {
            return LocalTime.parse(timeString, getLocalTimeFormatter(chat));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public LocalDateTime parseDateTime(Chat chat, String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, getLocalDateTimeFormatter(chat));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public LocalDateTime parseDateTimeOptional(Chat chat, String dateTimeString) {
        LocalDateTime result = parseDateTime(chat, dateTimeString);
        if (result != null) {
            return result;
        }
        try {
            return LocalDate.parse(dateTimeString, getLocalDateFormatter(chat)).atStartOfDay();
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

    public String formatDate(Chat chat, LocalDate localDate) {
        return localDate.format(getLocalDateFormatter(chat));
    }

    public String formatTime(Chat chat, LocalTime localTime) {
        return localTime.format(getLocalTimeFormatter(chat));
    }

    @Nullable
    public String formatDateTime(Chat chat, @Nullable LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.format(getLocalDateTimeFormatter(chat));
    }

    public String getExampleDate(Chat chat) {
        return formatDate(chat, LocalDate.ofInstant(Instant.EPOCH, botTimeZone));
    }

    public String getExampleTime(Chat chat) {
        return formatTime(chat, LocalTime.ofInstant(Instant.EPOCH, botTimeZone));
    }

    public String getExampleDateTime(Chat chat) {
        return formatDateTime(chat, LocalDateTime.ofInstant(Instant.EPOCH, botTimeZone));
    }

    public LocalDateTime now() {
        return LocalDateTime.now(botTimeZone);
    }

    public LocalDateTime toRealDateTime(LocalDateTime localDateTime) {
        return ZonedDateTime.of(localDateTime, botTimeZone).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
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

    public LocalDateTime getStagingWeekAuxStartDateTime() {
        return stagingWeekAuxStartDateTime;
    }

    private DateTimeFormatter getLocalDateFormatter(Chat chat) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(chat.getLanguageCode());
    }

    private DateTimeFormatter getLocalTimeFormatter(Chat chat) {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).localizedBy(chat.getLanguageCode());
    }

    private DateTimeFormatter getLocalDateTimeFormatter(Chat chat) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).localizedBy(chat.getLanguageCode());
    }
}
