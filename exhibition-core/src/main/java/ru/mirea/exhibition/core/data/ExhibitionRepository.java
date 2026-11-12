package ru.mirea.exhibition.core.data;

import java.util.List;

import ru.mirea.exhibition.core.domain.Exhibit;
import ru.mirea.exhibition.core.domain.Exhibition;
import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.domain.PublicationVersion;
import ru.mirea.exhibition.core.domain.Stand;

/**
 * Источник серверных данных выставочного центра. Сервисы работают
 * только с этим интерфейсом и не знают, откуда берутся данные:
 * сейчас это CSV-файлы ({@link CsvExhibitionRepository}),
 * на следующем этапе — база данных PostgreSQL.
 */
public interface ExhibitionRepository {

    List<Exhibition> exhibitions();

    List<Exhibit> exhibits();

    List<Stand> stands();

    List<PublicationVersion> versions();

    List<Placement> placements();
}
