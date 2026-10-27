package ru.mirea.exhibition.procedural;

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
     * Строит план стенда: id экспоната → опубликованная версия.
     *
     * @param standId    идентификатор стенда
     * @param placements размещения:
     *                   stand_id;exhibit_id;display_order;version_on_stand
     * @param exhibits   экспонаты: id;exhibition_id;title;version;published
     */
    public static Map<Integer, Integer> buildServerPlan(
            String standId, List<String[]> placements,
            List<String[]> exhibits) {
        Map<Integer, Integer> published = new HashMap<>();
        for (String[] exhibit : exhibits) {
            if (Boolean.parseBoolean(exhibit[4])) {
                published.put(Integer.parseInt(exhibit[0]),
                        Integer.parseInt(exhibit[3]));
            }
        }
        Map<Integer, Integer> plan = new HashMap<>();
        for (String[] placement : placements) {
            if (!placement[0].equals(standId)) {
                continue;
            }
            int exhibitId = Integer.parseInt(placement[1]);
            Integer version = published.get(exhibitId);
            if (version != null) {      // черновики на стенд не выдаются
                plan.put(exhibitId, version);
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
