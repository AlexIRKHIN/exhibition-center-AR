package ru.mirea.exhibition.core.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Период действия в виде полуинтервала [начало; окончание).
 * Пустое окончание (null) означает «действует по настоящее время».
 * Полуинтервал выбран для того, чтобы в момент смены версии или
 * размещения действовала ровно одна запись — новая.
 *
 * @param from начало периода (включительно)
 * @param to   окончание периода (не включительно) или null
 */
public record Period(LocalDateTime from, LocalDateTime to) {

    public Period {
        Objects.requireNonNull(from, "Не задано начало периода");
        if (to != null && !to.isAfter(from)) {
            throw new IllegalArgumentException(
                    "Окончание периода должно быть позже начала: "
                            + from + " — " + to);
        }
    }

    /** Период без окончания: действует с from по настоящее время. */
    public static Period openFrom(LocalDateTime from) {
        return new Period(from, null);
    }

    public boolean isOpen() {
        return to == null;
    }

    /** Действует ли период в момент t: начало ≤ t < окончание. */
    public boolean contains(LocalDateTime t) {
        return !t.isBefore(from) && (to == null || t.isBefore(to));
    }

    /** Пересекается ли период с другим (общий момент действия). */
    public boolean overlaps(Period other) {
        boolean thisEndsBefore = to != null && !to.isAfter(other.from);
        boolean otherEndsBefore =
                other.to != null && !other.to.isAfter(from);
        return !thisEndsBefore && !otherEndsBefore;
    }
}
