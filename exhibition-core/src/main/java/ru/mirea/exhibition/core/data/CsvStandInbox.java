package ru.mirea.exhibition.core.data;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.mirea.exhibition.core.sync.StandContent;

/**
 * Данные, которые присылают стенды: манифест кэша стенда
 * (manifest_&lt;id стенда&gt;.csv) и пакет событий (events.csv).
 * События возвращаются «как есть»: их корректность проверяет
 * служба регистрации, а не чтение файла.
 */
public final class CsvStandInbox {

    private final Path directory;

    public CsvStandInbox(Path directory) {
        this.directory = directory;
    }

    /** Манифест стенда: id экспоната → номер версии в кэше. */
    public StandContent manifest(String standId) {
        Path file = directory.resolve("manifest_" + standId + ".csv");
        Map<Integer, Integer> versions = new HashMap<>();
        for (CsvRow row : CsvFile.rows(file)) {
            versions.put(row.integer(0), row.integer(1));
        }
        return new StandContent(versions);
    }

    /** Пакет событий: каждая запись — массив полей строки. */
    public List<String[]> eventPackage() {
        return CsvFile.rows(directory.resolve("events.csv")).stream()
                .map(CsvRow::fields)
                .toList();
    }
}
