package com.m3c.wgk;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Application
 *
 */
public class App
{

    static String FILE_NAME;
    static String WORKING_DIR;
    static String PREFIX;
    static int CHUNK_SIZE;
    static int THREAD_SIZE;

    private static Logger log = Logger.getLogger(App.class.getName());
    private AtomicInteger atomicFileCount;
    private ConcurrentHashMap<String, AtomicInteger> words;
    private BlockingQueue<File> blockingQueue;
    private Producer producer;
    private int fileCount;
    private ExecutorService service;

    public StringBuilder initialiseApp() {
        log.info("App initialised");
        loadConfiguration();
        fileCount = makeFiles();
        blockingQueue = new ArrayBlockingQueue(fileCount);
        producer = new Producer(blockingQueue);
        producer.start();
        atomicFileCount = new AtomicInteger();
        atomicFileCount.set(fileCount);
        words = new ConcurrentHashMap<>();
        service = Executors.newFixedThreadPool(THREAD_SIZE);

        for (int i = 0; i < THREAD_SIZE; i++) {
            log.info("Executing thread: " + i);
            service.execute(new Consumer(blockingQueue, atomicFileCount, words));
        }

        service.shutdown();

        try {
            if(!service.awaitTermination(3, TimeUnit.MINUTES)) {
                log.warn("Threads have not terminated in time, system will shut down!");
                System.exit(1);
            }
        } catch (InterruptedException e) {
            log.error("Interrupted Exception", e);
        }

        return topThree(words);
    }

    private static void loadConfiguration() {
        Configurations configurations = new Configurations();

        try {
            Configuration config = configurations.properties(new File("resources/config.properties"));

            String workingDir = config.getString("workingDir", "/path");
            String fileName = config.getString("fileName", "aLargeFile");
            String prefix = config.getString("prefix");
            int chunkSize = config.getInt("chunkSize");
            int threadSize = config.getInt("threadSize");

            FILE_NAME = fileName;
            WORKING_DIR = workingDir;
            PREFIX = prefix;
            CHUNK_SIZE = chunkSize;
            THREAD_SIZE = threadSize;

            log.info("Config initialised");
            log.debug("File name: " + FILE_NAME + "\n" +
                    "Working Directory: " + WORKING_DIR + "\n" +
                    "File Prefix: " + PREFIX + "\n" +
                    "Chunk Size: " + CHUNK_SIZE + "\n" +
                    "Thread Amount: " + THREAD_SIZE + "\n"
            );

        } catch (ConfigurationException e) {
            log.error("Config Exception", e);
        }
    }


    private static int makeFiles() {
        File dir = new File(WORKING_DIR);
        File file = new File(WORKING_DIR + FILE_NAME);
        try {
            splitFile(file, CHUNK_SIZE);
            log.info("Files created");
            return dir.listFiles((dir1, name) -> name.startsWith(PREFIX)).length;
        } catch (IOException e) {
            log.error("IO Exception", e);
        }
        return -1;
    }

    static void deleteFiles() {
        File dir = new File(WORKING_DIR);
        File[] files = dir.listFiles((dir1, name) -> name.startsWith(PREFIX));

        for (File file : files) {
            file.delete();
            log.debug("File: " + file.getName() + " deleted");
        }
        log.info("Files deleted");
    }

    public static StringBuilder topThree(ConcurrentHashMap<String, AtomicInteger> words) {

        String topWord = null;
        String secondWord = null;
        String thirdWord = null;

        Integer second = null;
        Integer first = null;
        Integer third = null;

        for(String word : words.keySet()) {
            Integer value = words.get(word).get();

            if(first == null) {
                first = value;
                topWord = word;
            }

            if(value > first) {

                Integer tmp_second = first;
                String tmp_secondWord = topWord;

                Integer tmp_third = second;
                String tmp_thirdWord = secondWord;

                first = value;
                topWord = word;

                second = tmp_second;
                secondWord = tmp_secondWord;

                third = tmp_third;
                thirdWord = tmp_thirdWord;

            } else if(second == null || value > second) {

                Integer tmp_third = second;
                String tmp_thirdWord = secondWord;

                second = value;
                secondWord = word;

                third = tmp_third;
                thirdWord = tmp_thirdWord;

            } else if(third == null || value > third) {
                third = value;
                thirdWord = word;
            }
        }
        StringBuilder topThree = new StringBuilder();
        topThree.append("Top Word: " + "'" + topWord + "'" + " occuring: " + first + " times.\n" +
                "Top Word: " + "'" + secondWord + "'" + " occuring: " + second + " times.\n" +
                "Top Word: " + "'" + thirdWord + "'" + " occuring: " + third + " times.");

        return topThree;
    }

    private static void splitFile(File file, int fileSize) throws IOException {
        int counter = 1;
        int sizeFile = 1024 * 1024 * fileSize;
        String endOfFile = System.lineSeparator();

        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line = reader.readLine();

            while(line != null) {
                File fileChunk = new File(file.getParent(), PREFIX + String.format("%03d", counter++));
                try(OutputStream make = new BufferedOutputStream(new FileOutputStream(fileChunk))) {
                    int size = 0;
                    while(line != null) {
                        byte[] bytes = (line + endOfFile).getBytes(Charset.defaultCharset());
                        if(size + bytes.length > sizeFile)
                            break;
                        make.write(bytes);
                        size += bytes.length;
                        line = reader.readLine();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            log.error("File Not Found Exception", e);
        }
    }
}
