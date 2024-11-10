package store;

import store.application.ConvenienceStoreService;
import store.application.OrderParser;
import store.infra.FilerLoaderProductRepository;
import store.presentation.ConvenienceStoreController;
import store.presentation.InputView;
import store.presentation.OutputView;

public class Application {
    public static void main(String[] args) {
        InputView inputView = new InputView();
        OutputView outputView = new OutputView();
        FilerLoaderProductRepository filerLoaderProductRepository = new FilerLoaderProductRepository();
        OrderParser orderParser = new OrderParser();
        ConvenienceStoreService convenienceStoreService = new ConvenienceStoreService(filerLoaderProductRepository,
                orderParser);
        ConvenienceStoreController convenienceStoreController = new ConvenienceStoreController(inputView, outputView,
                convenienceStoreService);
        convenienceStoreController.run();
    }
}
