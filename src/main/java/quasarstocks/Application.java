package quasarstocks;

import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author circlespainter
 */
public class Application {
    public static class Conf {
        public static final ObjectMapper mapper = new ObjectMapper();
        public static String[] defaultStocks = new String[] { "GOOG", "AAPL", "ORCL" };
        public static String sentimentUrl = "http://text-processing.com/api/sentiment/";
        public static String tweetUrl = "http://twitter-search-proxy.herokuapp.com/search/tweets?q=%%24%s";
    }

    public static void main(String[] args) throws Exception {
        JettyServer embeddedServer = new JettyServer();
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader());
        embeddedServer.addServletContextListener(WebActorInitializer.class);
        embeddedServer.addServletContextListener(StaticFilesInitializer.class);
        embeddedServer.enableWebsockets();
        embeddedServer.getContext().setResourceBase(Application.class.getClassLoader().getResource("webapp").toExternalForm());
        embeddedServer.start();
        JettyServer.waitUrlAvailable("http://localhost:8080");
        System.out.println("Server is up");
    }
}
