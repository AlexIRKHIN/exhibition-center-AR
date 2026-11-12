package ru.mirea.exhibition.core.domain;

import java.util.Objects;

/**
 * Размещение экспоната на стенде в течение периода. При снятии
 * экспоната запись не удаляется, а получает окончание периода,
 * поэтому размещения образуют историю.
 */
public record Placement(int id, String standId, int exhibitId,
                        int displayOrder, Period period)
        implements Periodic {

    public Placement {
        Objects.requireNonNull(period, "Не задан период размещения");
    }

    /** Ключ истории размещений: пара «стенд — экспонат». */
    public record Key(String standId, int exhibitId) {
    }

    public Key key() {
        return new Key(standId, exhibitId);
    }
}
