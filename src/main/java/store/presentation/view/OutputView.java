package store.presentation.view;

public class OutputView {
    private static final String START_MESSAGE = "안녕하세요. W편의점입니다." + System.lineSeparator() + "현재 보유하고 있는 상품입니다.";

    public void printStart() {
        System.out.println(START_MESSAGE);
    }

    public void printStocks(String stocks) {
        System.out.println(System.lineSeparator() + stocks);
    }

    public void printReceipt(String receipt) {
        System.out.println(receipt);
    }
}
