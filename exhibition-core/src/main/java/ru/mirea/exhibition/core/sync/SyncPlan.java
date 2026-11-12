package ru.mirea.exhibition.core.sync;

import java.util.List;

/**
 * Состав синхронизации стенда: какие экспонаты загрузить, обновить
 * и удалить (id по возрастанию) и сколько уже актуальны.
 */
public record SyncPlan(List<Integer> toAdd, List<Integer> toUpdate,
                       List<Integer> toDelete, int unchanged) {

    public SyncPlan {
        toAdd = List.copyOf(toAdd);
        toUpdate = List.copyOf(toUpdate);
        toDelete = List.copyOf(toDelete);
    }

    /** Стенд актуален: менять ничего не нужно. */
    public boolean isUpToDate() {
        return toAdd.isEmpty() && toUpdate.isEmpty() && toDelete.isEmpty();
    }
}
