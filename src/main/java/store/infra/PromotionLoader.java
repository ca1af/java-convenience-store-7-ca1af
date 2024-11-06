package store.infra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import store.domain.Promotion;

public class PromotionLoader {
    private static final String FILE_PATH = "src/main/resources/promotions.md";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DELIMITER = ",";
    private static final int EXPECTED_FIELD_COUNT = 5;

    public List<Promotion> loadPromotions() throws IOException {
        List<String> lines = readFileLines();
        return parsePromotions(lines);
    }

    private List<String> readFileLines() throws IOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(InfraErrorMessage.FILE_NOT_FOUND.getMessage());
        }
        return Files.readAllLines(path);
    }

    private List<Promotion> parsePromotions(List<String> lines) {
        return lines.stream()
                .skip(1) // Skip header
                .map(this::createPromotion)
                .toList();
    }

    private Promotion createPromotion(String line) {
        String[] fields = line.split(DELIMITER);
        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_PROMOTION_FORMAT.getMessage());
        }

        String name = fields[0].trim();
        int buyQuantity = parseInteger(fields[1]);
        int getQuantity = parseInteger(fields[2]);
        LocalDate startDate = parseDate(fields[3]);
        LocalDate endDate = parseDate(fields[4]);

        return new Promotion(name, buyQuantity, getQuantity, startDate, endDate);
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_INTEGER.getMessage());
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (Exception e) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_DATE_FORMAT.getMessage());
        }
    }
}
