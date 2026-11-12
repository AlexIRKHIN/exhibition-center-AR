package ru.mirea.exhibition.core.registration;

import java.util.Optional;

import ru.mirea.exhibition.core.domain.Periodic;
import ru.mirea.exhibition.core.history.HistoryIndex;

/**
 * Общая схема проверок «по истории»: найти запись, действовавшую
 * в момент начала события; если её нет — отклонить событие, если есть
 * — запомнить её в событии. Подклассы задают только ключ поиска
 * и то, куда запоминается найденная запись (шаблонный метод).
 *
 * @param <K> ключ истории
 * @param <T> тип записи истории
 */
public abstract class HistoryRule<K, T extends Periodic>
        implements EventRule {

    private final HistoryIndex<K, T> history;
    private final RejectionReason reason;

    protected HistoryRule(HistoryIndex<K, T> history,
                          RejectionReason reason) {
        this.history = history;
        this.reason = reason;
    }

    @Override
    public final Optional<RejectionReason> check(EventCandidate c) {
        IncomingEvent event = c.event();
        Optional<T> active = history.activeAt(keyOf(event), event.start());
        if (active.isEmpty()) {
            return Optional.of(reason);
        }
        remember(c, active.get());
        return Optional.empty();
    }

    /** Ключ, по которому ищется история для события. */
    protected abstract K keyOf(IncomingEvent event);

    /** Сохранение найденной записи в проверяемом событии. */
    protected abstract void remember(EventCandidate c, T found);
}
