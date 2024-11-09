package store.presentation;

import java.util.Arrays;
import java.util.List;

public class OrderParser {
    private OrderParser() {
        throw new UnsupportedOperationException();
    }

    public static List<OrderRequestDto> parseInput(String input) {
        input = input.replaceAll("[\\[\\]]", ""); // Remove brackets
        String[] items = input.split(",");

        return Arrays.stream(items).map(OrderParser::parseItem).toList();
    }

    private static OrderRequestDto parseItem(String item) {
        String[] parts = item.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException(PresentationErrorMessage.INVALID_PRODUCT_INPUT.getMessage());
        }
        String productName = parts[0].trim();
        int quantity = parseQuantity(parts[1].trim());
        return new OrderRequestDto(productName, quantity);
    }

    private static int parseQuantity(String quantityStr) {
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                throw new IllegalArgumentException(PresentationErrorMessage.INVALID_QUANTITY.getMessage());
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(PresentationErrorMessage.INVALID_INPUT.getMessage());
        }
    }
}
