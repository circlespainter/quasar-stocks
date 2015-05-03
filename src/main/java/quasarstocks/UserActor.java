package quasarstocks;

import co.paralleluniverse.actors.*;
import co.paralleluniverse.comsat.webactors.*;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.SendPort;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import static co.paralleluniverse.comsat.webactors.HttpResponse.*;

import java.io.IOException;

import static quasarstocks.StockCmds.*;

/**
 * @author circlespainter
 */
@WebActor(httpUrlPatterns = {"/ws/*"}, webSocketUrlPatterns = {"/ws"})
public class UserActor extends BasicActor<Object, Void> {
    // The client representation of this actor
    private SendPort<WebDataMessage> peer;

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
        for (;;) {
            final Object message = receive();
            System.out.println("UserActor (web): " + message);
            if (message instanceof HttpRequest) {
                final HttpRequest msg = (HttpRequest) message;
                msg.getFrom().send(ok(self(), msg, "httpResponse").setContentType("text/html").build());
                // -------- WebSocket/SSE opened --------
            } else if (message instanceof WebStreamOpened) {
                final WebStreamOpened msg = (WebStreamOpened) message;
                watch(msg.getFrom()); // will call handleLifecycleMessage with ExitMessage when the session ends

                final SendPort<WebDataMessage> p = msg.getFrom();
                this.peer = p;
                for (final String sym : Application.Conf.defaultStocks) {
                    StockActors.stocksSingletonActor.send(new WatchStock(sym, self()));
                }
            }
            // -------- WebSocket message received --------
            else if (message instanceof WebDataMessage) {
                final WebDataMessage msg = (WebDataMessage) message;
                try {
                    // parse the JSON into WatchStock
                    final WatchStock ws = new WatchStock(Application.Conf.mapper.readTree(msg.getStringBody()).get("symbol").asText(), self());
                    // send the watchStock message to the StocksActor
                    StockActors.stocksSingletonActor.send(ws);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            // ---------- Stock update messages -----------
            else if (message instanceof StockUpdate) {
                // push the stock to the client
                final StockUpdate stockUpdate = (StockUpdate)message;
                final ObjectNode stockUpdateMessage = Application.Conf.mapper.createObjectNode();
                stockUpdateMessage.put("type", "stockupdate");
                stockUpdateMessage.put("symbol", stockUpdate.symbol);
                stockUpdateMessage.put("price", stockUpdate.price.doubleValue());
                peer.send(new WebDataMessage(self(), stockUpdateMessage.toString()));
            }
            else if (message instanceof StockHistory) {
                // push the history to the client
                final StockHistory stockHistory = (StockHistory) message;
                final ObjectNode stockUpdateMessage = Application.Conf.mapper.createObjectNode();
                stockUpdateMessage.put("type", "stockhistory");
                stockUpdateMessage.put("symbol", stockHistory.symbol);

                final ArrayNode historyJson = stockUpdateMessage.putArray("history");
                for (Object price : stockHistory.history) {
                    historyJson.add(((Number)price).doubleValue());
                }

                peer.send(new WebDataMessage(self(), stockUpdateMessage.toString()));
            }
        }
    }
}
