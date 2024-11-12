package store.domain.order;

public interface OrderItem {
    void addQuantity();
    void subtract(int amount);
    boolean hasEnoughStock();
    void decreaseStocks();
    int getTotalPrice();
    int getOrderQuantity();
    int getProductPrice();
    String getProductName();
    int calculatePromotedCount();
    boolean hasUnclaimedFreeItem();
    int getPromotionStock();
    int getNormalProductPrice();
    boolean hasFallbackToNormal();
}
