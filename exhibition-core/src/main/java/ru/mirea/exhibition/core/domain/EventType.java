package ru.mirea.exhibition.core.domain;

import java.util.Optional;

/** Допустимые типы событий взаимодействия посетителя со стендом. */
public enum EventType {

    OPEN("открытие карточки экспоната"),
    MODEL_3D("просмотр 3D-модели"),
    VIDEO("просмотр видео");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    /** Тип по коду из записи стенда; пусто, если код неизвестен. */
    public static Optional<EventType> fromCode(String code) {
        for (EventType type : values()) {
            if (type.name().equals(code)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
