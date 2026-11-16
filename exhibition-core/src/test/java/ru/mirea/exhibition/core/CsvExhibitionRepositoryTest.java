package ru.mirea.exhibition.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ru.mirea.exhibition.core.data.CsvExhibitionRepository;
import ru.mirea.exhibition.core.data.DataLoadException;
import ru.mirea.exhibition.core.domain.PublicationVersion;

class CsvExhibitionRepositoryTest {

    @TempDir
    Path temp;

    static Stream<Arguments> malformedRows() {
        return Stream.of(
                Arguments.of("exhibitions.csv", "1;title;2026-10-16"),
                Arguments.of("exhibits.csv", "1;1"),
                Arguments.of("stands.csv", "S1;name;hall"),
                Arguments.of("versions.csv", "1;1;1"),
                Arguments.of("placements.csv", "1;S1;1;1;2026-10-15T09:00:00"),
                Arguments.of("versions.csv", "1;1;1;;;extra"));
    }

    @ParameterizedTest(name = "Обрезанная или лишняя колонка: {0}")
    @MethodSource("malformedRows")
    void rejectsWrongColumnCount(String file, String row) throws IOException {
        Files.writeString(temp.resolve(file), "header\n" + row);
        CsvExhibitionRepository repo = new CsvExhibitionRepository(temp);
        DataLoadException e = assertThrows(DataLoadException.class, () -> {
            switch (file) {
                case "exhibitions.csv" -> repo.exhibitions();
                case "exhibits.csv" -> repo.exhibits();
                case "stands.csv" -> repo.stands();
                case "versions.csv" -> repo.versions();
                case "placements.csv" -> repo.placements();
                default -> throw new AssertionError(file);
            }
        });
        assertTrue(e.getMessage().startsWith(file + ", строка 2"));
    }

    @Test
    @DisplayName("Явно пустое окончание периода и черновик разрешены")
    void explicitEmptyColumnsAreAllowed() throws IOException {
        Files.writeString(temp.resolve("placements.csv"),
                "header\n1;S1;1;1;2026-10-15T09:00:00;");
        Files.writeString(temp.resolve("versions.csv"),
                "header\n1;1;1;;\n2;1;2;2026-10-15T09:00:00;");
        CsvExhibitionRepository repo = new CsvExhibitionRepository(temp);
        assertTrue(repo.placements().get(0).period().isOpen());
        assertTrue(repo.versions().get(0).isDraft());
        assertTrue(repo.versions().get(1).period().isOpen());
    }

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
