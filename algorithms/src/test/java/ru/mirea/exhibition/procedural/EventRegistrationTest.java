package ru.mirea.exhibition.procedural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventRegistrationTest {

    private static final LocalDateTime NOW = LocalDateTime.parse("2026-10-27T18:00:00");
    private static final Set<String> STANDS = Set.of("ST-01", "ST-02");
    private static final Set<String> PLACEMENTS = Set.of(
            EventRegistration.placementKey("ST-01", "1"),
            EventRegistration.placementKey("ST-02", "5"));

    private static String check(String line, Set<String> acceptedIds) {
        return EventRegistration.validate(line.split(";", -1), STANDS, PLACEMENTS, acceptedIds, NOW);
    }

    private static String check(String line) {
        return check(line, new HashSet<>());
    }

    @Test
    @DisplayName("Корректное событие принимается")
    void acceptsValidEvent() {
        assertEquals(EventRegistration.OK, check("E1;ST-01;1;OPEN;2026-10-20T10:15:00;45"));
    }

    @Test
    @DisplayName("Каждое нарушение даёт свою причину отклонения")
    void rejectsWithReason() {
        assertEquals(EventRegistration.BAD_FORMAT, check("E1;ST-01;1;OPEN;вчера;45"));
        assertEquals(EventRegistration.BAD_FORMAT, check("E1;ST-01;1;OPEN"));
        assertEquals(EventRegistration.UNKNOWN_STAND, check("E1;ST-09;1;OPEN;2026-10-20T10:15:00;45"));
        assertEquals(EventRegistration.NOT_PLACED, check("E1;ST-02;1;OPEN;2026-10-20T10:15:00;45"));
        assertEquals(EventRegistration.BAD_TYPE, check("E1;ST-01;1;SWIPE;2026-10-20T10:15:00;45"));
        assertEquals(EventRegistration.FUTURE_TIME, check("E1;ST-01;1;OPEN;2027-01-15T10:00:00;45"));
        assertEquals(EventRegistration.BAD_DURATION, check("E1;ST-01;1;OPEN;2026-10-20T10:15:00;0"));
        assertEquals(EventRegistration.BAD_DURATION, check("E1;ST-01;1;OPEN;2026-10-20T10:15:00;3601"));
    }

    @Test
    @DisplayName("Границы длительности 1 и 3600 с допустимы")
    void durationBoundsAreInclusive() {
        assertEquals(EventRegistration.OK, check("E1;ST-01;1;OPEN;2026-10-20T10:15:00;1"));
        assertEquals(EventRegistration.OK, check("E2;ST-01;1;OPEN;2026-10-20T10:15:00;3600"));
    }

    @Test
    @DisplayName("Повторно переданное событие отклоняется как дубликат")
    void rejectsDuplicate() {
        Set<String> accepted = new HashSet<>();
        assertEquals(EventRegistration.OK, check("E7;ST-01;1;VIDEO;2026-10-21T12:00:00;180", accepted));
        assertEquals(EventRegistration.DUPLICATE, check("E7;ST-01;1;VIDEO;2026-10-21T12:00:00;180", accepted));
    }

    @Test
    @DisplayName("Пакет: принятые события сохраняют порядок, отклонённые получают причину")
    void registersBatch() {
        List<String[]> batch = List.of(
                "E1;ST-01;1;OPEN;2026-10-20T10:15:00;45".split(";"),
                "E2;ST-09;1;OPEN;2026-10-20T10:16:00;45".split(";"),
                "E3;ST-02;5;MODEL_3D;2026-10-20T10:17:00;60".split(";"));
        List<String> rejected = new ArrayList<>();

        List<String[]> accepted = EventRegistration.registerAll(batch, STANDS, PLACEMENTS, NOW, rejected);

        assertEquals(2, accepted.size());
        assertEquals("E1", accepted.get(0)[0]);
        assertEquals("E3", accepted.get(1)[0]);
        assertEquals(List.of("E2 — UNKNOWN_STAND"), rejected);
    }
}
