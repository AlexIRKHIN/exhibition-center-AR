package ru.mirea.exhibition.core.domain;

/**
 * Запись, действующая в течение периода: размещение экспоната на стенде
 * или опубликованная версия сведений об экспонате. Интерфейс позволяет
 * хранить такие записи в одной обобщённой истории
 * ({@link ru.mirea.exhibition.core.history.History}).
 */
public interface Periodic {

    /** Период действия записи. */
    Period period();
}
