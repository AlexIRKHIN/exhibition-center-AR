package ru.mirea.exhibition.core.registration;

/** Причины отклонения события (коды совпадают с главой 2). */
public enum RejectionReason {

    BAD_FORMAT("нарушен формат записи"),
    UNKNOWN_STAND("стенд не зарегистрирован"),
    BAD_TYPE("недопустимый тип события"),
    FUTURE_TIME("время начала позже текущего"),
    BAD_DURATION("длительность вне допустимых границ"),
    NOT_PLACED("в момент события экспонат не был размещён на стенде"),
    NOT_PUBLISHED("в момент события у экспоната не было"
            + " опубликованной версии"),
    EXHIBITION_CLOSED("в момент события выставка не была открыта"
            + " для посетителей"),
    DUPLICATE("событие уже было принято");

    private final String description;

    RejectionReason(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
