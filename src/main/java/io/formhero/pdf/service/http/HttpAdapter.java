package io.formhero.pdf.service.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Created by ryan.kimber on 2017-12-08.
 */
public class HttpAdapter implements Runnable {

    private static final Logger log = LogManager.getLogger(HttpAdapter.class.getName());
    public void run()
    {
        try {
            Server server = setup();
            // The use of server.join() the will make the current thread join and wait until the server is done executing.
            // See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
            server.start();
            server.dumpStdErr();
            server.join();
            log.info("Server running on port " + ((ServerConnector)server.getConnectors()[0]).getLocalPort());
        }
        catch(Exception e) {
            log.error("Error running Jetty server:", e);
            System.exit(1);
        }
    }

    private Server setup() throws Exception
    {
        Server server = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setOutputBufferSize(32768);
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendXPoweredBy(false);

        // HTTP connector
        // The first server connector we create is the one for http, passing in
        // the http configuration we configured above so it can get things like
        // the output buffer size, etc. We also set the port (8080) and
        // configure an idle timeout.
        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        http.setPort(9999);
        http.setHost("0.0.0.0");
        http.setIdleTimeout(30000);
        server.addConnector(http);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(FillPdfServlet.class, "/fill");
        handler.addServletWithMapping(GeneratePageImageServlet.class, "/generatePageImage");
        handler.addServletWithMapping(MergePdfServlet.class, "/merge");
        handler.addServletWithMapping(ExaminePdfServlet.class, "/examine");
        return server;
    }
}
