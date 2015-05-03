package quasarstocks.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Creates a randomly generated price based on the previous price
 */
public class FakeStockQuote implements StockQuote {

    public Double newPrice(Double lastPrice) {
        // todo: this trends towards zero
        return lastPrice * (0.95  + (0.1 * ThreadLocalRandom.current().nextDouble())); // lastPrice * (0.95 to 1.05)
    }

}
