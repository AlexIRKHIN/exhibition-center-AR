package ru.mirea.exhibition.procedural;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Работа с периодами действия: размещения экспоната на стенде и версии
 * публикации действуют в полуинтервале [начало, окончание); пустое
 * окончание означает «действует по настоящее время».
 * <p>
 * Форматы строк:
 * placements.csv: placement_id;stand_id;exhibit_id;display_order;
 * valid_from;valid_to
 * versions.csv: version_id;exhibit_id;version_no;published_from;
 * published_to (пустое published_from — черновик)
 */
public final class Periods {

    public static final int PL_ID = 0;
    public static final int PL_STAND = 1;
    public static final int PL_EXHIBIT = 2;
    public static final int PL_FROM = 4;
    public static final int PL_TO = 5;

    public static final int V_ID = 0;
    public static final int V_EXHIBIT = 1;
    public static final int V_NO = 2;
    public static final int V_FROM = 3;
    public static final int V_TO = 4;

    private Periods() {
    }

    public static String key(String standId, String exhibitId) {
        return standId + "/" + exhibitId;
    }

    /** История размещений: «стенд/экспонат» → строки по началу периода. */
    public static Map<String, List<String[]>> placementHistory(
            List<String[]> placements) {
        Map<String, List<String[]>> history = new HashMap<>();
        for (String[] p : placements) {
            history.computeIfAbsent(key(p[PL_STAND], p[PL_EXHIBIT]),
                    k -> new ArrayList<>()).add(p);
        }
        for (List<String[]> rows : history.values()) {
            rows.sort((a, b) -> a[PL_FROM].compareTo(b[PL_FROM]));
        }
        return history;
    }

    /** История публикации: экспонат → опубликованные версии по началу. */
    public static Map<Integer, List<String[]>> publicationHistory(
            List<String[]> versions) {
        Map<Integer, List<String[]>> history = new HashMap<>();
        for (String[] v : versions) {
            if (v[V_FROM].isEmpty()) {
                continue;                     // черновик не публиковался
            }
            history.computeIfAbsent(Integer.parseInt(v[V_EXHIBIT]),
                    k -> new ArrayList<>()).add(v);
        }
        for (List<String[]> rows : history.values()) {
            rows.sort((a, b) -> a[V_FROM].compareTo(b[V_FROM]));
        }
        return history;
    }

    /**
     * Находит строку, период которой содержит момент t: двоичным поиском
     * выбирается последняя строка с началом не позже t, затем
     * проверяется её окончание.
     *
     * @return найденная строка или null
     */
    public static String[] activeAt(List<String[]> rows, int fromIdx,
                                    int toIdx, LocalDateTime t) {
        if (rows == null) {
            return null;
        }
        int lo = 0;
        int hi = rows.size() - 1;
        int found = -1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            String from = rows.get(mid)[fromIdx];
            if (!LocalDateTime.parse(from).isAfter(t)) {
                found = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        if (found < 0) {
            return null;
        }
        String[] row = rows.get(found);
        return isActive(row, fromIdx, toIdx, t) ? row : null;
    }

    /** Действует ли строка в момент t: начало ≤ t < окончание. */
    public static boolean isActive(String[] row, int fromIdx, int toIdx,
                                   LocalDateTime t) {
        if (row[fromIdx].isEmpty()
                || LocalDateTime.parse(row[fromIdx]).isAfter(t)) {
            return false;
        }
        String to = row.length > toIdx ? row[toIdx] : "";
        return to.isEmpty() || t.isBefore(LocalDateTime.parse(to));
    }
}
