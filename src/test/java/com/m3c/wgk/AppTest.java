package com.m3c.wgk;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private App app;
    @Before
    public void init(){
       app = new App();
       app.initialiseApp();

    }

    @org.junit.Test
    public void testTopThreeWords() {
        CharSequence one = "the";
        CharSequence two = "and";
        CharSequence three = "of";
        System.out.println(app.initialiseApp());
        Assert.assertTrue(app.initialiseApp().toString().contains(one));
        Assert.assertTrue(app.initialiseApp().toString().contains(two));
        Assert.assertTrue(app.initialiseApp().toString().contains(three));
    }

    @org.junit.Test
    public void fileNotFound() {

        String filePath = App.WORKING_DIR + App.FILE_NAME;
        File file = new File(filePath);
        Assert.assertTrue(file.isFile() && file.getName().equals("aLargeFile"));
    }

    @After
    public void tearDown()
    {

    }
}
