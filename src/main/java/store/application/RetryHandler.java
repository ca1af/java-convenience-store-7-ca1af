package store.application;

import java.util.function.Supplier;

public class RetryHandler {
    private RetryHandler() {
        throw new UnsupportedOperationException();
    }

    public static <T> T retry(Supplier<T> supplier) {
        while (true) {
            try {
                return supplier.get();
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
