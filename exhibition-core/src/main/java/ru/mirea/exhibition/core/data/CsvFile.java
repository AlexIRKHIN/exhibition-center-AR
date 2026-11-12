package ru.mirea.exhibition.core.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Чтение CSV-файла (разделитель «;», UTF-8, первая строка —
 * заголовок, пустые строки пропускаются) с преобразованием каждой
 * строки в объект.
 */
public final class CsvFile {

    private CsvFile() {
    }

    public static List<CsvRow> rows(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataLoadException(file, e);
        }
        List<CsvRow> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split(";", -1);
            for (int j = 0; j < fields.length; j++) {
                fields[j] = fields[j].trim();
            }
            rows.add(new CsvRow(file, i + 1, fields));
        }
        return rows;
    }

    /**
     * Читает файл и преобразует строки в объекты. Если конструктор
     * объекта отверг значения (например, период с концом раньше
     * начала), ошибка дополняется номером строки.
     */
    public static <T> List<T> read(Path file, Function<CsvRow, T> mapper) {
        List<T> result = new ArrayList<>();
        for (CsvRow row : rows(file)) {
            try {
                result.add(mapper.apply(row));
            } catch (IllegalArgumentException | NullPointerException e) {
                throw row.error(e.getMessage());
            }
        }
        return result;
    }
}
