package store.domain;

import java.time.LocalDateTime;

public record Promotion(String name, int buy, int get, LocalDateTime startDate, LocalDateTime endDate) {
    public boolean hasUnclaimedFreeItem(int quantity){
        return quantity % (buy + get) == buy;
    }

    public boolean applicable(LocalDateTime now) {
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    public int promotionGetCount(int quantity){
        return quantity / (buy + get);
    }
}
