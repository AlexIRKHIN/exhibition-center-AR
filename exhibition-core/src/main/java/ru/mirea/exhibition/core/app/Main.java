package ru.mirea.exhibition.core.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;
import ru.mirea.exhibition.core.data.CsvExhibitionRepository;
import ru.mirea.exhibition.core.data.CsvStandInbox;
import ru.mirea.exhibition.core.registration.EventRegistrationService;
import ru.mirea.exhibition.core.registration.InMemoryEventStore;
import ru.mirea.exhibition.core.registration.RegisteredEvent;
import ru.mirea.exhibition.core.registration.RegistrationResult;
import ru.mirea.exhibition.core.registration.RejectedEvent;
import ru.mirea.exhibition.core.stats.ExhibitStats;
import ru.mirea.exhibition.core.stats.StatisticsReport;
import ru.mirea.exhibition.core.stats.StatisticsService;
import ru.mirea.exhibition.core.sync.ContentSyncService;
import ru.mirea.exhibition.core.sync.StandContent;
import ru.mirea.exhibition.core.sync.SyncPlan;

/**
 * Демонстрация объектно-ориентированной реализации на тех же
 * тестовых данных, что и процедурная (каталог algorithms/data).
 * Вывод совпадает по форме с выводом процедурной программы.
 * <p>
 * Аргументы (необязательные): [каталог данных] [id стенда]
 * [текущее время ISO-8601]. По умолчанию — время компьютера.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Path dataDir = resolveDataDir(args.length > 0 ? args[0] : null);
        String standId = args.length > 1 ? args[1] : "ST-01";
        Clock clock = args.length > 2
                ? fixedClock(LocalDateTime.parse(args[2]))
                : Clock.systemDefaultZone();
        LocalDateTime now = LocalDateTime.now(clock);

        ExhibitionCatalog catalog = new ExhibitionCatalog(
                new CsvExhibitionRepository(dataDir));
        CsvStandInbox inbox = new CsvStandInbox(dataDir);
        System.out.println("Объектно-ориентированная реализация");
        System.out.println("Каталог данных: " + dataDir.toAbsolutePath());
        System.out.println("Время проверки: " + now);

        // ---------- Алгоритм 1 ----------
        ContentSyncService sync = new ContentSyncService(catalog);
        StandContent plan = sync.planFor(standId, now);
        StandContent manifest = inbox.manifest(standId);
        SyncPlan result = sync.synchronize(plan, manifest);
        System.out.println();
        System.out.println("=== Алгоритм 1. Синхронизация контента стенда "
                + standId + " ===");
        System.out.println("План на сервере (экспонат=версия): " + plan);
        System.out.println("Манифест стенда  (экспонат=версия): "
                + manifest);
        printIds("Загрузить", result.toAdd(), catalog);
        printIds("Обновить", result.toUpdate(), catalog);
        printIds("Удалить", result.toDelete(), catalog);
        System.out.println("Без изменений: " + result.unchanged());

        // ---------- Алгоритм 2 ----------
        InMemoryEventStore store = new InMemoryEventStore();
        EventRegistrationService registration =
                EventRegistrationService.standard(catalog, store, clock);
        List<String[]> eventPackage = inbox.eventPackage();
        RegistrationResult registered = registration.register(eventPackage);
        System.out.println();
        System.out.println(
                "=== Алгоритм 2. Регистрация событий взаимодействия ===");
        System.out.println("Получено событий: " + registered.received()
                + ", принято: " + registered.accepted().size()
                + ", отклонено: " + registered.rejected().size());
        for (RegisteredEvent e : registered.accepted()) {
            System.out.println("  принято " + e.id() + " — размещение "
                    + e.placement().id() + ", версия " + e.version().id()
                    + " (№" + e.version().number() + ")");
        }
        for (RejectedEvent e : registered.rejected()) {
            System.out.println("  отклонено " + e.eventId() + " — "
                    + e.reason());
        }

        // ---------- Алгоритм 3 ----------
        StatisticsReport report = new StatisticsService(catalog)
                .report(store.findAll(), now);
        System.out.println();
        System.out.println("=== Алгоритм 3. Статистика взаимодействий"
                + " по экспонатам ===");
        System.out.printf("%-3s %-32s %10s %10s %11s%n", "№", "Экспонат",
                "Обращений", "Всего, с", "Среднее, с");
        int place = 1;
        for (ExhibitStats s : report.ranking()) {
            System.out.printf("%-3d %-32s %10d %10d %11.1f%n", place++,
                    s.exhibitId() + ". " + catalog.titleOf(s.exhibitId()),
                    s.count(), s.totalSec(), s.averageSec());
        }
        System.out.println("Экспозиция на момент проверки: "
                + report.exposition());
        printIds("Невостребованные экспонаты", report.unrequested(),
                catalog);
    }

    private static Clock fixedClock(LocalDateTime now) {
        ZoneId zone = ZoneId.systemDefault();
        return Clock.fixed(now.atZone(zone).toInstant(), zone);
    }

    /** Ищет общий каталог тестовых данных проекта. */
    private static Path resolveDataDir(String argument) {
        if (argument != null) {
            return Path.of(argument);
        }
        Path[] candidates = {Path.of("..", "algorithms", "data"),
                Path.of("algorithms", "data"), Path.of("data")};
        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }
        return candidates[0];
    }

    private static void printIds(String caption, List<Integer> ids,
                                 ExhibitionCatalog catalog) {
        StringBuilder line = new StringBuilder(caption).append(": ");
        if (ids.isEmpty()) {
            line.append("—");
        }
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                line.append(", ");
            }
            int id = ids.get(i);
            line.append(id).append(" (").append(catalog.titleOf(id))
                    .append(")");
        }
        System.out.println(line);
    }
}
