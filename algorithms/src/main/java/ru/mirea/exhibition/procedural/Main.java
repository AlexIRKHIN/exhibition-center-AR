package ru.mirea.exhibition.procedural;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Демонстрация процедурной реализации алгоритмов на тестовых данных.
 * <p>
 * Аргументы (необязательные): [каталог данных] [id стенда]
 * [текущее время ISO-8601]. По умолчанию: data, ST-01, время компьютера.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        Path dataDir = resolveDataDir(args.length > 0 ? args[0] : null);
        String standId = args.length > 1 ? args[1] : "ST-01";
        LocalDateTime now = args.length > 2
                ? LocalDateTime.parse(args[2]) : LocalDateTime.now();

        List<String[]> exhibits = read(dataDir, "exhibits.csv");
        List<String[]> stands = read(dataDir, "stands.csv");
        List<String[]> placements = read(dataDir, "placements.csv");
        List<String[]> manifestRows =
                read(dataDir, "manifest_" + standId + ".csv");
        List<String[]> events = read(dataDir, "events.csv");

        Map<Integer, String> titles = new HashMap<>();
        for (String[] exhibit : exhibits) {
            titles.put(Integer.parseInt(exhibit[0]), exhibit[2]);
        }

        System.out.println("Каталог данных: " + dataDir.toAbsolutePath());
        System.out.println("Время проверки: " + now);

        // ---------- Алгоритм 1 ----------
        System.out.println();
        System.out.println("=== Алгоритм 1. Синхронизация контента стенда "
                + standId + " ===");
        Map<Integer, Integer> plan =
                ContentSync.buildServerPlan(standId, placements, exhibits);
        Map<Integer, Integer> manifest =
                ContentSync.readManifest(manifestRows);
        System.out.println("План на сервере (экспонат=версия): "
                + new TreeMap<>(plan));
        System.out.println("Манифест стенда  (экспонат=версия): "
                + new TreeMap<>(manifest));
        List<List<Integer>> sync = ContentSync.computeSync(plan, manifest);
        printIds("Загрузить", sync.get(ContentSync.ADD), titles);
        printIds("Обновить", sync.get(ContentSync.UPDATE), titles);
        printIds("Удалить", sync.get(ContentSync.DELETE), titles);
        System.out.println("Без изменений: "
                + ContentSync.countUnchanged(plan, sync));

        // ---------- Алгоритм 2 ----------
        System.out.println();
        System.out.println(
                "=== Алгоритм 2. Регистрация событий взаимодействия ===");
        Set<String> standIds = EventRegistration.buildStandIds(stands);
        Set<String> placementKeys =
                EventRegistration.buildPlacementKeys(placements);
        List<String> rejected = new ArrayList<>();
        List<String[]> accepted = EventRegistration.registerAll(
                events, standIds, placementKeys, now, rejected);
        System.out.println("Получено событий: " + events.size()
                + ", принято: " + accepted.size()
                + ", отклонено: " + rejected.size());
        for (String line : rejected) {
            System.out.println("  отклонено " + line);
        }

        // ---------- Алгоритм 3 ----------
        System.out.println();
        System.out.println(
                "=== Алгоритм 3. Статистика взаимодействий по экспонатам ===");
        Map<Integer, int[]> stats = StatisticsAggregation.aggregate(accepted);
        List<Integer> ranking = StatisticsAggregation.rank(stats);
        System.out.printf("%-3s %-32s %10s %10s %11s%n", "№", "Экспонат",
                "Обращений", "Всего, с", "Среднее, с");
        int place = 1;
        for (Integer exhibitId : ranking) {
            int[] values = stats.get(exhibitId);
            System.out.printf("%-3d %-32s %10d %10d %11.1f%n", place++,
                    exhibitId + ". " + titles.get(exhibitId),
                    values[StatisticsAggregation.COUNT],
                    values[StatisticsAggregation.TOTAL_SEC],
                    StatisticsAggregation.average(values));
        }
        Set<Integer> exposition =
                StatisticsAggregation.exhibitsOnExposition(placements, exhibits);
        printIds("Невостребованные экспонаты",
                StatisticsAggregation.unrequested(exposition, stats), titles);
    }

    private static List<String[]> read(Path dir, String name)
            throws IOException {
        return CsvReader.read(dir.resolve(name));
    }

    /** Ищет каталог data, чтобы запуск работал и из корня репозитория. */
    private static Path resolveDataDir(String argument) {
        if (argument != null) {
            return Path.of(argument);
        }
        Path[] candidates = {Path.of("data"), Path.of("algorithms", "data")};
        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return candidates[0];
    }

    private static void printIds(String caption, List<Integer> ids,
                                 Map<Integer, String> titles) {
        StringBuilder line = new StringBuilder(caption).append(": ");
        if (ids.isEmpty()) {
            line.append("—");
        }
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                line.append(", ");
            }
            int id = ids.get(i);
            line.append(id).append(" (").append(titles.get(id)).append(")");
        }
        System.out.println(line);
    }
}
