package quasarstocks;

import co.paralleluniverse.actors.*;
import co.paralleluniverse.actors.behaviors.Supervisor;
import co.paralleluniverse.actors.behaviors.SupervisorActor;
import co.paralleluniverse.fibers.SuspendExecution;
import com.google.common.collect.ImmutableList;
import quasarstocks.utils.FakeStockQuote;
import quasarstocks.utils.StockQuote;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static quasarstocks.StockCmds.*;

/**
 * @author circlespainter
 */
public final class StockActors {
    public static final ActorRef stocksSingletonActor = new StocksActor().spawn();

    public final static class StockActor extends BasicActor<Object, Void> {
        private static final Supervisor.ChildSpec spec(final String sym) {
            return new Supervisor.ChildSpec (
                sym, Supervisor.ChildMode.TRANSIENT, 3, 10, TimeUnit.MILLISECONDS, 10,
                (ActorBuilder<Object, Void>) () -> new StockActor(sym)
            );
        }

        private final String symbol;
        private final StockQuote stockQuote = new FakeStockQuote();
        private final Set<ActorRef> watchers = new HashSet<>();
        private final List<Double> stockHistory = new ArrayList<>();

        public StockActor(String symbol) {
            this.symbol = symbol;
            // A random data set which uses stockQuote.newPrice to get each data point
            double val = new Random().nextDouble() * 800;
            for(int i = 0; i < 50; i++) {
                stockHistory.add(val);
                val = stockQuote.newPrice(val);
            }
        }

        @Override
        protected Void doRun() throws InterruptedException, SuspendExecution {
            for(;;) {
                final Object cmd = receive(75, TimeUnit.MILLISECONDS);
                System.out.println("StockActor(" + symbol + "): " + cmd);
                if (cmd != null) {
                    if (cmd == FetchLatest) {
                        // add a new stock price to the history and drop the oldest
                        Double newPrice = stockQuote.newPrice(stockHistory.get(stockHistory.size() - 1));
                        stockHistory.remove(0);
                        stockHistory.add(newPrice);
                        // notify watchers
                        for(final ActorRef ref : watchers)
                            ref.send(new StockUpdate(symbol, newPrice));
                    } else if (cmd instanceof WatchStock) {
                        final WatchStock ws = (WatchStock) cmd;
                        // send the stock history to the user
                        ws.sender.send(new StockHistory(ws.symbol, ImmutableList.copyOf(stockHistory)));
                        // add the watcher to the list
                        watchers.add(ws.sender);
                    } else if (cmd instanceof UnwatchStock) {
                        final UnwatchStock us = (UnwatchStock) cmd;
                        watchers.remove(us.sender);
                        if (watchers.isEmpty())
                            break; // Exit
                    }
                } else   // Fetch the latest stock value every 75ms
                    self().send(FetchLatest);
            }
            return null;
        }
    }

    public final static class StocksActor extends SupervisorActor {
        private Set<String> syms = new HashSet<>();

        public StocksActor() {
            super(RestartStrategy.ESCALATE);
        }

        @Override
        protected Void doRun() throws InterruptedException, SuspendExecution {
            for(;;) {
                final Object cmd = receive();
                System.out.println("StocksActor: " + cmd);
                if (cmd instanceof WatchStock) {
                    // get or create the StockActor for the symbol and forward this message
                    final WatchStock ws = (WatchStock) cmd;
                    final ActorRef child = getChild(ws.symbol);
                    final ActorRef sa = child != null ? child : addChild(StockActor.spec(ws.symbol));
                    syms.add(ws.symbol);
                    sa.send(cmd);
                } else if (cmd instanceof UnwatchStock) {
                    final UnwatchStock us = (UnwatchStock) cmd;
                    if (us.symbol != null) {
                        // if there is a StockActor for the symbol forward this message
                        final ActorRef sa = getChild(us.symbol);
                        if (sa != null) sa.send(cmd);
                    } else {
                        // if no symbol is specified, forward to everyone
                        for (final String sym : syms)
                            getChild(sym).send(us);
                    }
                }
            }
        }
    }
}
