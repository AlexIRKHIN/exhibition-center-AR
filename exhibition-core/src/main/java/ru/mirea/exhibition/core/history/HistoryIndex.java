package ru.mirea.exhibition.core.history;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import ru.mirea.exhibition.core.domain.Periodic;

/**
 * Набор историй, сгруппированных по ключу: например, истории
 * размещений по паре «стенд — экспонат» или истории публикации
 * по экспонату. Поиск истории по ключу — O(1) в хеш-таблице,
 * поиск записи внутри истории — двоичный.
 *
 * @param <K> ключ группировки
 * @param <T> тип записи с периодом действия
 */
public final class HistoryIndex<K, T extends Periodic> {

    private final Map<K, History<T>> histories;

    public HistoryIndex(Collection<T> records, Function<T, K> keyOf) {
        Map<K, List<T>> groups = new HashMap<>();
        for (T record : records) {
            groups.computeIfAbsent(keyOf.apply(record),
                    k -> new ArrayList<>()).add(record);
        }
        Map<K, History<T>> built = new HashMap<>();
        groups.forEach((key, group) ->
                built.put(key, new History<>(group)));
        this.histories = Collections.unmodifiableMap(built);
    }

    /** Запись с данным ключом, действовавшая в момент t. */
    public Optional<T> activeAt(K key, LocalDateTime t) {
        History<T> history = histories.get(key);
        return history == null ? Optional.empty() : history.activeAt(t);
    }

    /** История по ключу; пустая, если записей с таким ключом нет. */
    public History<T> historyOf(K key) {
        return histories.getOrDefault(key, new History<>(List.of()));
    }

    public Set<K> keys() {
        return histories.keySet();
    }
}
