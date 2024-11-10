package store;

import store.infra.FilerLoaderProductRepository;
import store.presentation.ConvenienceStoreController;
import store.presentation.InputView;
import store.presentation.OutputView;

public class Application {
    public static void main(String[] args) {
        InputView inputView = new InputView();
        OutputView outputView = new OutputView();
        FilerLoaderProductRepository filerLoaderProductRepository = new FilerLoaderProductRepository();
        ConvenienceStoreController convenienceStoreController = new ConvenienceStoreController(inputView, outputView,
                filerLoaderProductRepository);
        convenienceStoreController.run2();
    }
}
