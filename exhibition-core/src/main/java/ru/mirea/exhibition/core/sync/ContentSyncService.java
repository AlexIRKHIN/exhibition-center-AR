package ru.mirea.exhibition.core.sync;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;
import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.domain.PublicationVersion;

/**
 * Алгоритм 1 (операции О2, О3): синхронизация контента стенда.
 * Строит план стенда на сервере и сравнивает его с манифестом кэша.
 */
public final class ContentSyncService {

    private final ExhibitionCatalog catalog;

    public ContentSyncService(ExhibitionCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * План стенда в момент now: экспонаты с действующим размещением
     * и номер версии, опубликованной в этот момент. Черновики
     * и снятые с публикации экспонаты в план не входят.
     */
    public StandContent planFor(String standId, LocalDateTime now) {
        Map<Integer, Integer> plan = new HashMap<>();
        for (Placement p : catalog.placementsOnStandAt(standId, now)) {
            Optional<PublicationVersion> version =
                    catalog.publishedVersionAt(p.exhibitId(), now);
            version.ifPresent(v -> plan.put(p.exhibitId(), v.number()));
        }
        return new StandContent(plan);
    }

    /**
     * Сравнивает план сервера с манифестом стенда.
     * Цикл А — по плану (ADD, UPDATE), цикл Б — по манифесту (DELETE).
     */
    public SyncPlan synchronize(StandContent server, StandContent stand) {
        List<Integer> toAdd = new ArrayList<>();
        List<Integer> toUpdate = new ArrayList<>();
        List<Integer> toDelete = new ArrayList<>();
        for (int exhibitId : server.exhibitIds()) {
            Optional<Integer> cached = stand.versionOf(exhibitId);
            if (cached.isEmpty()) {
                toAdd.add(exhibitId);
            } else if (!cached.equals(server.versionOf(exhibitId))) {
                toUpdate.add(exhibitId);
            }
        }
        for (int exhibitId : stand.exhibitIds()) {
            if (!server.contains(exhibitId)) {
                toDelete.add(exhibitId);
            }
        }
        Collections.sort(toAdd);
        Collections.sort(toUpdate);
        Collections.sort(toDelete);
        int unchanged = server.size() - toAdd.size() - toUpdate.size();
        return new SyncPlan(toAdd, toUpdate, toDelete, unchanged);
    }
}
