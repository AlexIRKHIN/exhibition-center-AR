package ru.mirea.exhibition.core.registration;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;

/**
 * Алгоритм 2 (операция О4): регистрация пакета событий от стенда.
 * Каждое событие разбирается (проверка формата), затем к нему по
 * порядку применяются правила; принятое событие сохраняется
 * в хранилище вместе с размещением и версией на момент события.
 */
public final class EventRegistrationService {

    private final List<EventRule> rules;
    private final EventStore store;

    public EventRegistrationService(List<EventRule> rules,
                                    EventStore store) {
        this.rules = List.copyOf(rules);
        this.store = store;
    }

    /**
     * Стандартный набор правил в порядке таблицы проверок: правила
     * 2–8 из главы 2 и правило открытия выставки из подраздела 3.4.
     */
    public static EventRegistrationService standard(
            ExhibitionCatalog catalog, EventStore store, Clock clock) {
        List<EventRule> rules = List.of(
                new KnownStandRule(catalog),
                new EventTypeRule(),
                new NotInFutureRule(clock),
                new DurationRule(),
                new PlacementActiveRule(catalog.placementHistory()),
                new PublicationActiveRule(catalog.publicationHistory()),
                new ExhibitionOpenRule(catalog),
                new UniqueIdRule(store));
        return new EventRegistrationService(rules, store);
    }

    /** Обрабатывает пакет записей стенда в порядке поступления. */
    public RegistrationResult register(List<String[]> eventPackage) {
        List<RegisteredEvent> accepted = new ArrayList<>();
        List<RejectedEvent> rejected = new ArrayList<>();
        for (String[] record : eventPackage) {
            Optional<IncomingEvent> parsed = IncomingEvent.parse(record);
            if (parsed.isEmpty()) {
                rejected.add(new RejectedEvent(idOf(record),
                        RejectionReason.BAD_FORMAT));
                continue;
            }
            EventCandidate candidate = new EventCandidate(parsed.get());
            Optional<RejectionReason> reason = firstViolation(candidate);
            if (reason.isPresent()) {
                rejected.add(new RejectedEvent(parsed.get().id(),
                        reason.get()));
            } else {
                RegisteredEvent event = candidate.toRegistered();
                store.save(event);
                accepted.add(event);
            }
        }
        return new RegistrationResult(accepted, rejected);
    }

    private Optional<RejectionReason> firstViolation(EventCandidate c) {
        for (EventRule rule : rules) {
            Optional<RejectionReason> reason = rule.check(c);
            if (reason.isPresent()) {
                return reason;
            }
        }
        return Optional.empty();
    }

    private static String idOf(String[] record) {
        return record.length > 0 && !record[0].isEmpty() ? record[0] : "?";
    }
}
