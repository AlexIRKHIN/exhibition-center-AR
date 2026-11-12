package ru.mirea.exhibition.core.registration;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Проверка 4: время начала не позже текущего (иначе FUTURE_TIME).
 * Текущее время берётся из часов {@link Clock}, поэтому в тестах
 * его можно зафиксировать.
 */
public final class NotInFutureRule implements EventRule {

    private final Clock clock;

    public NotInFutureRule(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<RejectionReason> check(EventCandidate c) {
        LocalDateTime now = LocalDateTime.now(clock);
        return c.event().start().isAfter(now)
                ? Optional.of(RejectionReason.FUTURE_TIME)
                : Optional.empty();
    }
}
