package store.presentation;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDateTime;
import store.application.OrderService;
import store.application.ReceiptFormatter;
import store.application.RetryHandler;
import store.domain.discount.MemberShip;
import store.domain.order.Order;
import store.domain.order.PromotionOrderItem;
import store.presentation.view.InputView;
import store.presentation.view.OutputView;

public class ConvenienceStoreController {
    private final InputView inputView;
    private final OutputView outputView;
    private final OrderService orderService;

    public ConvenienceStoreController(InputView inputView, OutputView outputView,
                                      OrderService orderService) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.orderService = orderService;
    }

    public void printStart() {
        outputView.printStart();
        outputView.printStocks(orderService.getStocks());
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
        Order order = RetryHandler.retry(() -> retrieveOrdersFromInput(currentOrderDate));
        processOrderDetails(order);
        ReceiptFormatter receiptFormatter = finalizePurchase(order, membership, isMembershipDiscountApplicable());
        outputView.printReceipt(receiptFormatter.format());
        order.decreaseAmount();
    }

    private ReceiptFormatter finalizePurchase(Order order, MemberShip memberShip, boolean memberShipApplicable) {
        if (memberShipApplicable) {
            int discount = memberShip.applyDiscount(order);
            return new ReceiptFormatter(order, discount);
        }

        return new ReceiptFormatter(order, 0);
    }

    private Order retrieveOrdersFromInput(LocalDateTime currentOrderDate) {
        return orderService.retrieveOrdersFromInput(inputView.getOrders(), currentOrderDate);
    }

    private void processOrderDetails(Order order) {
        order.getUnclaimedFreeItemOrder().forEach(this::handleUnclaimedFreeItems);
        order.getFallBackToNormalOrders().forEach(this::handleFallbackItems);
    }

    private void handleFallbackItems(PromotionOrderItem orderProduct) {
        if (!orderProduct.hasFallbackToNormal()) {
            return;
        }

        int fallbackItemCount = orderProduct.countFallbackToNormal();
        String fallbackPurchaseDecision = inputView.askToPurchaseNormalItems(orderProduct.getProductName(), fallbackItemCount);
        if (fallbackPurchaseDecision.equalsIgnoreCase("N")) {
            orderProduct.subtract(fallbackItemCount);
        }
    }

    private void handleUnclaimedFreeItems(PromotionOrderItem orderProduct) {
        String freeItemDecision = inputView.getUnclaimedFreeItemWanted(orderProduct.getProductName());
        if (freeItemDecision.equalsIgnoreCase("Y")) {
            orderProduct.addQuantity();
        }
    }

    private boolean isMembershipDiscountApplicable() {
        return inputView.addMembershipDiscount().equalsIgnoreCase("Y");
    }
}
