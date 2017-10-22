package com.m3c.wgk;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Hello world!
 *
 */
public class Starter
{

    private static Logger log = Logger.getLogger(Starter.class.getName());
    private static final String LOG_PROPERTIES_FILE = "resources/log4j.properties";

    static StringBuilder output = null;

    public static void main( String[] args ) {
        long start = System.nanoTime();
        initialiseLogging();
        App app = new App();
        output = app.initialiseApp();
        System.out.println(output);

        long end = System.nanoTime();
        double seconds = (double)(end - start) / 1000000000.0;
        log.info("Word Time: " + seconds);

        app.deleteFiles();

        long end2 = System.nanoTime();
        double seconds2 = (double)(end2 - start) / 1000000000.0;
        log.info("Overall Time: " + seconds2);
    }

    private static void initialiseLogging() {
        PropertyConfigurator.configure(LOG_PROPERTIES_FILE);
        log.info("Logging initialised");
    }
}
