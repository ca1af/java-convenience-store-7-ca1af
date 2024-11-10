package store.presentation;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDateTime;
import java.util.List;
import store.domain.MemberShip;
import store.domain.OrderProduct;
import store.domain.Order;
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
        String userOrderInput = inputView.getOrders();
        List<UserOrder> parsedUserOrders = orderParser.parseInput(userOrderInput);
        return convertToDomainOrders(parsedUserOrders, currentOrderDate);
    }

    private Order convertToDomainOrders(List<UserOrder> parsedUserOrders, LocalDateTime orderDate) {
        List<OrderProduct> domainOrderProducts = parsedUserOrders.stream()
                .map(each -> each.toDomain(filerLoaderProductRepository.findAllByName(each.productName()), orderDate))
                .toList();
        return new Order(domainOrderProducts);
    }

    private void processOrderDetails(Order order) {
        order.getUnclaimedFreeItemOrder().forEach(this::handleUnclaimedFreeItems);
        order.getFallBackToNormalOrders().forEach(this::handleFallbackItems);
    }

    private void handleFallbackItems(OrderProduct orderProduct) {
        if (!orderProduct.hasFallbackToNormal()) {
            return;
        }

        int fallbackItemCount = orderProduct.countFallbackToNormal();
        String fallbackPurchaseDecision = inputView.askToPurchaseNormalItems(orderProduct.getProductName(), fallbackItemCount);
        if (fallbackPurchaseDecision.equalsIgnoreCase("N")) {
            orderProduct.subtract(fallbackItemCount);
        }
    }

    private void handleUnclaimedFreeItems(OrderProduct orderProduct) {
        String freeItemDecision = inputView.getUnclaimedFreeItemWanted(orderProduct.getProductName());
        if (freeItemDecision.equalsIgnoreCase("Y")) {
            orderProduct.addQuantity();
        }
    }

    private boolean isMembershipDiscountApplicable() {
        return inputView.addMembershipDiscount().equalsIgnoreCase("Y");
    }
}
