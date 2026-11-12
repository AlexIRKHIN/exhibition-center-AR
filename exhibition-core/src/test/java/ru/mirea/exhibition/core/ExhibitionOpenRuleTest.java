package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.mirea.exhibition.core.TestData.event;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.mirea.exhibition.core.registration.EventRegistrationService;
import ru.mirea.exhibition.core.registration.InMemoryEventStore;
import ru.mirea.exhibition.core.registration.RegistrationResult;
import ru.mirea.exhibition.core.registration.RejectionReason;

/** Новое требование из подраздела 3.4: только открытая выставка. */
class ExhibitionOpenRuleTest {

    @Test
    @DisplayName("Обращения во время пусконаладки до открытия выставки"
            + " не учитываются")
    void rejectsEventsBeforeOpening() {
        EventRegistrationService service = EventRegistrationService
                .standard(TestData.catalog(), new InMemoryEventStore(),
                        TestData.clockAt("2026-10-27T18:00:00"));
        RegistrationResult r = service.register(List.of(
                event("G1", "S1", 1, "OPEN", "2026-10-15T15:00:00", 30),
                event("G2", "S1", 1, "OPEN", "2026-10-16T10:00:00", 30)));
        assertEquals(RejectionReason.EXHIBITION_CLOSED,
                r.rejected().get(0).reason());
        assertEquals("G2", r.accepted().get(0).id());
    }
}
