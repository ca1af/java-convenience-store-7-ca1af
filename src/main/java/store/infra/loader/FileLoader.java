package store.infra.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import store.infra.InfraErrorMessage;

public abstract class FileLoader<T> {
    protected static final String DELIMITER = ",";
    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected List<String> readFileLines(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(InfraErrorMessage.FILE_NOT_FOUND.getMessage());
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new IllegalArgumentException(InfraErrorMessage.FILE_READ_FAILED.getMessage());
        }
    }

    protected int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_INTEGER.getMessage());
        }
    }

    protected String[] getDeclaredFields(String line, int expectedFieldCount) {
        String[] fields = line.split(DELIMITER);
        if (fields.length != expectedFieldCount) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_PROMOTION_FORMAT.getMessage());
        }
        return fields;
    }

    protected LocalDateTime parseDate(String value) {
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT).atStartOfDay();
        } catch (Exception e) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_DATE_FORMAT.getMessage());
        }
    }

    protected abstract List<T> parseLines(List<String> lines);

    protected abstract List<T> load(String filePath);
}
