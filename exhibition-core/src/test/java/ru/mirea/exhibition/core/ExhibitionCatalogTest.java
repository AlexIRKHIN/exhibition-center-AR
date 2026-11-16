package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ru.mirea.exhibition.core.catalog.ExhibitionCatalog;
import ru.mirea.exhibition.core.domain.Exhibition;
import ru.mirea.exhibition.core.domain.Exhibit;
import ru.mirea.exhibition.core.domain.Stand;
import ru.mirea.exhibition.core.domain.Period;
import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.domain.PublicationVersion;

class ExhibitionCatalogTest {

    private static final Exhibition EXHIBITION = new Exhibition(1, "Выставка",
            LocalDate.of(2026, 10, 16), LocalDate.of(2026, 12, 20));
    private static final Exhibit EXHIBIT = new Exhibit(1, 1, "Экспонат");
    private static final Stand STAND = new Stand("S1", "Стенд", "Зал", "киоск");
    private static final Period PERIOD = Period.openFrom(
            TestData.at("2026-10-16T09:00:00"));
    private static final Placement PLACEMENT = new Placement(1, "S1", 1, 1, PERIOD);
    private static final PublicationVersion VERSION =
            new PublicationVersion(1, 1, 1, PERIOD);

    static Stream<Arguments> invalidCatalogs() {
        return Stream.of(
                Arguments.of(new InMemoryExhibitionRepository(
                        List.of(EXHIBITION, EXHIBITION), List.of(EXHIBIT),
                        List.of(STAND), List.of(VERSION), List.of(PLACEMENT)), "Выставка", "1"),
                Arguments.of(new InMemoryExhibitionRepository(
                        List.of(EXHIBITION), List.of(EXHIBIT, new Exhibit(1, 1, "Другой")),
                        List.of(STAND), List.of(VERSION), List.of(PLACEMENT)), "Экспонат", "1"),
                Arguments.of(new InMemoryExhibitionRepository(
                        List.of(EXHIBITION), List.of(EXHIBIT), List.of(STAND, STAND),
                        List.of(VERSION), List.of(PLACEMENT)), "Стенд", "S1"),
                Arguments.of(new InMemoryExhibitionRepository(
                        List.of(EXHIBITION), List.of(EXHIBIT), List.of(STAND),
                        List.of(VERSION, VERSION), List.of(PLACEMENT)), "Версия", "1"),
                Arguments.of(new InMemoryExhibitionRepository(
                        List.of(EXHIBITION), List.of(EXHIBIT), List.of(STAND),
                        List.of(VERSION), List.of(PLACEMENT, PLACEMENT)), "Размещение", "1"),
                Arguments.of(new InMemoryExhibitionRepository(
                        List.of(EXHIBITION), List.of(new Exhibit(1, 99, "Нет выставки")),
                        List.of(STAND), List.of(VERSION), List.of(PLACEMENT)), "выставка", "99"),
                Arguments.of(new InMemoryExhibitionRepository(
                        List.of(EXHIBITION), List.of(EXHIBIT), List.of(STAND),
                        List.of(new PublicationVersion(2, 99, 1, null)),
                        List.of(PLACEMENT)), "экспонат", "99"),
                Arguments.of(new InMemoryExhibitionRepository(
                        List.of(EXHIBITION), List.of(EXHIBIT), List.of(STAND),
                        List.of(VERSION), List.of(new Placement(2, "missing", 1, 1, PERIOD))),
                        "стенд", "missing"),
                Arguments.of(new InMemoryExhibitionRepository(
                        List.of(EXHIBITION), List.of(EXHIBIT), List.of(STAND),
                        List.of(VERSION), List.of(new Placement(2, "S1", 99, 1, PERIOD))),
                        "экспонат", "99"));
    }

    @ParameterizedTest(name = "Некорректный каталог: {1}, id={2}")
    @MethodSource("invalidCatalogs")
    void rejectsDuplicatesAndBrokenReferences(InMemoryExhibitionRepository repo,
                                               String entity, String id) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new ExhibitionCatalog(repo));
        assertTrue(e.getMessage().contains(entity), e.getMessage());
        assertTrue(e.getMessage().contains(id), e.getMessage());
    }
}
