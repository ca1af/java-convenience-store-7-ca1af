package store.infra;

public enum InfraErrorMessage {
    FILE_NOT_FOUND("프로모션 파일을 찾을 수 없습니다"),
    INVALID_PROMOTION_FORMAT("잘못된 프로모션 형식입니다"),
    INVALID_INTEGER("유효하지 않은 정수 값입니다"),
    INVALID_DATE_FORMAT("유효하지 않은 날짜 형식 입니다"),
    INVALID_PROMOTION_NAME("잘못된 프로모션 이름입니다."),

    ;

    private static final String ERROR_PREFIX = "[ERROR]";
    private final String message;

    InfraErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return ERROR_PREFIX + message;
    }
}
