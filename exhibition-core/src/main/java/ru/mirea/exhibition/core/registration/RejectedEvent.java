package ru.mirea.exhibition.core.registration;

/** Отклонённое событие и причина отклонения. */
public record RejectedEvent(String eventId, RejectionReason reason) {
}
