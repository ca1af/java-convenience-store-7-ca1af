package store.infra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import store.domain.Promotion;

public class PromotionFactory {
    private final Map<String, Promotion> promotions = new HashMap<>();

    public PromotionFactory(List<Promotion> loadedPromotions) {
        loadedPromotions.forEach(promotion -> promotions.put(promotion.name(), promotion));
    }

    public Promotion getPromotion(String promotionName) {
        if ("null".equals(promotionName)) {
            return null;
        }
        if (!promotions.containsKey(promotionName)) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_PROMOTION_NAME.getMessage());
        }
        return promotions.get(promotionName);
    }
}
