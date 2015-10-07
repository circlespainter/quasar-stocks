package quasarstocks;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.httpclient.FiberHttpClientBuilder;
import co.paralleluniverse.strands.CheckedSuspendableCallable;
import co.paralleluniverse.strands.SuspendableUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

@Singleton
@Path("/")
public class Sentiment {
    final CloseableHttpClient client = FiberHttpClientBuilder.
            create(Runtime.getRuntime().availableProcessors()). // threads
            setMaxConnPerRoute(1000).
            setMaxConnTotal(1000000).build();

    @GET
    @Path("{sym}")
    @Produces(MediaType.APPLICATION_JSON)
    @Suspendable
    public JsonNode get(final @PathParam("sym") String sym) throws IOException, ExecutionException, InterruptedException {
        final List<Fiber<JsonNode>> agents = new ArrayList<>();
        final List<JsonNode> sentiments = new ArrayList<>();
        final JsonNode tweets = getTweets(sym);
        for (final JsonNode t : tweets.get("statuses"))
            agents.add(sentimentRetriever(t.get("text").asText()));
        for(final Fiber<JsonNode> f : agents)
            sentiments.add(f.get());
        return sentimentJson(sentiments);
    }

    private JsonNode sentimentJson(final List<JsonNode> sentiments) {
        final Double neg = getAverageSentiment(sentiments, "neg");
        final Double neutral = getAverageSentiment(sentiments, "neutral");
        final Double pos = getAverageSentiment(sentiments, "pos");

        final ObjectNode ret = Application.Conf.mapper.createObjectNode();
        final ObjectNode prob = Application.Conf.mapper.createObjectNode();
        ret.put("probability", prob);
        prob.put("neg", neg);
        prob.put("neutral", neutral);
        prob.put("pos", pos);
        String c;
        if (neutral > 0.5)
            c = "neutral";
        else if (neg > pos)
            c = "neg";
        else
            c = "pos";
        ret.put("label", c);
        return ret;
    }

    private Double getAverageSentiment(final List<JsonNode> sentiments, final String label) {
        Double sum = 0.0;
        final int size = sentiments.size();
        for (final JsonNode s : sentiments) sum += s.get("probability").get(label).asDouble();
        return sum / (size > 0 ? size : 1);
    }

    private Fiber<JsonNode> sentimentRetriever(final String text) throws IOException {
        return new Fiber<> (SuspendableUtils.asSuspendableCallable (new CheckedSuspendableCallable<JsonNode, Exception>() {
            @Override
            public JsonNode call() throws SuspendExecution, InterruptedException, Exception {
                final HttpPost req = new HttpPost(Application.Conf.sentimentUrl);
                final List<NameValuePair> urlParameters = new ArrayList<>();
                urlParameters.add(new BasicNameValuePair("text", text));
                req.setEntity(new UrlEncodedFormEntity(urlParameters));
                return Application.Conf.mapper.readTree(EntityUtils.toString(client.execute(req).getEntity()));
            }
        })).start();
    }

    @Suspendable
    private JsonNode getTweets(final String sym) throws IOException {
        return Application.Conf.mapper.readTree (
            EntityUtils.toString(client.execute(new HttpGet(Application.Conf.tweetUrl.replace(":sym:", sym))).getEntity())
        );
    }
}
