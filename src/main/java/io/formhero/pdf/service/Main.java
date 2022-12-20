package io.formhero.pdf.service;

import io.formhero.pdf.service.http.HttpAdapter;
import io.formhero.util.FhCache;
import io.formhero.util.FhConfig;
import io.formhero.util.FhConfigException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class.getName());
    public static void main(String[] args) {

        Main instance = new Main();
        //PDFBox doesn't like the color-space manager provided in Java 8 for performance reasons. They recommend overriding it with KcmsServiceProvider.
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        instance.setUpAndRun();
    }

    private void setUpAndRun()
    {
        try {
            // Load configuration into singleton
            FhConfig fhConfig = FhConfig.loadConfig();

            Thread httpThread = new Thread(new HttpAdapter());
            httpThread.start();

            // We're using the FhCache and we want it to shutdown properly if we're shutdown, so register a hook to do that.
            final Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        FhCache.shutdown();
                        mainThread.join();
                    }
                    catch(FhConfigException | InterruptedException ie)
                    {
                        log.error("Server interupted:", ie);
                        System.exit(1);
                    }
                }
            });
        }
        catch(Throwable t)
        {
            // If something goes wrong, we should just be able to exit and docker will restart us, and we should be happy.
            log.error("Something went wrong! We\'re shutting down and expect to be restarted...", t);
            System.exit(1);
        }
    }
}
