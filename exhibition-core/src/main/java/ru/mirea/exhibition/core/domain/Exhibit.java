package ru.mirea.exhibition.core.domain;

/** Экспонат в составе выставки. */
public record Exhibit(int id, int exhibitionId, String title) {
}
