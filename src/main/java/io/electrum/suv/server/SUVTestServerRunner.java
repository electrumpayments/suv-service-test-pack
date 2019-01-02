package io.electrum.suv.server;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.suv.resource.impl.SUVTestServer;

public class SUVTestServerRunner {

   private static final Logger log_logger = LoggerFactory.getLogger("io.electrum.suv.server.log");

   private static SUVTestServer testServer;

   public static void main(String[] args) throws Exception {
      if (args.length == 0)
         startSUVTestServer("8080");
      else
         startSUVTestServer(args[0]);
   }

   private static void startSUVTestServer(String port) throws Exception {

      log_logger.info("---- STARTING SUV SERVER ----");

      try {

         // === jetty.xml ===
         // Setup Threadpool
         QueuedThreadPool threadPool = new QueuedThreadPool();
         threadPool.setMaxThreads(500);

         // Server
         Server server = new Server(threadPool);

         // Scheduler
         server.addBean(new ScheduledExecutorScheduler());

         // HTTP Configuration
         HttpConfiguration http_config = new HttpConfiguration();
         http_config.setSecureScheme("https");
         http_config.setSecurePort(new Integer(port));
         http_config.setOutputBufferSize(32768);
         http_config.setRequestHeaderSize(8192);
         http_config.setResponseHeaderSize(8192);
         http_config.setSendServerVersion(true);
         http_config.setSendDateHeader(false);

         // Handler Structure
         HandlerCollection handlers = new HandlerCollection();
         ContextHandlerCollection contexts = new ContextHandlerCollection();
         handlers.setHandlers(new Handler[] { contexts, new DefaultHandler() });
         server.setHandler(handlers);

         // Extra options
         server.setDumpAfterStart(false);
         server.setDumpBeforeStop(false);
         server.setStopAtShutdown(true);

         // === jetty-http.xml ===
         ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
         http.setPort(new Integer(port));
         http.setIdleTimeout(30000);
         server.addConnector(http);

         ConstraintSecurityHandler sh = new ConstraintSecurityHandler();
         // sh.addConstraintMapping(cm);

         testServer = new SUVTestServer(); // This is unique
         ServletContainer servletContainer = new ServletContainer(testServer);
         ServletHolder servletHolder = new ServletHolder(servletContainer);
         ServletContextHandler context = new ServletContextHandler();
         context.setContextPath("/");
         context.addServlet(servletHolder, "/*");
         context.setHandler(sh);

         server.setHandler(context);

         // Start the server
         server.start();
         server.join();
      } catch (Exception e) {
         log_logger.error("Unable to start TestServer", e);
         throw e;
      }
   }

   public static SUVTestServer getTestServer() {
      return testServer;
   }
}
