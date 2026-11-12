package ru.mirea.exhibition.core.registration;

import java.util.Optional;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;

/**
 * Новое требование (подраздел 3.4): событие учитывается, только если
 * в момент его начала выставка была открыта для посетителей. Так
 * отсекаются обращения сотрудников во время пусконаладки стендов
 * до открытия выставки (иначе EXHIBITION_CLOSED).
 */
public final class ExhibitionOpenRule implements EventRule {

    private final ExhibitionCatalog catalog;

    public ExhibitionOpenRule(ExhibitionCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public Optional<RejectionReason> check(EventCandidate c) {
        IncomingEvent event = c.event();
        boolean open = catalog.exhibitionOf(event.exhibitId())
                .map(e -> e.isOpenAt(event.start()))
                .orElse(false);
        return open
                ? Optional.empty()
                : Optional.of(RejectionReason.EXHIBITION_CLOSED);
    }
}
