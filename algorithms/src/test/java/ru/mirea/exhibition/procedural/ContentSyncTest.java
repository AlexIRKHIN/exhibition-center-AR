package ru.mirea.exhibition.procedural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentSyncTest {

    private static final List<String[]> EXHIBITS = List.of(
            new String[]{"1", "1", "A", "3", "true"},
            new String[]{"2", "1", "B", "2", "true"},
            new String[]{"6", "1", "Черновик", "1", "false"});
    private static final List<String[]> PLACEMENTS = List.of(
            new String[]{"ST-01", "1", "1", "3"},
            new String[]{"ST-01", "2", "2", "1"},
            new String[]{"ST-01", "6", "3", "0"},
            new String[]{"ST-02", "2", "1", "2"});

    @Test
    @DisplayName("План стенда содержит только опубликованные экспонаты этого стенда")
    void serverPlanSkipsDraftsAndOtherStands() {
        Map<Integer, Integer> plan = ContentSync.buildServerPlan("ST-01", PLACEMENTS, EXHIBITS);
        assertEquals(Map.of(1, 3, 2, 2), plan);
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
        List<List<Integer>> sync = ContentSync.computeSync(Map.of(5, 1, 3, 2), Map.of());
        assertEquals(List.of(3, 5), sync.get(ContentSync.ADD));
        assertEquals(List.of(), sync.get(ContentSync.UPDATE));
        assertEquals(List.of(), sync.get(ContentSync.DELETE));
    }

    @Test
    @DisplayName("Стенд актуален: изменений нет")
    void upToDateStandNeedsNothing() {
        Map<Integer, Integer> same = Map.of(1, 3, 2, 2);
        List<List<Integer>> sync = ContentSync.computeSync(same, same);
        assertEquals(0, sync.get(ContentSync.ADD).size() + sync.get(ContentSync.UPDATE).size()
                + sync.get(ContentSync.DELETE).size());
        assertEquals(2, ContentSync.countUnchanged(same, sync));
    }
}
