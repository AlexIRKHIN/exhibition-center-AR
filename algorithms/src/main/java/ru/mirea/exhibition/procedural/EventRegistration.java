package ru.mirea.exhibition.procedural;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Алгоритм 2. Регистрация событий взаимодействия посетителей.
 * <p>
 * Стенд передаёт пакет событий; каждое событие проверяется, корректные
 * сохраняются для статистики, ошибочные отклоняются с причиной.
 * Формат: event_id;stand_id;exhibit_id;type;start_time;duration_sec
 */
public final class EventRegistration {

    public static final String OK = "OK";
    /** Нарушен формат записи. */
    public static final String BAD_FORMAT = "BAD_FORMAT";
    /** Стенд не зарегистрирован. */
    public static final String UNKNOWN_STAND = "UNKNOWN_STAND";
    /** Экспонат не размещён на этом стенде. */
    public static final String NOT_PLACED = "NOT_PLACED";
    /** Недопустимый тип события. */
    public static final String BAD_TYPE = "BAD_TYPE";
    /** Время начала позже текущего. */
    public static final String FUTURE_TIME = "FUTURE_TIME";
    /** Длительность вне допустимых границ. */
    public static final String BAD_DURATION = "BAD_DURATION";
    /** Событие уже было принято. */
    public static final String DUPLICATE = "DUPLICATE";

    public static final Set<String> EVENT_TYPES =
            Set.of("OPEN", "MODEL_3D", "VIDEO");
    public static final int MIN_DURATION_SEC = 1;
    public static final int MAX_DURATION_SEC = 3600;

    private EventRegistration() {
    }

    public static String placementKey(String standId, String exhibitId) {
        return standId + "/" + exhibitId;
    }

    /** Множество id стендов из строк stands.csv (id;name;hall;type). */
    public static Set<String> buildStandIds(List<String[]> stands) {
        Set<String> ids = new HashSet<>();
        for (String[] stand : stands) {
            ids.add(stand[0]);
        }
        return ids;
    }

    /** Множество пар «стенд/экспонат» из строк placements.csv. */
    public static Set<String> buildPlacementKeys(
            List<String[]> placements) {
        Set<String> keys = new HashSet<>();
        for (String[] placement : placements) {
            keys.add(placementKey(placement[0], placement[1]));
        }
        return keys;
    }

    /**
     * Проверяет одно событие. Корректное событие добавляет свой id
     * в acceptedIds.
     *
     * @return {@link #OK} или код причины отклонения
     */
    public static String validate(String[] event, Set<String> standIds,
                                  Set<String> placementKeys,
                                  Set<String> acceptedIds,
                                  LocalDateTime now) {
        if (event.length != 6) {
            return BAD_FORMAT;
        }
        LocalDateTime start;
        int duration;
        try {
            Integer.parseInt(event[2]);
            start = LocalDateTime.parse(event[4]);
            duration = Integer.parseInt(event[5]);
        } catch (RuntimeException e) {
            return BAD_FORMAT;
        }
        if (!standIds.contains(event[1])) {
            return UNKNOWN_STAND;
        }
        if (!placementKeys.contains(placementKey(event[1], event[2]))) {
            return NOT_PLACED;
        }
        if (!EVENT_TYPES.contains(event[3])) {
            return BAD_TYPE;
        }
        if (start.isAfter(now)) {
            return FUTURE_TIME;
        }
        if (duration < MIN_DURATION_SEC || duration > MAX_DURATION_SEC) {
            return BAD_DURATION;
        }
        if (!acceptedIds.add(event[0])) {
            return DUPLICATE;
        }
        return OK;
    }

    /**
     * Обрабатывает пакет событий.
     *
     * @param rejected сюда добавляются строки вида «E012 — UNKNOWN_STAND»
     * @return принятые события в исходном порядке
     */
    public static List<String[]> registerAll(List<String[]> events,
                                             Set<String> standIds,
                                             Set<String> placementKeys,
                                             LocalDateTime now,
                                             List<String> rejected) {
        List<String[]> accepted = new ArrayList<>();
        Set<String> acceptedIds = new HashSet<>();
        for (String[] event : events) {
            String status = validate(event, standIds, placementKeys,
                    acceptedIds, now);
            if (OK.equals(status)) {
                accepted.add(event);
            } else {
                rejected.add(event[0] + " — " + status);
            }
        }
        return accepted;
    }
}
