package store.presentation;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import store.domain.MemberShip;
import store.domain.Order;
import store.domain.Orders;
import store.domain.ReceiptPrinter;
import store.infra.FilerLoaderProductRepository;

public class ConvenienceStoreController {
    private final InputView inputView;
    private final OutputView outputView;
    private final FilerLoaderProductRepository filerLoaderProductRepository;

    public ConvenienceStoreController(InputView inputView, OutputView outputView, FilerLoaderProductRepository filerLoaderProductRepository) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.filerLoaderProductRepository = filerLoaderProductRepository;
    }

    public void printStart(){
        outputView.printStart();
        outputView.printStocks(filerLoaderProductRepository.toString());
    }

    public void run2() {
        MemberShip memberShip = new MemberShip();
        do {
            run(memberShip, DateTimes.now());
            String wantMore = inputView.getWantMore();
            if (wantMore.equalsIgnoreCase("N")){
                break;
            }
        } while (true);
    }

    public void run(MemberShip memberShip, LocalDateTime orderDate){
        printStart();
        Orders orders = RetryHandler.retry(() -> getOrders(orderDate));
        ReceiptPrinter receiptPrinter = processPurchase(orders, memberShip);
        outputView.printReceipt(receiptPrinter.print());
        orders.decreaseAmount();
    }

    public Orders getOrders(LocalDateTime orderDate){
        List<OrderRequestDto> orderRequestDtos = OrderParser.parseInput(inputView.getOrders());
        List<Order> orders = orderRequestDtos.stream()
                .map(each -> each.toDomain(filerLoaderProductRepository.findAllByName(each.productName()), orderDate))
                .toList();
        return new Orders(orders);
    }

    public ReceiptPrinter processPurchase(Orders orders, MemberShip memberShip){
        formatOrders(orders);

        if (memberShipApplicable()){
            int discount = memberShip.applyDiscount(orders);
            return new ReceiptPrinter(orders, discount);
        }

        return new ReceiptPrinter(orders, 0);
    }

    private void formatOrders(Orders orders) {
        List<Order> unclaimedFreeItemOrder = orders.getUnclaimedFreeItemOrder();
        unclaimedFreeItemOrder.forEach(this::askToAddFreeItem);
        List<Order> fallBackToNormalOrders = orders.getFallBackToNormalOrders();
        fallBackToNormalOrders.forEach(this::askToPurchaseNormalItems);
    }

    private boolean memberShipApplicable(){
        return Objects.equals("Y", inputView.addMembershipDiscount());
    }

    private void askToPurchaseNormalItems(Order order){
        if (!order.hasFallbackToNormal()){
            return;
        }

        int normalItemCount = order.countFallbackToNormal();
        String unclaimedFreeItemWanted = inputView.askToPurchaseNormalItems(order.getProductName(), normalItemCount);
        if (Objects.equals("N", unclaimedFreeItemWanted)){
            order.decreaseQuantity(normalItemCount);
        }
    }

    private void askToAddFreeItem(Order order){
        String unclaimedFreeItemWanted = inputView.getUnclaimedFreeItemWanted(order.getProductName());
        if (Objects.equals(unclaimedFreeItemWanted, "Y")){
            order.addQuantity();
        }
    }
}
