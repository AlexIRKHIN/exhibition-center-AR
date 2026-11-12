package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.mirea.exhibition.core.TestData.at;
import static ru.mirea.exhibition.core.TestData.event;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;
import ru.mirea.exhibition.core.registration.EventRegistrationService;
import ru.mirea.exhibition.core.registration.InMemoryEventStore;
import ru.mirea.exhibition.core.registration.RegisteredEvent;
import ru.mirea.exhibition.core.stats.ExhibitStats;
import ru.mirea.exhibition.core.stats.StatisticsReport;
import ru.mirea.exhibition.core.stats.StatisticsService;

class StatisticsServiceTest {

    private final ExhibitionCatalog catalog = TestData.catalog();
    private final StatisticsService service =
            new StatisticsService(catalog);

    private List<RegisteredEvent> accepted(String[]... records) {
        return EventRegistrationService.standard(catalog,
                        new InMemoryEventStore(),
                        TestData.clockAt("2026-10-27T18:00:00"))
                .register(List.of(records)).accepted();
    }

    private static List<Integer> ids(StatisticsReport report) {
        List<Integer> ids = new ArrayList<>();
        for (ExhibitStats s : report.ranking()) {
            ids.add(s.exhibitId());
        }
        return ids;
    }

    @Test
    @DisplayName("Суммирование и порядок рейтинга при равенстве"
            + " показателей")
    void rankingOrder() {
        String t = "2026-10-20T10:00:00";
        List<RegisteredEvent> events = accepted(
                event("1", "S1", 1, "OPEN", t, 30),
                event("2", "S1", 1, "OPEN", t, 90),
                event("3", "S1", 2, "OPEN", t, 100),
                event("4", "S1", 2, "OPEN", t, 20),
                event("5", "S1", 4, "VIDEO", t, 500));
        StatisticsReport report = service.report(events,
                at("2026-10-20T12:00:00"));
        assertEquals(List.of(1, 2, 4), ids(report));
        ExhibitStats first = report.ranking().get(0);
        assertEquals(2, first.count());
        assertEquals(120, first.totalSec());
        assertEquals(60.0, first.averageSec(), 1e-9);
    }

    @Test
    @DisplayName("Невостребованные — экспонаты экспозиции без обращений;"
            + " черновик в экспозицию не входит")
    void unrequested() {
        List<RegisteredEvent> events = accepted(
                event("1", "S1", 1, "OPEN", "2026-10-20T10:00:00", 30));
        StatisticsReport report = service.report(events,
                at("2026-10-20T12:00:00"));
        assertEquals("[1, 2, 4]", report.exposition().toString());
        assertEquals(List.of(2, 4), report.unrequested());
    }

    @Test
    @DisplayName("Без событий рейтинг пуст, невостребована вся"
            + " экспозиция")
    void noEvents() {
        StatisticsReport report = service.report(List.of(),
                at("2026-10-24T12:00:00"));
        assertTrue(report.ranking().isEmpty());
        assertEquals(List.of(1, 2), report.unrequested());
    }
}
