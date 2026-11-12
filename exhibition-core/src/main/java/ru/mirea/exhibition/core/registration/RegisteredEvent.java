package ru.mirea.exhibition.core.registration;

import ru.mirea.exhibition.core.domain.EventType;
import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.domain.PublicationVersion;

/**
 * Принятое событие взаимодействия, привязанное к размещению и версии
 * публикации, действовавшим в момент его начала (сущность «Событие
 * взаимодействия» логической модели данных).
 */
public record RegisteredEvent(IncomingEvent event, EventType type,
                              Placement placement,
                              PublicationVersion version) {

    public String id() {
        return event.id();
    }

    public int exhibitId() {
        return event.exhibitId();
    }

    public int durationSec() {
        return event.durationSec();
    }
}
