package ru.mirea.exhibition.core.registration;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Событие, полученное от стенда и успешно разобранное: поля имеют
 * нужные типы, но правила предметной области ещё не проверены.
 * Формат записи: event_id;stand_id;exhibit_id;type;start_time;
 * duration_sec.
 */
public record IncomingEvent(String id, String standId, int exhibitId,
                            String typeCode, LocalDateTime start,
                            int durationSec) {

    private static final int FIELD_COUNT = 6;

    /**
     * Разбор записи стенда (проверка 1 «формат записи»).
     *
     * @return событие или пусто, если формат нарушен
     */
    public static Optional<IncomingEvent> parse(String[] fields) {
        if (fields.length != FIELD_COUNT) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IncomingEvent(fields[0], fields[1],
                    Integer.parseInt(fields[2]), fields[3],
                    LocalDateTime.parse(fields[4]),
                    Integer.parseInt(fields[5])));
        } catch (NumberFormatException | DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
