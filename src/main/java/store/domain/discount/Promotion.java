package store.domain.discount;

import java.time.LocalDateTime;
import store.domain.DomainErrorMessage;

public record Promotion(String name, int buy, int get, LocalDateTime startDate, LocalDateTime endDate) {
    public boolean hasUnclaimedFreeItem(int quantity) {
        return quantity % (buy + get) == buy;
    }

    public boolean applicable(LocalDateTime now) {
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    public int promotionGetCount(int quantity) {
        return quantity / (buy + get);
    }

    public Promotion {
        validate(startDate, endDate);
    }

    private void validate(LocalDateTime startDate, LocalDateTime endDate){
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_PROMOTION_DATE.getMessage());
        }
    }
}
