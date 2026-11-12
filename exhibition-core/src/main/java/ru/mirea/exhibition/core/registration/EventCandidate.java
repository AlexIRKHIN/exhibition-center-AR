package ru.mirea.exhibition.core.registration;

import ru.mirea.exhibition.core.domain.EventType;
import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.domain.PublicationVersion;

/**
 * Событие в процессе проверки. Правила истории запоминают в нём
 * найденные размещение и версию, чтобы принятое событие было
 * привязано к тому содержанию, которое посетитель действительно видел.
 */
public final class EventCandidate {

    private final IncomingEvent event;
    private Placement placement;
    private PublicationVersion version;

    public EventCandidate(IncomingEvent event) {
        this.event = event;
    }

    public IncomingEvent event() {
        return event;
    }

    void bindPlacement(Placement placement) {
        this.placement = placement;
    }

    void bindVersion(PublicationVersion version) {
        this.version = version;
    }

    /**
     * Принятое событие. Вызывается только после того, как все правила
     * выполнены.
     *
     * @throws IllegalStateException если размещение или версия
     *                               не были найдены правилами
     */
    RegisteredEvent toRegistered() {
        if (placement == null || version == null) {
            throw new IllegalStateException(
                    "Событие " + event.id() + " не привязано"
                            + " к размещению и версии");
        }
        EventType type = EventType.fromCode(event.typeCode())
                .orElseThrow(() -> new IllegalStateException(
                        "Тип события не проверен: " + event.id()));
        return new RegisteredEvent(event, type, placement, version);
    }
}
