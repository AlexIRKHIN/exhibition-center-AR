package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.mirea.exhibition.core.TestData.at;
import static ru.mirea.exhibition.core.TestData.period;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.mirea.exhibition.core.domain.Period;

class PeriodTest {

    @Test
    @DisplayName("Период — полуинтервал: начало входит, окончание нет")
    void halfOpenInterval() {
        Period p = period("2026-10-20T10:00:00", "2026-10-20T12:00:00");
        assertFalse(p.contains(at("2026-10-20T09:59:59")));
        assertTrue(p.contains(at("2026-10-20T10:00:00")));
        assertTrue(p.contains(at("2026-10-20T11:59:59")));
        assertFalse(p.contains(at("2026-10-20T12:00:00")));
    }

    @Test
    @DisplayName("Период без окончания действует по настоящее время")
    void openPeriod() {
        Period p = Period.openFrom(at("2026-10-15T09:00:00"));
        assertTrue(p.isOpen());
        assertTrue(p.contains(at("2030-01-01T00:00:00")));
    }

    @Test
    @DisplayName("Окончание раньше начала — ошибка")
    void rejectsWrongBounds() {
        assertThrows(IllegalArgumentException.class, () -> period(
                "2026-10-20T12:00:00", "2026-10-20T10:00:00"));
    }

    @Test
    @DisplayName("Смежные периоды не пересекаются, наложенные — да")
    void overlaps() {
        Period first = period("2026-10-15T09:00:00",
                "2026-10-21T08:00:00");
        Period next = period("2026-10-21T08:00:00", null);
        Period inside = period("2026-10-20T00:00:00",
                "2026-10-20T01:00:00");
        assertFalse(first.overlaps(next));
        assertTrue(first.overlaps(inside));
        assertTrue(next.overlaps(Period.openFrom(
                at("2026-11-01T00:00:00"))));
    }
}
