package ru.mirea.exhibition.core;

import java.util.List;

import ru.mirea.exhibition.core.data.ExhibitionRepository;
import ru.mirea.exhibition.core.domain.Exhibit;
import ru.mirea.exhibition.core.domain.Exhibition;
import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.domain.PublicationVersion;
import ru.mirea.exhibition.core.domain.Stand;

/**
 * Хранилище в памяти для тестов: тесты работают с тем же интерфейсом,
 * что и программа, но без файлов.
 */
record InMemoryExhibitionRepository(List<Exhibition> exhibitions,
                                    List<Exhibit> exhibits,
                                    List<Stand> stands,
                                    List<PublicationVersion> versions,
                                    List<Placement> placements)
        implements ExhibitionRepository {
}
