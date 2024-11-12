package store.domain;

public enum DomainErrorMessage {
    INVALID_QUANTITY("주문 수량이 부적절합니다."),
    QUANTITY_EXCEEDED("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요."),
    INVALID_INPUT("잘못된 입력입니다. 다시 입력해 주세요."),

    NOT_IDENTICAL("주문 상품에는 동일한 제품 종류만 포함되어야 합니다."),
    INTERNAL_SERVER_ERROR("시스템 오류가 발생했습니다. 어플리케이션을 다시 시작 해 주세요"),
    PROMOTION_CANNOT_BE_NULL("프로모션 상품은 프로모션이 반드시 존재해야 합니다."),

    INVALID_PROMOTION_DATE("프로모션 날짜가 부적절합니다."),
    NORMAL_PRODUCT_NEEDED("일반 상품은 반드시 포함되어야 합니다."),
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
