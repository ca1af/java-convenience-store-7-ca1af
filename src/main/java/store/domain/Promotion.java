package store.domain;

import java.time.LocalDate;

public record Promotion(String name, int buy, int get, LocalDate startDate, LocalDate endDate) {
    public boolean hasRemains(int quantity){
        return quantity % (buy + get) == buy;
    }

    public boolean applicable(LocalDate now) {
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
}
