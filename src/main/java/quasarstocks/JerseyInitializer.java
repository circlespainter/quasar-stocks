package quasarstocks;

import javax.servlet.*;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.annotation.WebListener;
import co.paralleluniverse.fibers.jersey.ServletContainer;

@WebListener  public class JerseyInitializer implements ServletContextListener {
    @Override public void contextInitialized(final ServletContextEvent sce) {
        final ServletContext sc = sce.getServletContext();
        // Register fiber-blocking Jersey servlet
        Dynamic fiber = sc.addServlet("fiber", ServletContainer.class);
        // Add Jersey configuration class
        fiber.setInitParameter("javax.ws.rs.Application", "quasarstocks.JerseyApplication");
        // Set packages to be scanned for resources
        fiber.setInitParameter("jersey.config.server.provider.packages", "quasarstocks");
        // Don't lazy-load (fail-fast)
        fiber.setLoadOnStartup(1);
        // Support async (needed by fiber-blocking)
        fiber.setAsyncSupported(true);
        // Mapping
        fiber.addMapping("/sentiment/*");
    }

    @Override public void contextDestroyed(ServletContextEvent servletContextEvent) {}
}
