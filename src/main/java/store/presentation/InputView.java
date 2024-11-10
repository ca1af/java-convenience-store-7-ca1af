package store.presentation;

import camp.nextstep.edu.missionutils.Console;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class InputView {
    private static final String INPUT_PRODUCT_AND_QUANTITY = "구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])";
    private static final String FREE_ITEM_REMAINING = "현재 %s은(는) 1개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)";
    private static final String FALLBACK_TO_NORMAL = "현재 %s %d개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)";
    private static final String MEMBERSHIP_NEEDED = "멤버십 할인을 받으시겠습니까? (Y/N)";
    private static final String WANT_MORE = "감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)";

    public String getUnclaimedFreeItemWanted(String productName) {
        return askWithValidation(InputValidator::validateYesOrNo, String.format(FREE_ITEM_REMAINING, productName)
        );
    }

    public String askToPurchaseNormalItems(String productName, int quantity) {
        return askWithValidation(InputValidator::validateYesOrNo,
                String.format(FALLBACK_TO_NORMAL, productName, quantity)
        );
    }

    public String addMembershipDiscount() {
        return askWithValidation(InputValidator::validateYesOrNo, MEMBERSHIP_NEEDED);
    }

    public String getOrders() {
        return askWithValidation(
                InputValidator::validateProduct,
                INPUT_PRODUCT_AND_QUANTITY
        );
    }

    public String getWantMore() {
        return askWithValidation(InputValidator::validateYesOrNo, WANT_MORE);
    }

    private String askWithValidation(Consumer<String> validator, String message) {
        return RetryHandler.retry(() -> {
            printMessage(message);
            String input = getInput();
            validator.accept(input);
            return input;
        });
    }

    private String getInput() {
        try {
            return Console.readLine();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(PresentationErrorMessage.NULL_LINE_PROVIDED.getMessage());
        }
    }

    private void printMessage(String message) {
        System.out.println(System.lineSeparator() + message);
    }
}
