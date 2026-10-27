package ru.mirea.exhibition.procedural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticsAggregationTest {

    private static String[] event(String exhibitId, int duration) {
        return new String[]{"E", "ST-01", exhibitId, "OPEN", "2026-10-20T10:00:00", String.valueOf(duration)};
    }

    @Test
    @DisplayName("Обращения и время суммируются по каждому экспонату")
    void aggregatesCountAndTime() {
        Map<Integer, int[]> stats = StatisticsAggregation.aggregate(List.of(
                event("1", 45), event("2", 120), event("1", 50)));

        assertArrayEquals(new int[]{2, 95}, stats.get(1));
        assertArrayEquals(new int[]{1, 120}, stats.get(2));
        assertEquals(47.5, StatisticsAggregation.average(stats.get(1)), 1e-9);
    }

    @Test
    @DisplayName("Рейтинг: сначала по обращениям, затем по среднему времени, затем по id")
    void ranksByCountThenAverage() {
        Map<Integer, int[]> stats = StatisticsAggregation.aggregate(List.of(
                event("2", 120), event("2", 95), event("2", 75),     // 3 обращения, среднее 96.7
                event("8", 180), event("8", 150), event("8", 35),    // 3 обращения, среднее 121.7
                event("4", 210),                                     // 1 обращение
                event("5", 60), event("5", 240),                     // 2 обращения, среднее 150
                event("7", 100), event("6", 100), event("6", 100), event("7", 100))); // равенство → по id

        assertEquals(List.of(8, 2, 5, 6, 7, 4), StatisticsAggregation.rank(stats));
    }

    @Test
    @DisplayName("Без событий статистика пуста, а все экспонаты экспозиции невостребованы")
    void noEvents() {
        Map<Integer, int[]> stats = StatisticsAggregation.aggregate(List.of());
        assertTrue(stats.isEmpty());
        assertEquals(List.of(1, 3), StatisticsAggregation.unrequested(Set.of(3, 1), stats).stream().sorted().toList());
    }

    @Test
    @DisplayName("В экспозицию входят только опубликованные размещённые экспонаты")
    void expositionAndUnrequested() {
        List<String[]> exhibits = List.of(
                new String[]{"1", "1", "A", "1", "true"},
                new String[]{"3", "1", "C", "1", "true"},
                new String[]{"6", "1", "Черновик", "1", "false"},
                new String[]{"9", "1", "Не размещён", "1", "true"});
        List<String[]> placements = List.of(
                new String[]{"ST-01", "1", "1", "1"},
                new String[]{"ST-01", "6", "2", "0"},
                new String[]{"ST-03", "3", "1", "1"});

        Set<Integer> exposition = StatisticsAggregation.exhibitsOnExposition(placements, exhibits);
        assertEquals(Set.of(1, 3), exposition);

        Map<Integer, int[]> stats = StatisticsAggregation.aggregate(List.<String[]>of(event("1", 30)));
        assertEquals(List.of(3), StatisticsAggregation.unrequested(exposition, stats));
    }
}
