package ru.mirea.exhibition.core.stats;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;
import ru.mirea.exhibition.core.registration.RegisteredEvent;

/**
 * Алгоритм 3 (операция О5): статистика взаимодействий по экспонатам.
 */
public final class StatisticsService {

    /**
     * Порядок рейтинга: по убыванию числа обращений, при равенстве —
     * по убыванию среднего времени, затем по возрастанию id.
     */
    public static final Comparator<ExhibitStats> BY_POPULARITY =
            Comparator.comparingInt(ExhibitStats::count).reversed()
                    .thenComparing(Comparator
                            .comparingDouble(ExhibitStats::averageSec)
                            .reversed())
                    .thenComparingInt(ExhibitStats::exhibitId);

    private final ExhibitionCatalog catalog;

    public StatisticsService(ExhibitionCatalog catalog) {
        this.catalog = catalog;
    }

    /** Цикл А: суммирование показателей по экспонатам. */
    public List<ExhibitStats> aggregate(
            Collection<RegisteredEvent> events) {
        Map<Integer, ExhibitStats> byExhibit = new HashMap<>();
        for (RegisteredEvent event : events) {
            byExhibit.computeIfAbsent(event.exhibitId(), ExhibitStats::new)
                    .add(event.durationSec());
        }
        return new ArrayList<>(byExhibit.values());
    }

    /**
     * Полный отчёт: рейтинг, экспозиция в момент now и невостребованные
     * экспонаты (цикл Б — по экспозиции).
     */
    public StatisticsReport report(Collection<RegisteredEvent> events,
                                   LocalDateTime now) {
        List<ExhibitStats> ranking = aggregate(events);
        ranking.sort(BY_POPULARITY);
        Set<Integer> requested = new HashSet<>();
        ranking.forEach(s -> requested.add(s.exhibitId()));
        Set<Integer> exposition = catalog.expositionAt(now);
        List<Integer> unrequested = new ArrayList<>();
        for (int exhibitId : exposition) {
            if (!requested.contains(exhibitId)) {
                unrequested.add(exhibitId);
            }
        }
        return new StatisticsReport(ranking, exposition, unrequested);
    }
}
