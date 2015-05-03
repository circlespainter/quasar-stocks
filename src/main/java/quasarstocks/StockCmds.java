package quasarstocks;

import co.paralleluniverse.actors.ActorRef;

import java.util.List;

/**
 * @author circlespainter
 */
public final class StockCmds {
    public interface StockCommand {}

    public final static StockCommand FetchLatest = new StockCommand(){};

    public final static class StockUpdate implements StockCommand {
        public final String symbol;
        public final Number price;
        public StockUpdate(final String symbol, final Number price) {
            this.symbol = symbol;
            this.price = price;
        }
    }

    public final static class StockHistory implements StockCommand {
        public final String symbol;
        public final List<Double> history;
        public StockHistory(final String symbol, final List<Double> history) {
            this.symbol = symbol;
            this.history = history;
        }
    }

    public final static class WatchStock implements StockCommand {
        public final String symbol;
        public final ActorRef sender;
        public WatchStock(final String symbol, final ActorRef sender) {
            this.symbol = symbol;
            this.sender = sender;
        }
    }

    public final static class UnwatchStock implements StockCommand {
        public final String symbol;
        public final ActorRef sender;
        public UnwatchStock(final String symbol, final ActorRef sender) {
            this.symbol = symbol;
            this.sender = sender;
        }
    }
}
