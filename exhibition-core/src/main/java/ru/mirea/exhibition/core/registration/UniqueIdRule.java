package ru.mirea.exhibition.core.registration;

import java.util.Optional;

/**
 * Проверка 8: событие с таким идентификатором ещё не принято
 * (иначе DUPLICATE). Защищает от повторной отправки пакета стендом.
 */
public final class UniqueIdRule implements EventRule {

    private final EventStore store;

    public UniqueIdRule(EventStore store) {
        this.store = store;
    }

    @Override
    public Optional<RejectionReason> check(EventCandidate c) {
        return store.contains(c.event().id())
                ? Optional.of(RejectionReason.DUPLICATE)
                : Optional.empty();
    }
}
