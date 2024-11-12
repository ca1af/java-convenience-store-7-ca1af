package store;

import store.application.OrderService;
import store.infra.FilerLoaderProductRepository;
import store.presentation.ConvenienceStoreController;
import store.application.OrderParser;
import store.presentation.view.InputView;
import store.presentation.view.OutputView;

public class Application {
    public static void main(String[] args) {
        InputView inputView = new InputView();
        OutputView outputView = new OutputView();
        FilerLoaderProductRepository filerLoaderProductRepository = new FilerLoaderProductRepository();
        OrderParser orderParser = new OrderParser();
        OrderService orderService = new OrderService(filerLoaderProductRepository, orderParser);
        ConvenienceStoreController convenienceStoreController = new ConvenienceStoreController(inputView, outputView,
                orderService);
        convenienceStoreController.run();
    }
}
