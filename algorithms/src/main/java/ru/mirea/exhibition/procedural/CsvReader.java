package ru.mirea.exhibition.procedural;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Чтение табличных данных из CSV-файлов (разделитель «;», кодировка UTF-8).
 * Первая строка файла считается заголовком и пропускается.
 */
public final class CsvReader {

    private CsvReader() {
    }

    public static List<String[]> read(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        return parse(lines);
    }

    public static List<String[]> parse(List<String> lines) {
        List<String[]> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split(";", -1);
            for (int j = 0; j < fields.length; j++) {
                fields[j] = fields[j].trim();
            }
            rows.add(fields);
        }
        return rows;
    }
}
