package com.m3c.wgk;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class Producer extends Thread {

    private static Logger log = Logger.getLogger(App.class.getName());
    private BlockingQueue<File> blockingQueue = null;

    public Producer(BlockingQueue<File> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    public void run() {
        File dir = new File(App.WORKING_DIR);
        File[] files = dir.listFiles((dir1, name) -> name.startsWith(App.PREFIX));

        for (File file : files) {
            try {
                blockingQueue.put(file);
                log.debug(file.getName() + " put on Blocking Queue");
            } catch (InterruptedException e) {
                log.error("Interrupted Exception", e);
            }

        }
    }

}
