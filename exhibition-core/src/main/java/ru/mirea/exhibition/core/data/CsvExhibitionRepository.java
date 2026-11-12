package ru.mirea.exhibition.core.data;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import ru.mirea.exhibition.core.domain.Exhibit;
import ru.mirea.exhibition.core.domain.Exhibition;
import ru.mirea.exhibition.core.domain.Period;
import ru.mirea.exhibition.core.domain.Placement;
import ru.mirea.exhibition.core.domain.PublicationVersion;
import ru.mirea.exhibition.core.domain.Stand;

/**
 * Серверные данные из CSV-файлов каталога: exhibitions.csv,
 * exhibits.csv, stands.csv, versions.csv, placements.csv.
 * Столбцы файлов соответствуют атрибутам логической модели данных.
 */
public final class CsvExhibitionRepository
        implements ExhibitionRepository {

    private final Path directory;

    public CsvExhibitionRepository(Path directory) {
        this.directory = directory;
    }

    @Override
    public List<Exhibition> exhibitions() {
        // id;title;opens;closes
        return CsvFile.read(file("exhibitions.csv"),
                r -> new Exhibition(r.integer(0), r.text(1), r.date(2),
                        r.date(3)));
    }

    @Override
    public List<Exhibit> exhibits() {
        // id;exhibition_id;title
        return CsvFile.read(file("exhibits.csv"),
                r -> new Exhibit(r.integer(0), r.integer(1), r.text(2)));
    }

    @Override
    public List<Stand> stands() {
        // id;name;hall;type
        return CsvFile.read(file("stands.csv"),
                r -> new Stand(r.text(0), r.text(1), r.text(2),
                        r.text(3)));
    }

    @Override
    public List<PublicationVersion> versions() {
        // version_id;exhibit_id;version_no;published_from;published_to
        return CsvFile.read(file("versions.csv"), r -> {
            LocalDateTime from = r.dateTimeOrNull(3);
            Period period = from == null
                    ? null : new Period(from, r.dateTimeOrNull(4));
            return new PublicationVersion(r.integer(0), r.integer(1),
                    r.integer(2), period);
        });
    }

    @Override
    public List<Placement> placements() {
        // placement_id;stand_id;exhibit_id;display_order;valid_from;
        // valid_to
        return CsvFile.read(file("placements.csv"),
                r -> new Placement(r.integer(0), r.text(1), r.integer(2),
                        r.integer(3), new Period(r.dateTimeOrNull(4),
                        r.dateTimeOrNull(5))));
    }

    private Path file(String name) {
        return directory.resolve(name);
    }
}
