package quasarstocks;

import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import co.paralleluniverse.embedded.containers.JettyServer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Application {
    public static class Conf {
        public static final ObjectMapper mapper = new ObjectMapper();
        public static String[] defaultStocks = new String[] { "GOOG", "AAPL", "ORCL" };
        public static String sentimentUrl = "http://text-processing.com/api/sentiment/";
        public static String tweetUrl = "http://twitter-search-proxy.herokuapp.com/search/tweets?q=:sym:";
    }

    public static void main(String[] args) throws Exception {
        JettyServer embeddedServer = new JettyServer();
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader());
        embeddedServer.addServletContextListener(JerseyInitializer.class);
        embeddedServer.addServletContextListener(WebActorInitializer.class);
        embeddedServer.addServletContextListener(StaticFilesInitializer.class);
        embeddedServer.enableWebsockets();
        embeddedServer.setResourceBase(Application.class.getClassLoader().getResource("webapp").toExternalForm());
        embeddedServer.start();
    }
}
