package store.presentation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderParser {
    private static final String BRACKETS_REGEX = "[\\[\\]]";
    private static final String ITEM_DELIMITER = ",";
    private static final String PARTS_DELIMITER = "-";
    private static final int EXPECTED_PARTS_COUNT = 2;

    public List<UserOrder> parseInput(String input) {
        input = input.replaceAll(BRACKETS_REGEX, ""); // Remove brackets
        String[] items = input.split(ITEM_DELIMITER);

        List<UserOrder> orderRequests = Arrays.stream(items).map(OrderParser::parseItem).toList();

        checkForDuplicateProductNames(orderRequests);
        return orderRequests;
    }

    private static void checkForDuplicateProductNames(List<UserOrder> orderRequests) {
        Set<String> uniqueProductNames = new HashSet<>();
        for (UserOrder request : orderRequests) {
            if (!uniqueProductNames.add(request.productName())) {
                throw new IllegalArgumentException(PresentationErrorMessage.DUPLICATED_PRODUCT_NAME.getMessage());
            }
        }
    }

    private static UserOrder parseItem(String item) {
        String[] parts = item.split(PARTS_DELIMITER);
        if (parts.length != EXPECTED_PARTS_COUNT) {
            throw new IllegalArgumentException(PresentationErrorMessage.INVALID_PRODUCT_INPUT.getMessage());
        }
        String productName = parts[0].trim();
        int quantity = parseQuantity(parts[1].trim());
        return new UserOrder(productName, quantity);
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
