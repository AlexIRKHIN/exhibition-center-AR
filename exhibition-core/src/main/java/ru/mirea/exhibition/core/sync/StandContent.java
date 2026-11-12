package ru.mirea.exhibition.core.sync;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Содержимое стенда: какой экспонат в какой версии на нём
 * показывается. Тем же классом описываются и план стенда на сервере,
 * и манифест кэша стенда — их и сравнивает синхронизация.
 */
public final class StandContent {

    private final Map<Integer, Integer> versions;

    /** @param versions id экспоната → номер версии */
    public StandContent(Map<Integer, Integer> versions) {
        this.versions = Map.copyOf(versions);
    }

    public Optional<Integer> versionOf(int exhibitId) {
        return Optional.ofNullable(versions.get(exhibitId));
    }

    public boolean contains(int exhibitId) {
        return versions.containsKey(exhibitId);
    }

    public Set<Integer> exhibitIds() {
        return versions.keySet();
    }

    public int size() {
        return versions.size();
    }

    /** Например, {1=3, 2=2, 4=4} — по возрастанию id экспоната. */
    @Override
    public String toString() {
        return new TreeMap<>(versions).toString();
    }
}
