package ru.mirea.exhibition.core.data;

import java.nio.file.Path;

/**
 * Ошибка в серверных данных: файл не прочитан или строка не
 * соответствует формату. В отличие от ошибочных событий стенда,
 * которые отклоняются с причиной, ошибка в справочных данных
 * останавливает загрузку: работать с неверным каталогом нельзя.
 */
public class DataLoadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataLoadException(Path file, int line, String message) {
        super(file.getFileName() + ", строка " + line + ": " + message);
    }

    public DataLoadException(Path file, Throwable cause) {
        super("Не удалось прочитать файл " + file + ": "
                + cause.getMessage(), cause);
    }
}
