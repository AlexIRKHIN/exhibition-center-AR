package ru.mirea.exhibition.core.registration;

import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.history.HistoryIndex;

/**
 * Проверка 6: в момент начала события экспонат был размещён на этом
 * стенде (иначе NOT_PLACED).
 */
public final class PlacementActiveRule
        extends HistoryRule<Placement.Key, Placement> {

    public PlacementActiveRule(
            HistoryIndex<Placement.Key, Placement> placements) {
        super(placements, RejectionReason.NOT_PLACED);
    }

    @Override
    protected Placement.Key keyOf(IncomingEvent event) {
        return new Placement.Key(event.standId(), event.exhibitId());
    }

    @Override
    protected void remember(EventCandidate c, Placement found) {
        c.bindPlacement(found);
    }
}
