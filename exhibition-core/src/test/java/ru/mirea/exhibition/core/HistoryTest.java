package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.mirea.exhibition.core.TestData.at;
import static ru.mirea.exhibition.core.TestData.period;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.mirea.exhibition.core.domain.PublicationVersion;
import ru.mirea.exhibition.core.history.History;

class HistoryTest {

    private final History<PublicationVersion> history = new History<>(
            List.of(new PublicationVersion(3, 1, 3,
                            period("2026-10-25T00:00:00", null)),
                    new PublicationVersion(1, 1, 1,
                            period("2026-10-10T00:00:00",
                                    "2026-10-15T00:00:00")),
                    new PublicationVersion(2, 1, 2,
                            period("2026-10-15T00:00:00",
                                    "2026-10-20T00:00:00"))));

    @Test
    @DisplayName("Двоичный поиск находит версию на момент, в том числе"
            + " на границе смены")
    void findsActiveRecord() {
        assertTrue(history.activeAt(at("2026-10-09T23:59:59")).isEmpty());
        assertEquals(1, history.activeAt(at("2026-10-12T12:00:00"))
                .orElseThrow().number());
        assertEquals(2, history.activeAt(at("2026-10-15T00:00:00"))
                .orElseThrow().number());
        assertTrue(history.activeAt(at("2026-10-22T00:00:00")).isEmpty());
        assertEquals(3, history.activeAt(at("2027-01-01T00:00:00"))
                .orElseThrow().number());
        assertEquals(3, history.size());
    }

    @Test
    @DisplayName("История не допускает пересекающихся периодов")
    void rejectsOverlap() {
        assertThrows(IllegalArgumentException.class, () -> new History<>(
                List.of(new PublicationVersion(1, 1, 1,
                                period("2026-10-10T00:00:00", null)),
                        new PublicationVersion(2, 1, 2,
                                period("2026-10-15T00:00:00", null)))));
    }
}
