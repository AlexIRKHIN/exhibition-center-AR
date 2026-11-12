package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.mirea.exhibition.core.TestData.event;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.mirea.exhibition.core.registration.EventRegistrationService;
import ru.mirea.exhibition.core.registration.InMemoryEventStore;
import ru.mirea.exhibition.core.registration.RegisteredEvent;
import ru.mirea.exhibition.core.registration.RegistrationResult;
import ru.mirea.exhibition.core.registration.RejectedEvent;

class EventRegistrationServiceTest {

    private final InMemoryEventStore store = new InMemoryEventStore();
    private final EventRegistrationService service =
            EventRegistrationService.standard(TestData.catalog(), store,
                    TestData.clockAt("2026-10-27T18:00:00"));

    private static List<String> reasons(RegistrationResult result) {
        List<String> lines = new ArrayList<>();
        for (RejectedEvent e : result.rejected()) {
            lines.add(e.eventId() + " " + e.reason());
        }
        return lines;
    }

    @Test
    @DisplayName("Событие привязывается к версии на момент начала,"
            + " в том числе в момент смены версии")
    void bindsVersionAtEventStart() {
        RegistrationResult r = service.register(List.of(
                event("A1", "S1", 1, "OPEN", "2026-10-20T10:00:00", 30),
                event("A2", "S1", 1, "VIDEO", "2026-10-21T08:00:00", 60)));
        assertEquals(2, r.accepted().size());
        RegisteredEvent first = r.accepted().get(0);
        assertEquals(1, first.placement().id());
        assertEquals(11, first.version().id());
        assertEquals(12, r.accepted().get(1).version().id());
        assertEquals(2, store.findAll().size());
    }

    @Test
    @DisplayName("Запоздавшее событие о снятом экспонате принимается,"
            + " а событие после снятия — нет")
    void delayedEventAboutRemovedExhibit() {
        RegistrationResult r = service.register(List.of(
                event("B1", "S1", 2, "OPEN", "2026-10-20T14:00:00", 40),
                event("B2", "S1", 2, "OPEN", "2026-10-22T10:00:00", 40),
                event("B3", "S2", 2, "OPEN", "2026-10-22T08:00:00", 40)));
        assertEquals(2, r.accepted().get(0).placement().id());
        assertEquals(List.of("B2 NOT_PLACED", "B3 NOT_PLACED"),
                reasons(r));
    }

    @Test
    @DisplayName("События о черновике и снятой публикации отклоняются")
    void draftAndWithdrawn() {
        RegistrationResult r = service.register(List.of(
                event("C1", "S1", 3, "OPEN", "2026-10-20T10:00:00", 30),
                event("C2", "S1", 4, "OPEN", "2026-10-24T10:00:00", 30),
                event("C3", "S1", 4, "OPEN", "2026-10-22T10:00:00", 30)));
        assertEquals(List.of("C1 NOT_PUBLISHED", "C2 NOT_PUBLISHED"),
                reasons(r));
        assertEquals("C3", r.accepted().get(0).id());
    }

    @Test
    @DisplayName("Причины отклонения по формату, стенду, типу, времени"
            + " и границам длительности")
    void simpleRules() {
        String t = "2026-10-20T10:00:00";
        RegistrationResult r = service.register(List.of(
                new String[]{"D1", "S1", "1", "OPEN", t},
                event("D2", "S1", 1, "OPEN", "вчера", 30),
                event("D3", "S9", 1, "OPEN", t, 30),
                event("D4", "S1", 1, "SWIPE", t, 30),
                event("D5", "S1", 1, "OPEN", "2027-01-15T10:00:00", 30),
                event("D6", "S1", 1, "OPEN", t, 0),
                event("D7", "S1", 1, "OPEN", t, 3601),
                event("D8", "S1", 1, "OPEN", t, 1),
                event("D9", "S1", 1, "OPEN", t, 3600)));
        assertEquals(List.of("D1 BAD_FORMAT", "D2 BAD_FORMAT",
                "D3 UNKNOWN_STAND", "D4 BAD_TYPE", "D5 FUTURE_TIME",
                "D6 BAD_DURATION", "D7 BAD_DURATION"), reasons(r));
        assertEquals(2, r.accepted().size());
    }

    @Test
    @DisplayName("Повтор события отклоняется и в пакете, и в следующем"
            + " пакете")
    void duplicates() {
        String[] e =
                event("E1", "S1", 1, "OPEN", "2026-10-20T10:00:00", 30);
        RegistrationResult first = service.register(List.of(e, e));
        assertEquals(List.of("E1 DUPLICATE"), reasons(first));
        RegistrationResult second = service.register(List.<String[]>of(e));
        assertEquals(List.of("E1 DUPLICATE"), reasons(second));
        assertEquals(1, store.findAll().size());
    }

    @Test
    @DisplayName("Событие отклоняется по первому невыполненному правилу")
    void firstViolatedRuleWins() {
        RegistrationResult r = service.register(List.<String[]>of(
                event("F1", "S9", 3, "SWIPE", "2027-01-01T00:00:00", 0)));
        assertEquals(List.of("F1 UNKNOWN_STAND"), reasons(r));
    }
}
