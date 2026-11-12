package ru.mirea.exhibition.core.history;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import ru.mirea.exhibition.core.domain.Period;
import ru.mirea.exhibition.core.domain.Periodic;

/**
 * История записей одного объекта (например, все размещения экспоната
 * на стенде или все опубликованные версии экспоната), упорядоченная
 * по началу периода. Класс гарантирует, что периоды не пересекаются,
 * то есть в любой момент действует не больше одной записи.
 *
 * @param <T> тип записи с периодом действия
 */
public final class History<T extends Periodic> {

    private final List<T> records;

    /**
     * @throws IllegalArgumentException если периоды записей
     *                                  пересекаются
     */
    public History(Collection<T> records) {
        List<T> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(r -> r.period().from()));
        for (int i = 1; i < sorted.size(); i++) {
            Period previous = sorted.get(i - 1).period();
            Period current = sorted.get(i).period();
            if (previous.overlaps(current)) {
                throw new IllegalArgumentException(
                        "Периоды записей пересекаются: " + previous
                                + " и " + current);
            }
        }
        this.records = List.copyOf(sorted);
    }

    /**
     * Запись, действовавшая в момент t. Двоичным поиском находится
     * последняя запись с началом не позже t, затем проверяется,
     * что t раньше её окончания. Сложность O(log h), h — длина истории.
     */
    public Optional<T> activeAt(LocalDateTime t) {
        int low = 0;
        int high = records.size() - 1;
        int found = -1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            if (records.get(middle).period().from().isAfter(t)) {
                high = middle - 1;
            } else {
                found = middle;
                low = middle + 1;
            }
        }
        if (found < 0) {
            return Optional.empty();
        }
        T candidate = records.get(found);
        return candidate.period().contains(t)
                ? Optional.of(candidate) : Optional.empty();
    }

    /** Все записи истории по возрастанию начала периода. */
    public List<T> records() {
        return records;
    }

    public int size() {
        return records.size();
    }
}
