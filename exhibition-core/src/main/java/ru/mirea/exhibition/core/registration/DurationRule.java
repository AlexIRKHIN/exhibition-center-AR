package ru.mirea.exhibition.core.registration;

import java.util.Optional;

/** Проверка 5: длительность в допустимых границах (BAD_DURATION). */
public final class DurationRule implements EventRule {

    public static final int MIN_SEC = 1;
    public static final int MAX_SEC = 3600;

    private final int minSec;
    private final int maxSec;

    public DurationRule() {
        this(MIN_SEC, MAX_SEC);
    }

    public DurationRule(int minSec, int maxSec) {
        if (minSec > maxSec) {
            throw new IllegalArgumentException("min > max");
        }
        this.minSec = minSec;
        this.maxSec = maxSec;
    }

    @Override
    public Optional<RejectionReason> check(EventCandidate c) {
        int duration = c.event().durationSec();
        return duration < minSec || duration > maxSec
                ? Optional.of(RejectionReason.BAD_DURATION)
                : Optional.empty();
    }
}
