package ru.mirea.exhibition.core.registration;

import java.util.Optional;

import ru.mirea.exhibition.core.domain.EventType;

/** Проверка 3: тип события допустим (иначе BAD_TYPE). */
public final class EventTypeRule implements EventRule {

    @Override
    public Optional<RejectionReason> check(EventCandidate c) {
        return EventType.fromCode(c.event().typeCode()).isPresent()
                ? Optional.empty()
                : Optional.of(RejectionReason.BAD_TYPE);
    }
}
