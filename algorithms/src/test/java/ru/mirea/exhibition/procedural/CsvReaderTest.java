package ru.mirea.exhibition.procedural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvReaderTest {

    @Test
    @DisplayName("Заголовок и пустые строки пропускаются, поля обрезаются")
    void parsesRows() {
        List<String[]> rows = CsvReader.parse(List.of(
                "id;name",
                " 1 ; Стенд А ",
                "",
                "2;"));

        assertEquals(2, rows.size());
        assertArrayEquals(new String[]{"1", "Стенд А"}, rows.get(0));
        assertArrayEquals(new String[]{"2", ""}, rows.get(1));
    }
}
