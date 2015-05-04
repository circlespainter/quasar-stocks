package quasarstocks;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;
import org.eclipse.jetty.servlet.DefaultServlet;

@WebListener public class StaticFilesInitializer implements ServletContextListener {
    @Override public void contextInitialized(final ServletContextEvent sce) {
        final ServletContext sc = sce.getServletContext();
        ServletRegistration.Dynamic d = sc.addServlet("default", DefaultServlet.class);
        d.addMapping("/");
    }

    @Override public void contextDestroyed(ServletContextEvent servletContextEvent) {}
}
