package ru.mirea.exhibition.procedural;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Алгоритм 2. Регистрация событий взаимодействия посетителей.
 * <p>
 * Стенд передаёт пакет событий, возможно с задержкой. Каждое событие
 * проверяется на момент его начала: был ли экспонат размещён на стенде
 * и была ли опубликована его версия. Принятое событие привязывается
 * к размещению и версии публикации, ошибочные отклоняются с причиной.
 * Формат: event_id;stand_id;exhibit_id;type;start_time;duration_sec
 */
public final class EventRegistration {

    public static final String OK = "OK";
    /** Нарушен формат записи. */
    public static final String BAD_FORMAT = "BAD_FORMAT";
    /** Стенд не зарегистрирован. */
    public static final String UNKNOWN_STAND = "UNKNOWN_STAND";
    /** Недопустимый тип события. */
    public static final String BAD_TYPE = "BAD_TYPE";
    /** Время начала позже текущего. */
    public static final String FUTURE_TIME = "FUTURE_TIME";
    /** Длительность вне допустимых границ. */
    public static final String BAD_DURATION = "BAD_DURATION";
    /** В момент события экспонат не был размещён на этом стенде. */
    public static final String NOT_PLACED = "NOT_PLACED";
    /** В момент события у экспоната не было опубликованной версии. */
    public static final String NOT_PUBLISHED = "NOT_PUBLISHED";
    /** Событие уже было принято. */
    public static final String DUPLICATE = "DUPLICATE";

    public static final Set<String> EVENT_TYPES =
            Set.of("OPEN", "MODEL_3D", "VIDEO");
    public static final int MIN_DURATION_SEC = 1;
    public static final int MAX_DURATION_SEC = 3600;

    private EventRegistration() {
    }

    /** Множество id стендов из строк stands.csv (id;name;hall;type). */
    public static Set<String> buildStandIds(List<String[]> stands) {
        Set<String> ids = new HashSet<>();
        for (String[] stand : stands) {
            ids.add(stand[0]);
        }
        return ids;
    }

    /**
     * Проверяет одно событие. У корректного события id добавляется
     * в acceptedIds, а в link записываются id размещения и id версии,
     * действовавших в момент начала события.
     *
     * @param placements история размещений (Periods.placementHistory)
     * @param published  история публикации (Periods.publicationHistory)
     * @param link       массив из двух элементов для результата
     * @return {@link #OK} или код причины отклонения
     */
    public static String validate(String[] event, Set<String> standIds,
                                  Map<String, List<String[]>> placements,
                                  Map<Integer, List<String[]>> published,
                                  Set<String> acceptedIds,
                                  LocalDateTime now, String[] link) {
        if (event.length != 6) {
            return BAD_FORMAT;
        }
        int exhibitId;
        LocalDateTime start;
        int duration;
        try {
            exhibitId = Integer.parseInt(event[2]);
            start = LocalDateTime.parse(event[4]);
            duration = Integer.parseInt(event[5]);
        } catch (RuntimeException e) {
            return BAD_FORMAT;
        }
        if (!standIds.contains(event[1])) {
            return UNKNOWN_STAND;
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
        String[] placement = Periods.activeAt(
                placements.get(Periods.key(event[1], event[2])),
                Periods.PL_FROM, Periods.PL_TO, start);
        if (placement == null) {
            return NOT_PLACED;
        }
        String[] version = Periods.activeAt(published.get(exhibitId),
                Periods.V_FROM, Periods.V_TO, start);
        if (version == null) {
            return NOT_PUBLISHED;
        }
        if (!acceptedIds.add(event[0])) {
            return DUPLICATE;
        }
        link[0] = placement[Periods.PL_ID];
        link[1] = version[Periods.V_ID];
        return OK;
    }

    /**
     * Обрабатывает пакет событий.
     *
     * @param rejected сюда добавляются строки вида «E012 — UNKNOWN_STAND»
     * @return принятые события в исходном порядке; к шести полям
     *         добавлены placement_id и version_id
     */
    public static List<String[]> registerAll(List<String[]> events,
                                             Set<String> standIds,
                                             List<String[]> placements,
                                             List<String[]> versions,
                                             LocalDateTime now,
                                             List<String> rejected) {
        Map<String, List<String[]>> placementHistory =
                Periods.placementHistory(placements);
        Map<Integer, List<String[]>> publicationHistory =
                Periods.publicationHistory(versions);
        List<String[]> accepted = new ArrayList<>();
        Set<String> acceptedIds = new HashSet<>();
        for (String[] event : events) {
            String[] link = new String[2];
            String status = validate(event, standIds, placementHistory,
                    publicationHistory, acceptedIds, now, link);
            if (OK.equals(status)) {
                String[] row = Arrays.copyOf(event, 8);
                row[6] = link[0];
                row[7] = link[1];
                accepted.add(row);
            } else {
                rejected.add(event[0] + " — " + status);
            }
        }
        return accepted;
    }
}
