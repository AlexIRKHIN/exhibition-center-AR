package ru.mirea.exhibition.core.registration;

import java.util.List;

/**
 * Хранилище принятых событий. Сейчас события хранятся в памяти
 * ({@link InMemoryEventStore}), на следующем этапе — в таблице
 * базы данных.
 */
public interface EventStore {

    boolean contains(String eventId);

    void save(RegisteredEvent event);

    /** Все принятые события в порядке приёма. */
    List<RegisteredEvent> findAll();
}
