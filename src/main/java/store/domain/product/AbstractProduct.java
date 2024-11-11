package store.domain.product;

public abstract class AbstractProduct implements Product {
    private static final String OUT_OF_STOCK_MESSAGE = " 재고 없음";
    private static final String WITH_STOCK_FORMAT = "%s %,d원 %d개";
    private static final String OUT_OF_STOCK_FORMAT = "%s %,d원";
    private static final String MESSAGE_PREFIX = "- ";

    private final String name;
    private final int price;
    protected int quantity;

    protected AbstractProduct(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        if (quantity > 0) {
            return formatWithStock();
        }
        return formatOutOfStock();
    }

    private String formatWithStock() {
        return String.format(WITH_STOCK_FORMAT, MESSAGE_PREFIX + name, price, quantity) + getPromotionInfo();
    }

    private String formatOutOfStock() {
        return String.format(OUT_OF_STOCK_FORMAT, MESSAGE_PREFIX + name, price) + OUT_OF_STOCK_MESSAGE
                + getPromotionInfo();
    }

    protected abstract String getPromotionInfo();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }
}
