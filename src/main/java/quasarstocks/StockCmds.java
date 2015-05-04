package quasarstocks;

import java.util.List;
import co.paralleluniverse.actors.ActorRef;

public final class StockCmds {
    public final static Object FetchLatest = new Object();

    public final static class StockUpdate {
        public final String symbol;
        public final Number price;
        public StockUpdate(final String symbol, final Number price) {
            this.symbol = symbol;
            this.price = price;
        }
    }

    public final static class StockHistory {
        public final String symbol;
        public final List<Double> history;
        public StockHistory(final String symbol, final List<Double> history) {
            this.symbol = symbol;
            this.history = history;
        }
    }

    public final static class WatchStock {
        public final String symbol;
        public final ActorRef sender;
        public WatchStock(final String symbol, final ActorRef sender) {
            this.symbol = symbol;
            this.sender = sender;
        }
    }

    public final static class UnwatchStock {
        public final String symbol;
        public final ActorRef sender;
        public UnwatchStock(final String symbol, final ActorRef sender) {
            this.symbol = symbol;
            this.sender = sender;
        }
    }
}
