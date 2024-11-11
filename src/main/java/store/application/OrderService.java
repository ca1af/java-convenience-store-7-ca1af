package store.application;

import java.time.LocalDateTime;
import java.util.List;
import store.domain.order.Order;
import store.domain.order.OrderProduct;
import store.domain.product.Product;
import store.infra.FilerLoaderProductRepository;

public class OrderService {
    private final FilerLoaderProductRepository filerLoaderProductRepository;
    private final OrderParser orderParser;

    public OrderService(FilerLoaderProductRepository filerLoaderProductRepository, OrderParser orderParser) {
        this.filerLoaderProductRepository = filerLoaderProductRepository;
        this.orderParser = orderParser;
    }

    public Order retrieveOrdersFromInput(String userOrderInput, LocalDateTime currentOrderDate) {
        List<UserOrder> parsedUserOrders = orderParser.parseInput(userOrderInput);
        return convertToDomainOrders(parsedUserOrders, currentOrderDate);
    }

    public Order convertToDomainOrders(List<UserOrder> parsedUserOrders, LocalDateTime orderDate) {
        List<OrderProduct> domainOrderProducts = parsedUserOrders.stream()
                .map(each -> createOrderProduct(orderDate, each))
                .toList();
        return new Order(domainOrderProducts);
    }

    public OrderProduct createOrderProduct(LocalDateTime orderDate, UserOrder each) {
        List<Product> products = filerLoaderProductRepository.findAllByName(each.productName());
        return each.toDomain(products, orderDate);
    }

    public String getStocks(){
        return filerLoaderProductRepository.toString();
    }
}
