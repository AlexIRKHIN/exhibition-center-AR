package ru.mirea.exhibition.procedural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentSyncTest {

    private static final List<String[]> VERSIONS = List.of(
            new String[]{"101", "1", "1", "2026-10-01T09:00:00",
                    "2026-10-10T09:00:00"},
            new String[]{"102", "1", "2", "2026-10-10T09:00:00", ""},
            new String[]{"103", "2", "1", "2026-10-01T09:00:00", ""},
            new String[]{"104", "6", "1", "", ""});        // черновик
    private static final List<String[]> PLACEMENTS = List.of(
            new String[]{"1", "ST-01", "1", "1", "2026-10-01T09:00:00", ""},
            new String[]{"2", "ST-01", "2", "2", "2026-10-01T09:00:00",
                    "2026-10-05T09:00:00"},                // снят со стенда
            new String[]{"3", "ST-01", "6", "3", "2026-10-01T09:00:00", ""},
            new String[]{"4", "ST-02", "2", "1", "2026-10-01T09:00:00", ""});

    @Test
    @DisplayName("План стенда берёт версию, опубликованную в момент проверки")
    void serverPlanUsesVersionAtMoment() {
        // 3 октября действует версия 1 экспоната 1, экспонат 2 ещё на стенде
        assertEquals(Map.of(1, 1, 2, 1), ContentSync.buildServerPlan(
                "ST-01", PLACEMENTS, VERSIONS,
                LocalDateTime.parse("2026-10-03T12:00:00")));
    }

    @Test
    @DisplayName("В план не входят черновики, снятые размещения и чужие стенды")
    void serverPlanSkipsDraftsRemovedAndOtherStands() {
        // 12 октября: у экспоната 1 версия 2, экспонат 2 снят со стенда,
        // экспонат 6 — черновик, размещение на ST-02 к стенду не относится
        Map<Integer, Integer> plan = ContentSync.buildServerPlan("ST-01",
                PLACEMENTS, VERSIONS,
                LocalDateTime.parse("2026-10-12T12:00:00"));
        assertEquals(Map.of(1, 2), plan);
    }

    @Test
    @DisplayName("Новые, изменённые и снятые экспонаты распределяются по трём спискам")
    void computesAddUpdateDelete() {
        Map<Integer, Integer> server = Map.of(1, 3, 2, 2, 4, 4, 8, 5);
        Map<Integer, Integer> stand = Map.of(1, 3, 2, 1, 3, 1, 4, 3);

        List<List<Integer>> sync = ContentSync.computeSync(server, stand);

        assertEquals(List.of(8), sync.get(ContentSync.ADD));
        assertEquals(List.of(2, 4), sync.get(ContentSync.UPDATE));
        assertEquals(List.of(3), sync.get(ContentSync.DELETE));
        assertEquals(1, ContentSync.countUnchanged(server, sync));
    }

    @Test
    @DisplayName("Пустой кэш стенда: все экспонаты нужно загрузить")
    void emptyStandDownloadsEverything() {
        List<List<Integer>> sync =
                ContentSync.computeSync(Map.of(5, 1, 3, 2), Map.of());
        assertEquals(List.of(3, 5), sync.get(ContentSync.ADD));
        assertEquals(List.of(), sync.get(ContentSync.UPDATE));
        assertEquals(List.of(), sync.get(ContentSync.DELETE));
    }

    @Test
    @DisplayName("Стенд актуален: изменений нет")
    void upToDateStandNeedsNothing() {
        Map<Integer, Integer> same = Map.of(1, 3, 2, 2);
        List<List<Integer>> sync = ContentSync.computeSync(same, same);
        assertEquals(0, sync.get(ContentSync.ADD).size()
                + sync.get(ContentSync.UPDATE).size()
                + sync.get(ContentSync.DELETE).size());
        assertEquals(2, ContentSync.countUnchanged(same, sync));
    }
}
