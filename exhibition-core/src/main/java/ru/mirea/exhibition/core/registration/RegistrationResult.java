package ru.mirea.exhibition.core.registration;

import java.util.List;

/** Результат обработки пакета событий. */
public record RegistrationResult(List<RegisteredEvent> accepted,
                                 List<RejectedEvent> rejected) {

    public RegistrationResult {
        accepted = List.copyOf(accepted);
        rejected = List.copyOf(rejected);
    }

    public int received() {
        return accepted.size() + rejected.size();
    }
}
