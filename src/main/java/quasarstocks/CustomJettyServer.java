package quasarstocks;

import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import java.lang.management.ManagementFactory;

public class CustomJettyServer extends AbstractEmbeddedServer {
	private static final Logger log = LoggerFactory.getLogger(CustomJettyServer.class);

	private Server server;
	private ServerConnector http;
	private ServletContextHandler context;
	private boolean error;
	private boolean wsEnabled;

	private void build() {
		if (server != null)
			return;
		this.server = new Server(new QueuedThreadPool(nThreads, nThreads));

		// Setup JMX
		MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		server.addEventListener(mbContainer);
		server.addBean(mbContainer);
		// Add loggers MBean to server (will be picked up by MBeanContainer above)
		server.addBean(Log.getLog());

		this.http = new ServerConnector(server);
		http.setPort(port);
		http.setAcceptQueueSize(maxConn);
		server.addConnector(http);
		this.context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	}

	@Override
	public ServletDesc addServlet(String name, Class<? extends Servlet> servletClass, String mapping) {
		if (context == null)
			build();
		ServletHolder sh = new ServletHolder(servletClass);
		context.addServlet(sh, mapping);
		return new JettyServletDesc(sh);
	}

	@Override
	public void addServletContextListener(Class<? extends ServletContextListener> scl) {
		if (context == null)
			build();
		try {
			context.addEventListener(scl.newInstance());
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void start() throws Exception {
		if (context==null)
			build();
		server.setHandler(context);
		if (wsEnabled)
			WebSocketServerContainerInitializer.configureContext(context);
		server.start();

		log.info("Jetty server started");
	}

	@Override
	public void stop() throws Exception {
		server.stop();
	}

	@Override
	public void enableWebsockets() throws Exception {
		this.wsEnabled = true;
	}

	@Override
	public void setResourceBase(final String resourceBaseUrl) {
		if (context==null)
			build();
		context.setResourceBase(resourceBaseUrl);
	}

	private static class JettyServletDesc implements ServletDesc {
		private final ServletHolder impl;

		public JettyServletDesc(ServletHolder sh) {
			this.impl = sh;
		}

		@Override
		public ServletDesc setInitParameter(String name, String value) {
			impl.setInitParameter(name, value);
			return this;
		}

		@Override
		public ServletDesc setLoadOnStartup(int load) {
			impl.setInitOrder(load);
			return this;
		}
	}
}
