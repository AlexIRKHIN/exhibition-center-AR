package ru.mirea.exhibition.procedural;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Алгоритм 1. Синхронизация контента стенда.
 * <p>
 * Сравнивает план стенда на сервере (какие экспонаты и в какой версии
 * должны показываться) с манифестом стенда (что уже загружено в кэш)
 * и определяет, какие экспонаты загрузить, обновить и удалить.
 */
public final class ContentSync {

    /** Индексы списков в результате {@link #computeSync}. */
    public static final int ADD = 0;
    public static final int UPDATE = 1;
    public static final int DELETE = 2;

    private ContentSync() {
    }

    /**
     * Строит план стенда на момент now: id экспоната → номер версии,
     * опубликованной в этот момент. Учитываются только размещения,
     * действующие в момент now; черновики и снятые с публикации
     * экспонаты в план не входят.
     *
     * @param placements размещения (формат см. {@link Periods})
     * @param versions   версии публикации (формат см. {@link Periods})
     */
    public static Map<Integer, Integer> buildServerPlan(
            String standId, List<String[]> placements,
            List<String[]> versions, LocalDateTime now) {
        Map<Integer, List<String[]>> published =
                Periods.publicationHistory(versions);
        Map<Integer, Integer> plan = new HashMap<>();
        for (String[] p : placements) {
            if (!p[Periods.PL_STAND].equals(standId)
                    || !Periods.isActive(p, Periods.PL_FROM,
                    Periods.PL_TO, now)) {
                continue;
            }
            int exhibitId = Integer.parseInt(p[Periods.PL_EXHIBIT]);
            String[] v = Periods.activeAt(published.get(exhibitId),
                    Periods.V_FROM, Periods.V_TO, now);
            if (v != null) {
                plan.put(exhibitId, Integer.parseInt(v[Periods.V_NO]));
            }
        }
        return plan;
    }

    /** Строки манифеста стенда (exhibit_id;version) → словарь. */
    public static Map<Integer, Integer> readManifest(List<String[]> rows) {
        Map<Integer, Integer> manifest = new HashMap<>();
        for (String[] row : rows) {
            manifest.put(Integer.parseInt(row[0]),
                    Integer.parseInt(row[1]));
        }
        return manifest;
    }

    /**
     * Вычисляет состав синхронизации.
     *
     * @param server план стенда на сервере: id экспоната → версия
     * @param stand  манифест стенда: id экспоната → версия в кэше
     * @return три списка id по возрастанию: [ADD] загрузить,
     *         [UPDATE] обновить, [DELETE] удалить
     */
    public static List<List<Integer>> computeSync(
            Map<Integer, Integer> server, Map<Integer, Integer> stand) {
        List<Integer> toAdd = new ArrayList<>();
        List<Integer> toUpdate = new ArrayList<>();
        List<Integer> toDelete = new ArrayList<>();

        for (Map.Entry<Integer, Integer> item : server.entrySet()) {
            Integer localVersion = stand.get(item.getKey());
            if (localVersion == null) {
                toAdd.add(item.getKey());
            } else if (!localVersion.equals(item.getValue())) {
                toUpdate.add(item.getKey());
            }
        }
        for (Integer exhibitId : stand.keySet()) {
            if (!server.containsKey(exhibitId)) {
                toDelete.add(exhibitId);
            }
        }

        Collections.sort(toAdd);
        Collections.sort(toUpdate);
        Collections.sort(toDelete);
        List<List<Integer>> result = new ArrayList<>();
        result.add(toAdd);
        result.add(toUpdate);
        result.add(toDelete);
        return result;
    }

    /** Число экспонатов, которые уже актуальны на стенде. */
    public static int countUnchanged(Map<Integer, Integer> server,
                                     List<List<Integer>> sync) {
        return server.size() - sync.get(ADD).size()
                - sync.get(UPDATE).size();
    }
}
