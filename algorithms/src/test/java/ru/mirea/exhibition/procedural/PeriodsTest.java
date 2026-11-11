package ru.mirea.exhibition.procedural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PeriodsTest {

    private static final List<String[]> ROWS = List.of(
            new String[]{"1", "", "", "", "2026-10-01T00:00:00",
                    "2026-10-10T00:00:00"},
            new String[]{"2", "", "", "", "2026-10-10T00:00:00",
                    "2026-10-20T00:00:00"},
            new String[]{"3", "", "", "", "2026-10-25T00:00:00", ""});

    private static String idAt(String t) {
        String[] row = Periods.activeAt(ROWS, Periods.PL_FROM, Periods.PL_TO,
                LocalDateTime.parse(t));
        return row == null ? null : row[0];
    }

    @Test
    @DisplayName("Начало периода входит в период, окончание — нет")
    void halfOpenInterval() {
        assertEquals("1", idAt("2026-10-01T00:00:00"));
        assertEquals("1", idAt("2026-10-09T23:59:59"));
        assertEquals("2", idAt("2026-10-10T00:00:00"));
        assertNull(idAt("2026-10-20T00:00:00"));
    }

    @Test
    @DisplayName("До первого периода и в разрыве между периодами — ничего")
    void beforeAndBetweenPeriods() {
        assertNull(idAt("2026-09-30T23:59:59"));
        assertNull(idAt("2026-10-22T12:00:00"));
        assertEquals("3", idAt("2030-01-01T00:00:00"));
        assertNull(Periods.activeAt(null, Periods.PL_FROM, Periods.PL_TO,
                LocalDateTime.parse("2026-10-22T12:00:00")));
    }
}
