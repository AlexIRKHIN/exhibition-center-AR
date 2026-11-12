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
        repository.exhibitions().forEach(e -> exhibitions.put(e.id(), e));
        repository.exhibits().forEach(e -> exhibits.put(e.id(), e));
        repository.stands().forEach(s -> stands.put(s.id(), s));
        placements = List.copyOf(repository.placements());
        placementHistory = new HistoryIndex<>(placements, Placement::key);
        List<PublicationVersion> published = new ArrayList<>();
        for (PublicationVersion v : repository.versions()) {
            if (!v.isDraft()) {
                published.add(v);   // черновик посетители не видят
            }
        }
        publicationHistory = new HistoryIndex<>(published,
                PublicationVersion::exhibitId);
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
