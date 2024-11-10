package store.application;

import java.time.LocalDateTime;
import java.util.List;
import store.domain.MemberShip;
import store.domain.Order;
import store.domain.Orders;
import store.infra.FilerLoaderProductRepository;

public class ConvenienceStoreService {
    private final FilerLoaderProductRepository filerLoaderProductRepository;
    private final OrderParser orderParser;

    public ConvenienceStoreService(FilerLoaderProductRepository filerLoaderProductRepository, OrderParser orderParser) {
        this.filerLoaderProductRepository = filerLoaderProductRepository;
        this.orderParser = orderParser;
    }

    public void addFreeItem(Order order) {
        order.addQuantity();
    }

    public void rejectFallbackItems(Order order, int fallbackItemCount) {
        order.decreaseQuantity(fallbackItemCount);
    }

    private Orders convertToDomainOrders(List<UserOrder> parsedUserOrders, LocalDateTime orderDate) {
        List<Order> domainOrders = parsedUserOrders.stream()
                .map(each -> each.toDomain(filerLoaderProductRepository.findAllByName(each.productName()), orderDate))
                .toList();
        return new Orders(domainOrders);
    }

    public Orders createOrdersFromInput(String userOrderInput, LocalDateTime orderDate) {
        List<UserOrder> parsedUserOrders = orderParser.parseInput(userOrderInput);
        return convertToDomainOrders(parsedUserOrders, orderDate);
    }

    public ReceiptFormatter finalizePurchase(Orders orders, MemberShip memberShip, boolean memberShipApplicable) {
        if (memberShipApplicable) {
            int discount = memberShip.applyDiscount(orders);
            return new ReceiptFormatter(orders, discount);
        }

        return new ReceiptFormatter(orders, 0);
    }

    public String getStocks() {
        return filerLoaderProductRepository.toString();
    }
}
