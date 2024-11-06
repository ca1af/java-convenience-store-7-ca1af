package store.infra.loader;

import java.time.LocalDate;
import java.util.List;
import store.domain.Promotion;

public class PromotionLoader extends FileLoader<Promotion> {
    private static final int EXPECTED_FIELD_COUNT = 5;
    private static final String FILE_PATH = "src/main/resources/promotions.md";

    public List<Promotion> loadPromotions() {
        return load(FILE_PATH);
    }

    @Override
    protected List<Promotion> load(String filePath) {
        List<String> lines = readFileLines(filePath);
        return parseLines(lines);
    }

    @Override
    protected List<Promotion> parseLines(List<String> lines) {
        return lines.stream()
                .skip(1) // Skip header
                .map(this::createPromotion)
                .toList();
    }

    private Promotion createPromotion(String line) {
        String[] fields = getDeclaredFields(line, EXPECTED_FIELD_COUNT);
        String name = fields[0].trim();
        int buyQuantity = parseInteger(fields[1]);
        int getQuantity = parseInteger(fields[2]);
        LocalDate startDate = parseDate(fields[3]);
        LocalDate endDate = parseDate(fields[4]);
        return new Promotion(name, buyQuantity, getQuantity, startDate, endDate);
    }
}
