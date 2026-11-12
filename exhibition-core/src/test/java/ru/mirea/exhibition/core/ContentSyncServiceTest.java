package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.mirea.exhibition.core.TestData.at;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.mirea.exhibition.core.sync.ContentSyncService;
import ru.mirea.exhibition.core.sync.StandContent;
import ru.mirea.exhibition.core.sync.SyncPlan;

class ContentSyncServiceTest {

    private final ContentSyncService service =
            new ContentSyncService(TestData.catalog());

    @Test
    @DisplayName("План стенда берёт версию, опубликованную в момент now;"
            + " черновик в план не входит")
    void planUsesVersionAtMoment() {
        StandContent plan = service.planFor("S1",
                at("2026-10-20T12:00:00"));
        assertEquals("{1=1, 2=1, 4=1}", plan.toString());
    }

    @Test
    @DisplayName("Снятые со стенда и снятые с публикации экспонаты"
            + " в план не входят")
    void planSkipsRemovedAndWithdrawn() {
        StandContent plan = service.planFor("S1",
                at("2026-10-24T12:00:00"));
        assertEquals("{1=2}", plan.toString());
        assertEquals("{2=1}", service.planFor("S2",
                at("2026-10-24T12:00:00")).toString());
    }

    @Test
    @DisplayName("Новые, изменённые и снятые экспонаты попадают"
            + " в разные списки")
    void splitsIntoLists() {
        SyncPlan plan = service.synchronize(
                new StandContent(Map.of(1, 2, 2, 1, 5, 1)),
                new StandContent(Map.of(1, 1, 2, 1, 3, 1)));
        assertEquals(List.of(5), plan.toAdd());
        assertEquals(List.of(1), plan.toUpdate());
        assertEquals(List.of(3), plan.toDelete());
        assertEquals(1, plan.unchanged());
        assertFalse(plan.isUpToDate());
    }

    @Test
    @DisplayName("Пустой кэш — загрузить всё; совпадающий — ничего")
    void emptyAndActualCache() {
        StandContent server = new StandContent(Map.of(3, 1, 1, 2));
        SyncPlan fresh = service.synchronize(server,
                new StandContent(Map.of()));
        assertEquals(List.of(1, 3), fresh.toAdd());
        SyncPlan actual = service.synchronize(server,
                new StandContent(Map.of(1, 2, 3, 1)));
        assertTrue(actual.isUpToDate());
        assertEquals(2, actual.unchanged());
    }
}
