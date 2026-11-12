package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ru.mirea.exhibition.core.data.CsvExhibitionRepository;
import ru.mirea.exhibition.core.data.DataLoadException;
import ru.mirea.exhibition.core.domain.PublicationVersion;

class CsvExhibitionRepositoryTest {

    static final Path DEMO_DATA = Path.of("..", "algorithms", "data");

    @Test
    @DisplayName("Тестовые данные проекта читаются полностью")
    void readsDemoData() {
        CsvExhibitionRepository repo =
                new CsvExhibitionRepository(DEMO_DATA);
        assertEquals(1, repo.exhibitions().size());
        assertEquals(9, repo.exhibits().size());
        assertEquals(3, repo.stands().size());
        assertEquals(11, repo.placements().size());
        List<PublicationVersion> versions = repo.versions();
        assertEquals(11, versions.size());
        assertEquals(1L, versions.stream()
                .filter(PublicationVersion::isDraft).count());
    }

    @Test
    @DisplayName("Ошибка в данных сообщается с файлом и номером строки")
    void reportsBadLine() throws IOException {
        Path dir = Files.createTempDirectory("catalog");
        Files.writeString(dir.resolve("placements.csv"), String.join("\n",
                "placement_id;stand_id;exhibit_id;display_order;from;to",
                "1;S1;1;1;2026-10-15T09:00:00;",
                "2;S1;2;2;2026-10-21T00:00:00;2026-10-20T00:00:00"),
                StandardCharsets.UTF_8);
        DataLoadException e = assertThrows(DataLoadException.class,
                () -> new CsvExhibitionRepository(dir).placements());
        assertTrue(e.getMessage().startsWith("placements.csv, строка 3"));
    }
}
