package store.application;

import java.time.LocalDateTime;
import java.util.List;
import store.domain.Order;
import store.domain.OrderProduct;
import store.infra.FilerLoaderProductRepository;
import store.presentation.UserOrder;

public class ConvenienceStoreService {
    private final OrderParser orderParser;
    private final FilerLoaderProductRepository filerLoaderProductRepository;

    public ConvenienceStoreService(OrderParser orderParser, FilerLoaderProductRepository filerLoaderProductRepository) {
        this.orderParser = orderParser;
        this.filerLoaderProductRepository = filerLoaderProductRepository;
    }

    public String getStocks(){
        return filerLoaderProductRepository.toString();
    }

    public Order retrieveOrderFromInput(String userOrderInput, LocalDateTime currentOrderDate) {
        List<UserOrder> parsedUserOrders = orderParser.parseInput(userOrderInput);
        return convertToDomainOrders(parsedUserOrders, currentOrderDate);
    }

    private Order convertToDomainOrders(List<UserOrder> parsedUserOrders, LocalDateTime orderDate) {
        List<OrderProduct> domainOrderProducts = parsedUserOrders.stream()
                .map(each -> each.toDomain(filerLoaderProductRepository.findAllByName(each.productName()), orderDate))
                .toList();
        return new Order(domainOrderProducts);
    }
}
