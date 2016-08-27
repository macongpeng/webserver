package com.adobe.test;

import org.junit.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by dennyma on 27/08/2016.
 * Unit test for WebServer class
 */
public class WebServerTest {
    @Before
    public void runBeforeTestMethod() {
        System.out.println("@Before - runBeforeTestMethod");
    }

    // Should rename to @AfterTestMethod
    @After
    public void runAfterTestMethod() {
        System.out.println("@After - runAfterTestMethod");
    }

    @Test
    public void shouldLoadProperties() throws Exception{
        System.out.println("@Test - Should load properties from config.properties");

        Object webserver = WebServer.getInstance();
        // Get the private method of loadProperties
        Method loadProperties = webserver.getClass().getDeclaredMethod("loadProperties", new Class<?>[0]);
        // Set it to public
        loadProperties.setAccessible(true);

        Properties expected = new Properties();
        expected.put("port","80");
        expected.put("threads","40");
        expected.put("root",".");

        Properties properties = (Properties)loadProperties.invoke(webserver);
        assertNotNull(properties);
        assertEquals(expected, properties);
    }

    @Test
    public void shouldInit() throws Exception{
        System.out.println("@Test - Init the web server");

        Object webserver = WebServer.getInstance();
        // Get the private method of loadProperties
        Method init = webserver.getClass().getDeclaredMethod("init", new Class<?>[0]);

        init.invoke(webserver);
        // Get the thread pool field
        Field threadPool = webserver.getClass().getDeclaredField("m_executor");
        threadPool.setAccessible(true);

        ExecutorService pool = (ExecutorService)threadPool.get(webserver);
        assertNotNull(pool);
        //assertEquals(pool., properties);
    }
}
