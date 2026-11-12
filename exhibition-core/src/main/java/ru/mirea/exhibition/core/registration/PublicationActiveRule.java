package ru.mirea.exhibition.core.registration;

import ru.mirea.exhibition.core.domain.PublicationVersion;
import ru.mirea.exhibition.core.history.HistoryIndex;

/**
 * Проверка 7: в момент начала события у экспоната была опубликованная
 * версия (иначе NOT_PUBLISHED). Черновики в историю публикации
 * не входят, поэтому событие о черновике отклоняется.
 */
public final class PublicationActiveRule
        extends HistoryRule<Integer, PublicationVersion> {

    public PublicationActiveRule(
            HistoryIndex<Integer, PublicationVersion> publications) {
        super(publications, RejectionReason.NOT_PUBLISHED);
    }

    @Override
    protected Integer keyOf(IncomingEvent event) {
        return event.exhibitId();
    }

    @Override
    protected void remember(EventCandidate c, PublicationVersion found) {
        c.bindVersion(found);
    }
}
