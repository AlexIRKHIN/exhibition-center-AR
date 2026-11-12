package ru.mirea.exhibition.core;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;
import ru.mirea.exhibition.core.domain.Exhibit;
import ru.mirea.exhibition.core.domain.Exhibition;
import ru.mirea.exhibition.core.domain.Period;
import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.domain.PublicationVersion;
import ru.mirea.exhibition.core.domain.Stand;

/**
 * Небольшой каталог для модульных тестов (октябрь 2026 года).
 * <ul>
 * <li>Выставка открыта для посетителей с 16.10 по 20.12.</li>
 * <li>Экспонат 1: версия 1 до 21.10 08:00, затем версия 2.</li>
 * <li>Экспонат 2: стоял на S1 до 21.10 18:00, с 22.10 — на S2.</li>
 * <li>Экспонат 3: размещён на S1, но это черновик.</li>
 * <li>Экспонат 4: публикация снята 23.10 в 12:00.</li>
 * </ul>
 */
final class TestData {

    static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    private TestData() {
    }

    static LocalDateTime at(String isoDateTime) {
        return LocalDateTime.parse(isoDateTime);
    }

    static Clock clockAt(String isoDateTime) {
        return Clock.fixed(at(isoDateTime).atZone(ZONE).toInstant(), ZONE);
    }

    static Period period(String from, String to) {
        return new Period(at(from), to == null ? null : at(to));
    }

    static ExhibitionCatalog catalog() {
        String open = "2026-10-15T09:00:00";
        return new ExhibitionCatalog(new InMemoryExhibitionRepository(
                List.of(new Exhibition(1, "Космос рядом",
                        LocalDate.of(2026, 10, 16),
                        LocalDate.of(2026, 12, 20))),
                List.of(new Exhibit(1, 1, "Метеорит"),
                        new Exhibit(2, 1, "Луноход"),
                        new Exhibit(3, 1, "Телескоп"),
                        new Exhibit(4, 1, "Карта неба")),
                List.of(new Stand("S1", "Стенд 1", "Зал 1", "киоск"),
                        new Stand("S2", "Стенд 2", "Зал 2", "стол")),
                List.of(new PublicationVersion(11, 1, 1,
                                period(open, "2026-10-21T08:00:00")),
                        new PublicationVersion(12, 1, 2,
                                period("2026-10-21T08:00:00", null)),
                        new PublicationVersion(13, 2, 1,
                                period(open, null)),
                        new PublicationVersion(14, 3, 1, null),
                        new PublicationVersion(15, 4, 1,
                                period(open, "2026-10-23T12:00:00"))),
                List.of(new Placement(1, "S1", 1, 1, period(open, null)),
                        new Placement(2, "S1", 2, 2,
                                period(open, "2026-10-21T18:00:00")),
                        new Placement(3, "S1", 3, 3, period(open, null)),
                        new Placement(4, "S2", 2, 1,
                                period("2026-10-22T09:00:00", null)),
                        new Placement(5, "S1", 4, 4,
                                period(open, null)))));
    }

    /** Запись стенда в формате events.csv. */
    static String[] event(String id, String stand, int exhibit,
                          String type, String start, int duration) {
        return new String[]{id, stand, String.valueOf(exhibit), type,
                start, String.valueOf(duration)};
    }
}
