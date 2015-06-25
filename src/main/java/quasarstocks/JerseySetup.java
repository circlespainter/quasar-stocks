package quasarstocks;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseySetup extends ResourceConfig {
    public JerseySetup() {
        register(JacksonFeature.class);   // Support jackson
    }
}