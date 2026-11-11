package ru.mirea.exhibition.procedural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EventRegistrationTest {

    private static final LocalDateTime NOW =
            LocalDateTime.parse("2026-10-27T18:00:00");
    private static final Set<String> STANDS = Set.of("ST-01", "ST-02");

    /** Экспонат 3 стоял на ST-01 до 21.10 18:00; 6 — размещённый черновик. */
    private static final List<String[]> PLACEMENTS = List.of(
            new String[]{"1", "ST-01", "1", "1", "2026-10-15T09:00:00", ""},
            new String[]{"3", "ST-01", "3", "2", "2026-10-15T09:00:00",
                    "2026-10-21T18:00:00"},
            new String[]{"5", "ST-01", "6", "3", "2026-10-15T09:00:00", ""},
            new String[]{"7", "ST-02", "5", "1", "2026-10-15T09:00:00", ""},
            new String[]{"11", "ST-02", "9", "2", "2026-10-15T09:00:00", ""});

    /** У экспоната 1 версия 2 сменила версию 1 20.10 в 12:00. */
    private static final List<String[]> VERSIONS = List.of(
            new String[]{"100", "1", "1", "2026-10-15T09:00:00",
                    "2026-10-20T12:00:00"},
            new String[]{"101", "1", "2", "2026-10-20T12:00:00", ""},
            new String[]{"104", "3", "1", "2026-10-15T09:00:00", ""},
            new String[]{"107", "5", "1", "2026-10-15T09:00:00",
                    "2026-10-23T12:00:00"},
            new String[]{"108", "6", "1", "", ""},
            new String[]{"111", "9", "1", "2026-10-24T09:00:00", ""});

    private static String check(String line, Set<String> acceptedIds,
                                String[] link) {
        return EventRegistration.validate(line.split(";", -1), STANDS,
                Periods.placementHistory(PLACEMENTS),
                Periods.publicationHistory(VERSIONS), acceptedIds, NOW, link);
    }

    private static String check(String line) {
        return check(line, new HashSet<>(), new String[2]);
    }

    @Test
    @DisplayName("Событие привязывается к версии, действовавшей в момент начала")
    void linksEventToVersionAtStart() {
        String[] link = new String[2];
        assertEquals(EventRegistration.OK, check(
                "E1;ST-01;1;OPEN;2026-10-20T11:59:00;45", new HashSet<>(),
                link));
        assertArrayEquals(new String[]{"1", "100"}, link);
        assertEquals(EventRegistration.OK, check(
                "E2;ST-01;1;OPEN;2026-10-20T12:00:00;45", new HashSet<>(),
                link));
        assertArrayEquals(new String[]{"1", "101"}, link);
    }

    @Test
    @DisplayName("Запоздавшее событие о снятом экспонате принимается")
    void acceptsDelayedEventForRemovedExhibit() {
        String[] link = new String[2];
        assertEquals(EventRegistration.OK, check(
                "E3;ST-01;3;OPEN;2026-10-20T14:00:00;40", new HashSet<>(),
                link));
        assertArrayEquals(new String[]{"3", "104"}, link);
    }

    @Test
    @DisplayName("Черновик, снятый экспонат и снятая публикация не засчитываются")
    void rejectsDraftRemovedAndWithdrawn() {
        assertEquals(EventRegistration.NOT_PUBLISHED,
                check("E4;ST-01;6;OPEN;2026-10-22T11:00:00;30"));
        assertEquals(EventRegistration.NOT_PLACED,
                check("E5;ST-01;3;OPEN;2026-10-22T10:30:00;25"));
        assertEquals(EventRegistration.NOT_PUBLISHED,
                check("E6;ST-02;5;OPEN;2026-10-23T16:00:00;25"));
        assertEquals(EventRegistration.NOT_PUBLISHED,
                check("E7;ST-02;9;OPEN;2026-10-23T15:00:00;40"));
    }

    @Test
    @DisplayName("Каждое нарушение формата и границ даёт свою причину")
    void rejectsWithReason() {
        assertEquals(EventRegistration.BAD_FORMAT,
                check("E1;ST-01;1;OPEN;вчера;45"));
        assertEquals(EventRegistration.BAD_FORMAT, check("E1;ST-01;1;OPEN"));
        assertEquals(EventRegistration.UNKNOWN_STAND,
                check("E1;ST-09;1;OPEN;2026-10-21T10:15:00;45"));
        assertEquals(EventRegistration.BAD_TYPE,
                check("E1;ST-01;1;SWIPE;2026-10-21T10:15:00;45"));
        assertEquals(EventRegistration.FUTURE_TIME,
                check("E1;ST-01;1;OPEN;2027-01-15T10:00:00;45"));
        assertEquals(EventRegistration.BAD_DURATION,
                check("E1;ST-01;1;OPEN;2026-10-21T10:15:00;0"));
        assertEquals(EventRegistration.BAD_DURATION,
                check("E1;ST-01;1;OPEN;2026-10-21T10:15:00;3601"));
        assertEquals(EventRegistration.NOT_PLACED,
                check("E1;ST-02;1;OPEN;2026-10-21T10:15:00;45"));
    }

    @Test
    @DisplayName("Границы длительности 1 и 3600 с допустимы")
    void durationBoundsAreInclusive() {
        assertEquals(EventRegistration.OK,
                check("E1;ST-01;1;OPEN;2026-10-21T10:15:00;1"));
        assertEquals(EventRegistration.OK,
                check("E2;ST-01;1;OPEN;2026-10-21T10:15:00;3600"));
    }

    @Test
    @DisplayName("Повторно переданное событие отклоняется как дубликат")
    void rejectsDuplicate() {
        Set<String> accepted = new HashSet<>();
        assertEquals(EventRegistration.OK, check(
                "E7;ST-01;1;VIDEO;2026-10-21T12:00:00;180", accepted,
                new String[2]));
        assertEquals(EventRegistration.DUPLICATE, check(
                "E7;ST-01;1;VIDEO;2026-10-21T12:00:00;180", accepted,
                new String[2]));
    }

    @Test
    @DisplayName("Пакет: к принятым событиям добавлены размещение и версия")
    void registersBatch() {
        List<String[]> batch = List.of(
                "E1;ST-01;1;OPEN;2026-10-21T10:15:00;45".split(";"),
                "E2;ST-09;1;OPEN;2026-10-21T10:16:00;45".split(";"),
                "E3;ST-01;6;OPEN;2026-10-21T10:17:00;60".split(";"));
        List<String> rejected = new ArrayList<>();

        List<String[]> accepted = EventRegistration.registerAll(batch,
                STANDS, PLACEMENTS, VERSIONS, NOW, rejected);

        assertEquals(1, accepted.size());
        assertArrayEquals(new String[]{"E1", "ST-01", "1", "OPEN",
                "2026-10-21T10:15:00", "45", "1", "101"}, accepted.get(0));
        assertEquals(List.of("E2 — UNKNOWN_STAND", "E3 — NOT_PUBLISHED"),
                rejected);
    }
}
