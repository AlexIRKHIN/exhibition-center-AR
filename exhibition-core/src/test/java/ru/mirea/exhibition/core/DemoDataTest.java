package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;
import ru.mirea.exhibition.core.data.CsvExhibitionRepository;
import ru.mirea.exhibition.core.data.CsvStandInbox;
import ru.mirea.exhibition.core.registration.EventRegistrationService;
import ru.mirea.exhibition.core.registration.InMemoryEventStore;
import ru.mirea.exhibition.core.registration.RegistrationResult;
import ru.mirea.exhibition.core.registration.RejectedEvent;
import ru.mirea.exhibition.core.stats.ExhibitStats;
import ru.mirea.exhibition.core.stats.StatisticsReport;
import ru.mirea.exhibition.core.stats.StatisticsService;
import ru.mirea.exhibition.core.sync.ContentSyncService;
import ru.mirea.exhibition.core.sync.SyncPlan;

/**
 * Результаты на общих тестовых данных совпадают с результатами
 * процедурной реализации (таблица 2.7 отчёта).
 */
class DemoDataTest {

    @Test
    @DisplayName("Результаты совпадают с процедурной реализацией")
    void sameResultsAsProcedural() {
        Clock clock = TestData.clockAt("2026-10-27T18:00:00");
        LocalDateTime now = LocalDateTime.now(clock);
        ExhibitionCatalog catalog = new ExhibitionCatalog(
                new CsvExhibitionRepository(
                        CsvExhibitionRepositoryTest.DEMO_DATA));
        CsvStandInbox inbox =
                new CsvStandInbox(CsvExhibitionRepositoryTest.DEMO_DATA);

        ContentSyncService sync = new ContentSyncService(catalog);
        SyncPlan plan = sync.synchronize(sync.planFor("ST-01", now),
                inbox.manifest("ST-01"));
        assertEquals(List.of(8), plan.toAdd());
        assertEquals(List.of(2, 4), plan.toUpdate());
        assertEquals(List.of(3), plan.toDelete());
        assertEquals(1, plan.unchanged());

        InMemoryEventStore store = new InMemoryEventStore();
        RegistrationResult r = EventRegistrationService
                .standard(catalog, store, clock)
                .register(inbox.eventPackage());
        assertEquals(13, r.accepted().size());
        List<String> rejected = new ArrayList<>();
        for (RejectedEvent e : r.rejected()) {
            rejected.add(e.eventId() + " " + e.reason());
        }
        assertEquals(List.of("E012 UNKNOWN_STAND", "E013 NOT_PLACED",
                "E014 BAD_TYPE", "E015 FUTURE_TIME", "E016 BAD_DURATION",
                "E007 DUPLICATE", "E018 BAD_FORMAT", "E020 NOT_PUBLISHED",
                "E021 NOT_PUBLISHED", "E022 NOT_PLACED",
                "E023 NOT_PUBLISHED"), rejected);

        StatisticsReport report = new StatisticsService(catalog)
                .report(store.findAll(), now);
        List<Integer> ranking = new ArrayList<>();
        for (ExhibitStats s : report.ranking()) {
            ranking.add(s.exhibitId());
        }
        assertEquals(List.of(8, 2, 5, 1, 4, 3, 7), ranking);
        assertEquals(List.of(9), report.unrequested());
    }
}
