package store.presentation;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDateTime;
import store.application.ConvenienceStoreService;
import store.application.ReceiptFormatter;
import store.domain.MemberShip;
import store.domain.Order;
import store.domain.Orders;

public class ConvenienceStoreController {
    private final InputView inputView;
    private final OutputView outputView;
    private final ConvenienceStoreService convenienceStoreService;

    public ConvenienceStoreController(InputView inputView, OutputView outputView,
                                      ConvenienceStoreService convenienceStoreService) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.convenienceStoreService = convenienceStoreService;
    }

    public void printStart() {
        outputView.printStart();
        outputView.printStocks(convenienceStoreService.getStocks());
    }

    public void run() {
        MemberShip membership = new MemberShip();
        do {
            handleOrder(membership, DateTimes.now());
            String continueOrder = inputView.getWantMore();
            if (continueOrder.equalsIgnoreCase("N")) {
                break;
            }
        } while (true);
    }

    public void handleOrder(MemberShip membership, LocalDateTime currentOrderDate) {
        printStart();
        Orders orders = RetryHandler.retry(() -> retrieveOrdersFromInput(currentOrderDate));
        processOrderDetails(orders);
        ReceiptFormatter receiptFormatter = convenienceStoreService.finalizePurchase(
                orders, membership, isMembershipDiscountApplicable()
        );
        outputView.printReceipt(receiptFormatter.format());
        orders.decreaseAmount();
    }

    private Orders retrieveOrdersFromInput(LocalDateTime currentOrderDate) {
        String userOrderInput = inputView.getOrders();
        return convenienceStoreService.createOrdersFromInput(userOrderInput, currentOrderDate);
    }

    private void processOrderDetails(Orders orders) {
        orders.getUnclaimedFreeItemOrder().forEach(this::handleUnclaimedFreeItems);
        orders.getFallBackToNormalOrders().forEach(this::handleFallbackItems);
    }

    private void handleFallbackItems(Order order) {
        if (!order.hasFallbackToNormal()) {
            return;
        }

        int fallbackItemCount = order.countFallbackToNormal();
        String fallbackPurchaseDecision = inputView.askToPurchaseNormalItems(order.getProductName(), fallbackItemCount);
        if (fallbackPurchaseDecision.equalsIgnoreCase("N")) {
            convenienceStoreService.rejectFallbackItems(order, fallbackItemCount);
        }
    }

    private void handleUnclaimedFreeItems(Order order) {
        String freeItemDecision = inputView.getUnclaimedFreeItemWanted(order.getProductName());
        if (freeItemDecision.equalsIgnoreCase("Y")) {
            convenienceStoreService.addFreeItem(order);
        }
    }

    private boolean isMembershipDiscountApplicable() {
        return inputView.addMembershipDiscount().equalsIgnoreCase("Y");
    }
}
