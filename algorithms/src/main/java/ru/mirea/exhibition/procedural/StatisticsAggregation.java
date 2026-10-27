package ru.mirea.exhibition.procedural;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Алгоритм 3. Агрегация статистики взаимодействий по экспонатам.
 * <p>
 * Для каждого экспоната считаются число обращений и суммарное время
 * просмотра, по ним — среднее время; строится рейтинг экспонатов
 * и список невостребованных.
 */
public final class StatisticsAggregation {

    /** Индексы в массиве показателей экспоната. */
    public static final int COUNT = 0;
    public static final int TOTAL_SEC = 1;

    private StatisticsAggregation() {
    }

    /**
     * Суммирует принятые события по экспонатам.
     *
     * @param events принятые события (формат см. EventRegistration)
     * @return id экспоната → {число обращений, суммарное время, с}
     */
    public static Map<Integer, int[]> aggregate(List<String[]> events) {
        Map<Integer, int[]> stats = new HashMap<>();
        for (String[] event : events) {
            int exhibitId = Integer.parseInt(event[2]);
            int duration = Integer.parseInt(event[5]);
            int[] values = stats.get(exhibitId);
            if (values == null) {
                values = new int[2];
                stats.put(exhibitId, values);
            }
            values[COUNT]++;
            values[TOTAL_SEC] += duration;
        }
        return stats;
    }

    public static double average(int[] values) {
        if (values[COUNT] == 0) {
            return 0.0;
        }
        return (double) values[TOTAL_SEC] / values[COUNT];
    }

    /**
     * Рейтинг: по убыванию числа обращений, при равенстве — по убыванию
     * среднего времени, затем по возрастанию id.
     */
    public static List<Integer> rank(Map<Integer, int[]> stats) {
        List<Integer> ids = new ArrayList<>(stats.keySet());
        ids.sort((a, b) -> {
            int[] sa = stats.get(a);
            int[] sb = stats.get(b);
            if (sa[COUNT] != sb[COUNT]) {
                return Integer.compare(sb[COUNT], sa[COUNT]);
            }
            int byAverage = Double.compare(average(sb), average(sa));
            return byAverage != 0 ? byAverage : Integer.compare(a, b);
        });
        return ids;
    }

    /** Экспонаты экспозиции: опубликованные и размещённые на стендах. */
    public static Set<Integer> exhibitsOnExposition(
            List<String[]> placements, List<String[]> exhibits) {
        Set<Integer> published = new HashSet<>();
        for (String[] exhibit : exhibits) {
            if (Boolean.parseBoolean(exhibit[4])) {
                published.add(Integer.parseInt(exhibit[0]));
            }
        }
        Set<Integer> result = new TreeSet<>();
        for (String[] placement : placements) {
            int exhibitId = Integer.parseInt(placement[1]);
            if (published.contains(exhibitId)) {
                result.add(exhibitId);
            }
        }
        return result;
    }

    /** Невостребованные: есть в экспозиции, но без обращений. */
    public static List<Integer> unrequested(Set<Integer> exposition,
                                            Map<Integer, int[]> stats) {
        List<Integer> result = new ArrayList<>();
        for (Integer exhibitId : exposition) {
            if (!stats.containsKey(exhibitId)) {
                result.add(exhibitId);
            }
        }
        return result;
    }
}
