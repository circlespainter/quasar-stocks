package quasarstocks;

import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import co.paralleluniverse.embedded.containers.JettyServer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Application {
    public static class Conf {
        public static final ObjectMapper mapper = new ObjectMapper();
        public static String[] defaultStocks = new String[] { "GOOG", "AAPL", "ORCL" };
    }

    public static void main(String[] args) throws Exception {
        JettyServer embeddedServer = new JettyServer();
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader());
        embeddedServer.addServletContextListener(WebActorInitializer.class);
        embeddedServer.addServletContextListener(StaticFilesInitializer.class);
        embeddedServer.enableWebsockets();
        embeddedServer.setResourceBase(Application.class.getClassLoader().getResource("webapp").toExternalForm());
        embeddedServer.start();
    }
}
