package store.presentation;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDateTime;
import java.util.List;
import store.domain.MemberShip;
import store.domain.Order;
import store.domain.Orders;
import store.infra.FilerLoaderProductRepository;
import store.presentation.view.InputView;
import store.presentation.view.OutputView;

public class ConvenienceStoreController {
    private final InputView inputView;
    private final OutputView outputView;
    private final FilerLoaderProductRepository filerLoaderProductRepository;
    private final OrderParser orderParser;

    public ConvenienceStoreController(InputView inputView, OutputView outputView,
                                      FilerLoaderProductRepository filerLoaderProductRepository,
                                      OrderParser orderParser) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.filerLoaderProductRepository = filerLoaderProductRepository;
        this.orderParser = orderParser;
    }

    public void printStart() {
        outputView.printStart();
        outputView.printStocks(filerLoaderProductRepository.toString());
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
        ReceiptFormatter receiptFormatter = finalizePurchase(orders, membership, isMembershipDiscountApplicable());
        outputView.printReceipt(receiptFormatter.format());
        orders.decreaseAmount();
    }

    private ReceiptFormatter finalizePurchase(Orders orders, MemberShip memberShip, boolean memberShipApplicable) {
        if (memberShipApplicable) {
            int discount = memberShip.applyDiscount(orders);
            return new ReceiptFormatter(orders, discount);
        }

        return new ReceiptFormatter(orders, 0);
    }

    private Orders retrieveOrdersFromInput(LocalDateTime currentOrderDate) {
        String userOrderInput = inputView.getOrders();
        List<UserOrder> parsedUserOrders = orderParser.parseInput(userOrderInput);
        return convertToDomainOrders(parsedUserOrders, currentOrderDate);
    }

    private Orders convertToDomainOrders(List<UserOrder> parsedUserOrders, LocalDateTime orderDate) {
        List<Order> domainOrders = parsedUserOrders.stream()
                .map(each -> each.toDomain(filerLoaderProductRepository.findAllByName(each.productName()), orderDate))
                .toList();
        return new Orders(domainOrders);
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
            order.subtract(fallbackItemCount);
        }
    }

    private void handleUnclaimedFreeItems(Order order) {
        String freeItemDecision = inputView.getUnclaimedFreeItemWanted(order.getProductName());
        if (freeItemDecision.equalsIgnoreCase("Y")) {
            order.addQuantity();
        }
    }

    private boolean isMembershipDiscountApplicable() {
        return inputView.addMembershipDiscount().equalsIgnoreCase("Y");
    }
}
