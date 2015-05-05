package quasarstocks;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyApplication extends ResourceConfig {
    public JerseyApplication() {
        register(JacksonFeature.class);   // Support jackson
    }
}