package store.domain;

public enum DomainErrorMessage {
    INVALID_QUANTITY("주문 수량이 부적절합니다."),
    QUANTITY_EXCEEDED("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요."),
    INVALID_INPUT("잘못된 입력입니다. 다시 입력해 주세요."),

    NOT_IDENTICAL("제품 목록에는 동일한 제품 종류만 포함되어야 합니다."),

    INVALID_PRODUCT_NAME("");

    ;
    private static final String ERROR_PREFIX = "[ERROR] ";
    private final String message;

    DomainErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return ERROR_PREFIX + message;
    }
}
