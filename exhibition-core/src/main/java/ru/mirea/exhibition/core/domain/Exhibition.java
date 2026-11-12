package ru.mirea.exhibition.core.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Выставка: сроки, в которые она открыта для посетителей
 * (включительно по обе даты).
 */
public record Exhibition(int id, String title, LocalDate opens,
                         LocalDate closes) {

    public Exhibition {
        Objects.requireNonNull(opens, "Не задана дата открытия");
        Objects.requireNonNull(closes, "Не задана дата закрытия");
        if (closes.isBefore(opens)) {
            throw new IllegalArgumentException(
                    "Выставка закрывается раньше открытия: " + title);
        }
    }

    /** Открыта ли выставка для посетителей в момент t. */
    public boolean isOpenAt(LocalDateTime t) {
        LocalDate day = t.toLocalDate();
        return !day.isBefore(opens) && !day.isAfter(closes);
    }
}
