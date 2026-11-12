package ru.mirea.exhibition.core.stats;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Итог алгоритма 3: рейтинг экспонатов, состав экспозиции на момент
 * проверки и невостребованные экспонаты.
 */
public record StatisticsReport(List<ExhibitStats> ranking,
                               Set<Integer> exposition,
                               List<Integer> unrequested) {

    public StatisticsReport {
        ranking = List.copyOf(ranking);
        exposition = Collections.unmodifiableSet(new TreeSet<>(exposition));
        unrequested = List.copyOf(unrequested);
    }
}
