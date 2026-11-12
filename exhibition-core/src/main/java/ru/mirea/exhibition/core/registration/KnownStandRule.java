package ru.mirea.exhibition.core.registration;

import java.util.Optional;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;

/** Проверка 2: стенд зарегистрирован (иначе UNKNOWN_STAND). */
public final class KnownStandRule implements EventRule {

    private final ExhibitionCatalog catalog;

    public KnownStandRule(ExhibitionCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public Optional<RejectionReason> check(EventCandidate c) {
        return catalog.hasStand(c.event().standId())
                ? Optional.empty()
                : Optional.of(RejectionReason.UNKNOWN_STAND);
    }
}
