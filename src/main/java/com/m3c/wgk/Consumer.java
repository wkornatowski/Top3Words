package com.m3c.wgk;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer extends Thread {

    private static Logger log = Logger.getLogger(App.class.getName());
    private BlockingQueue<File> blockingQueue = null;
    private AtomicInteger atomicFileCount;
    private ConcurrentHashMap<String, AtomicInteger> words;

    public Consumer(BlockingQueue<File> blockingQueue, AtomicInteger atomicFileCount, ConcurrentHashMap<String, AtomicInteger> words) {

        this.blockingQueue = blockingQueue;
        this.atomicFileCount = atomicFileCount;
        this.words = words;
    }

    public void run() {
        while (atomicFileCount.get() > 0) {
            try {
                File file = blockingQueue.take();
                if (file != null) {
                    atomicFileCount.decrementAndGet();
                    log.debug("Atomic FILE COUNT: " + atomicFileCount.get());
                    LineIterator iterator = FileUtils.lineIterator(file, String.valueOf(StandardCharsets.UTF_8));
                    try {
                        while (iterator.hasNext()) {
                            String[] split = iterator.nextLine().toLowerCase().replaceAll("[^a-z ]", "").split("\\s+"); // <-- 50-53 seconds
                            //String[] split = iterator.nextLine().toLowerCase().split("\\W+");                     //   <-- 35 seconds, same result tbh
                            Arrays.stream(split).allMatch((String s) -> {
                                if(words.keySet().contains(s)) {
                                    words.get(s).incrementAndGet();
                                }
                                else {
                                    AtomicInteger newInteger = new AtomicInteger();
                                    newInteger.set(1);
                                    words.put(s, newInteger);
                                }
                            return true;
                            });
                        }
                    } finally {
                        LineIterator.closeQuietly(iterator);
                        //iterator.close();
                    }
                }
            } catch (IOException e) {
                log.error("IO Exception", e);
            } catch (InterruptedException e) {
                log.error("Interrupted Exception", e);
            }
        }
    }
}
//                    try {
//                        while (iterator.hasNext()) {
//                            String[] split = iterator.nextLine().replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
//                            for (int i = 0; i < split.length; i++) {
//                                if (words.keySet().contains(split[i])) {
//                                    words.get(split[i]).incrementAndGet();
//                                } else {
//                                    AtomicInteger newInteger = new AtomicInteger();
//                                    newInteger.set(1);
//                                    words.put(split[i], newInteger);
//                                }
//
//                            }
//
//                        }
//                    } finally {
//                        LineIterator.closeQuietly(iterator);
//                    }
//                }

//                            for (int i = 0; i < split.length; i++) {
//                                String s = split[i];
//                                if (words.keySet().contains(s)) {
//                                    words.get(s).incrementAndGet();
//                                } else {
//                                    AtomicInteger newInteger = new AtomicInteger();
//                                    newInteger.set(1);
//                                    words.put(s, newInteger);
//                                }
//                            }


//

//                    try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(file.getAbsolutePath()))))) {
//                        reader.lines().forEach(line -> {
//                            //String[] split = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
//                            String[] split = line.toLowerCase().split("\\W+");
//                            //List<String> split = new ArrayList<>();
//                            //split.add(String.valueOf(line.toLowerCase().split("\\W+")));
//                            //Collections.addAll(split, line.toLowerCase().split("\\W+"));
//                            for (int i = 0; i < split.length; i++) {
//                                String s = split[i];
//                                if (words.keySet().contains(s)) {
//                                    words.get(s).incrementAndGet();
//                                } else {
//                                    AtomicInteger newInteger = new AtomicInteger();
//                                    newInteger.set(1);
//                                    words.put(s, newInteger);
//                                }
//                            }
//                        });
//                    } catch (IOException e) {
//                        log.debug("IO Exception", e);
//                    }