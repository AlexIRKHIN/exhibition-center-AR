package ru.mirea.exhibition.core.data;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Строка CSV-файла с номером строки. Методы чтения полей сообщают
 * о неверном значении с указанием файла и номера строки.
 */
public final class CsvRow {

    private final Path file;
    private final int line;
    private final String[] fields;

    CsvRow(Path file, int line, String[] fields) {
        this.file = file;
        this.line = line;
        this.fields = fields;
    }

    public int line() {
        return line;
    }

    public int size() {
        return fields.length;
    }

    /** Поля строки как есть (копия массива). */
    public String[] fields() {
        return fields.clone();
    }

    public String text(int index) {
        if (index >= fields.length) {
            throw error("нет поля №" + (index + 1));
        }
        return fields[index];
    }

    public int integer(int index) {
        try {
            return Integer.parseInt(text(index));
        } catch (NumberFormatException e) {
            throw error("поле №" + (index + 1) + " не число: "
                    + text(index));
        }
    }

    public LocalDate date(int index) {
        try {
            return LocalDate.parse(text(index));
        } catch (DateTimeParseException e) {
            throw error("поле №" + (index + 1) + " не дата: "
                    + text(index));
        }
    }

    /** Дата и время; пустое поле — null (открытый период, черновик). */
    public LocalDateTime dateTimeOrNull(int index) {
        String value = index < fields.length ? fields[index] : "";
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw error("поле №" + (index + 1)
                    + " не дата и время: " + value);
        }
    }

    public DataLoadException error(String message) {
        return new DataLoadException(file, line, message);
    }
}
