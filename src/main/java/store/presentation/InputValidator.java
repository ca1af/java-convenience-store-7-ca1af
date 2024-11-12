package store.presentation;

import java.util.regex.Pattern;

public class InputValidator {
    private static final String INPUT_PATTERN = "\\[.+-\\d+](,\\[.+-\\d+])*";

    private InputValidator() {
        throw new UnsupportedOperationException();
    }

    public static void validateProduct(String input) {
        validateBlank(input);
        validateProductPattern(input);
    }

    public static void validateYesOrNo(String input) {
        validateBlank(input);
        validateYesOrNoPattern(input);
    }

    private static void validateProductPattern(String input) {
        if (!Pattern.matches(INPUT_PATTERN, input)) {
            throw new IllegalArgumentException(PresentationErrorMessage.INVALID_PRODUCT_INPUT.getMessage());
        }
    }

    private static void validateBlank(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(PresentationErrorMessage.BLANK_INPUT.getMessage());
        }
    }

    private static void validateYesOrNoPattern(String input) {
        if (!"Y".equals(input) && !"N".equals(input)) {
            throw new IllegalArgumentException(PresentationErrorMessage.INVALID_INPUT.getMessage());
        }
    }
}
