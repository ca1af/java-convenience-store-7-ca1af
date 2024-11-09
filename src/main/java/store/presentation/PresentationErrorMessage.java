package store.presentation;

public enum PresentationErrorMessage {
    INVALID_QUANTITY("주문 수량이 부적절합니다. 다시 입력해 주세요."),
    QUANTITY_EXCEEDED("재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요."),
    INVALID_INPUT("잘못된 입력입니다. 다시 입력해 주세요."),
    INVALID_PRODUCT_INPUT("올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요."),
    PRODUCT_NOT_FOUND("존재하지 않는 상품입니다. 다시 입력해 주세요."),
    BLANK_INPUT("입력값이 비어 있습니다. 다시 입력해 주세요."),
    DUPLICATED_PRODUCT_NAME("중복된 상품이 있습니다. 다시 입력 해 주세요"),
    NULL_LINE_PROVIDED("라인이 없습니다. 시스템을 종료합니다."),
    ;


    private static final String ERROR_PREFIX = "[ERROR] ";
    private final String message;

    PresentationErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return ERROR_PREFIX + message;
    }
}
// input -> Service.method 를 한 사이클로 둔다? 어떨까...
// input 에서 한 번 validate 해서 재입력, service 에서 오류시 또 재입력. 괜찮은가?
