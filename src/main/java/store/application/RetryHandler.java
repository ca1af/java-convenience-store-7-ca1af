package store.application;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class RetryHandler {
    private RetryHandler() {
        throw new UnsupportedOperationException();
    }

    public static <T> T retry(Supplier<T> supplier) {
        while (true) {
            try {
                return supplier.get(); // 로직 실행 및 결과 반환
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
                throw e;
            }
        }
    }
}
