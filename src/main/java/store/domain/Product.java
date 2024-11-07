package store.domain;

import java.util.Objects;

public record Product(String name, int price, int quantity, Promotion promotion) {
    public boolean promotionExists(){
        return !Objects.isNull(promotion);
    }

    public int getTotalPrice(){
        return price * quantity;
    }

    public boolean hasRemains(int quantity){
        return promotion.hasRemains(quantity);
    }
}
