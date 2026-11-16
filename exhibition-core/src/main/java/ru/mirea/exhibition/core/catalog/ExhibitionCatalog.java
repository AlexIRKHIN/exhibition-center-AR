package ru.mirea.exhibition.core.catalog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collection;
import java.util.function.Function;

import ru.mirea.exhibition.core.data.ExhibitionRepository;
import ru.mirea.exhibition.core.domain.Exhibit;
import ru.mirea.exhibition.core.domain.Exhibition;
import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.domain.PublicationVersion;
import ru.mirea.exhibition.core.domain.Stand;
import ru.mirea.exhibition.core.history.HistoryIndex;

/**
 * Каталог экспозиции: справочники выставок, экспонатов и стендов,
 * история размещений и история публикации. Каталог один раз
 * загружается из хранилища и отвечает на вопросы «что было
 * размещено и опубликовано в момент t», которые нужны всем трём
 * алгоритмам.
 */
public final class ExhibitionCatalog {

    private final Map<Integer, Exhibition> exhibitions = new HashMap<>();
    private final Map<Integer, Exhibit> exhibits = new HashMap<>();
    private final Map<String, Stand> stands = new HashMap<>();
    private final List<Placement> placements;
    private final HistoryIndex<Placement.Key, Placement> placementHistory;
    private final HistoryIndex<Integer, PublicationVersion>
            publicationHistory;

    public ExhibitionCatalog(ExhibitionRepository repository) {
        exhibitions.putAll(uniqueById(repository.exhibitions(),
                Exhibition::id, "Выставка"));
        exhibits.putAll(uniqueById(repository.exhibits(), Exhibit::id, "Экспонат"));
        stands.putAll(uniqueById(repository.stands(), Stand::id, "Стенд"));
        placements = List.copyOf(repository.placements());
        uniqueById(placements, Placement::id, "Размещение");
        List<PublicationVersion> versions = List.copyOf(repository.versions());
        uniqueById(versions, PublicationVersion::id, "Версия");
        for (Exhibit e : exhibits.values()) {
            requireReference(exhibitions, e.exhibitionId(),
                    "Экспонат " + e.id(), "выставка");
        }
        for (Placement p : placements) {
            requireReference(stands, p.standId(), "Размещение " + p.id(), "стенд");
            requireReference(exhibits, p.exhibitId(), "Размещение " + p.id(), "экспонат");
        }
        for (PublicationVersion v : versions) {
            requireReference(exhibits, v.exhibitId(), "Версия " + v.id(), "экспонат");
        }
        placementHistory = new HistoryIndex<>(placements, Placement::key);
        List<PublicationVersion> published = new ArrayList<>();
        for (PublicationVersion v : versions) {
            if (!v.isDraft()) {
                published.add(v);   // черновик посетители не видят
            }
        }
        publicationHistory = new HistoryIndex<>(published,
                PublicationVersion::exhibitId);
    }

    private static <K, T> Map<K, T> uniqueById(Collection<T> records,
                                               Function<T, K> idOf, String entity) {
        Map<K, T> result = new HashMap<>();
        for (T record : records) {
            K id = idOf.apply(record);
            if (result.putIfAbsent(id, record) != null) {
                throw new IllegalArgumentException(entity + ": повторный id=" + id);
            }
        }
        return result;
    }

    private static <K> void requireReference(Map<K, ?> records, K id,
                                              String source, String target) {
        if (!records.containsKey(id)) {
            throw new IllegalArgumentException(source + ": отсутствует "
                    + target + " id=" + id);
        }
    }

    public boolean hasStand(String standId) {
        return stands.containsKey(standId);
    }

    public Optional<Exhibit> exhibit(int exhibitId) {
        return Optional.ofNullable(exhibits.get(exhibitId));
    }

    /** Название экспоната для отчётов; «?» для неизвестного id. */
    public String titleOf(int exhibitId) {
        return exhibit(exhibitId).map(Exhibit::title).orElse("?");
    }

    /** Выставка, в которую входит экспонат. */
    public Optional<Exhibition> exhibitionOf(int exhibitId) {
        return exhibit(exhibitId)
                .map(e -> exhibitions.get(e.exhibitionId()));
    }

    public HistoryIndex<Placement.Key, Placement> placementHistory() {
        return placementHistory;
    }

    public HistoryIndex<Integer, PublicationVersion>
            publicationHistory() {
        return publicationHistory;
    }

    /** Версия экспоната, опубликованная в момент t. */
    public Optional<PublicationVersion> publishedVersionAt(
            int exhibitId, LocalDateTime t) {
        return publicationHistory.activeAt(exhibitId, t);
    }

    /** Размещения на стенде, действующие в момент t. */
    public List<Placement> placementsOnStandAt(String standId,
                                               LocalDateTime t) {
        List<Placement> result = new ArrayList<>();
        for (Placement p : placements) {
            if (p.standId().equals(standId) && p.period().contains(t)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Экспозиция в момент t: экспонаты, которые размещены хотя бы
     * на одном стенде и имеют опубликованную версию.
     */
    public Set<Integer> expositionAt(LocalDateTime t) {
        Set<Integer> result = new TreeSet<>();
        for (Placement p : placements) {
            if (p.period().contains(t)
                    && publishedVersionAt(p.exhibitId(), t).isPresent()) {
                result.add(p.exhibitId());
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
