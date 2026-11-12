package ru.mirea.exhibition.core.registration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Хранилище принятых событий в памяти (порядок приёма сохраняется). */
public final class InMemoryEventStore implements EventStore {

    private final Map<String, RegisteredEvent> events =
            new LinkedHashMap<>();

    @Override
    public boolean contains(String eventId) {
        return events.containsKey(eventId);
    }

    @Override
    public void save(RegisteredEvent event) {
        if (events.putIfAbsent(event.id(), event) != null) {
            throw new IllegalStateException(
                    "Событие уже сохранено: " + event.id());
        }
    }

    @Override
    public List<RegisteredEvent> findAll() {
        return List.copyOf(events.values());
    }
}
