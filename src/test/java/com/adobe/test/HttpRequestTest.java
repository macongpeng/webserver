package com.adobe.test;

import org.junit.*;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by dennyma on 28/08/2016.
 * Unit test for HttpRequest class
 */
public class HttpRequestTest {
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
    public void shouldReturnCorrectContentType() throws Exception {
        System.out.println("@Test - Should return the correct content type");

        Socket socket = mock(Socket.class);
        Object httpRequest = new HttpRequest(socket);

        // Get the private method of loadProperties
        Method contentType = httpRequest.getClass().getDeclaredMethod("contentType", String.class);
        // Set it to public
        contentType.setAccessible(true);

        String testType1 = (String)contentType.invoke(httpRequest, "test.htm");
        assertEquals(testType1, "text/html");

        String testType2 = (String)contentType.invoke(httpRequest, "test.html");
        assertEquals(testType2, "text/html");

        String testType3 = (String)contentType.invoke(httpRequest, "test.jpg");
        assertEquals(testType3, "text/jpg");

        String testType4 = (String)contentType.invoke(httpRequest, "test.gif");
        assertEquals(testType4, "text/gif");

        String testType5 = (String)contentType.invoke(httpRequest, "test.svg");
        assertEquals(testType5, "image/svg+xml");
    }

    @Test
    public void shouldNotSendDataIfFileIsEmpty() throws Exception {
        System.out.println("@Test - Should Send data");

        Socket socket = mock(Socket.class);
        Object httpRequest = new HttpRequest(socket);

        // Get the private method of loadProperties
        Method sendBytes = httpRequest.getClass().getDeclaredMethod("sendBytes", FileInputStream.class, OutputStream.class);
        // Set it to public
        sendBytes.setAccessible(true);

        FileInputStream fis = mock(FileInputStream.class);

        int dataSize = 1024;
        when(fis.read(any(byte[].class))).thenReturn(-1);
        OutputStream ois = mock(OutputStream.class);

        sendBytes.invoke(httpRequest, fis, ois);
        verify(fis, times(1)).read(any(byte[].class));
        verify(ois, never()).write(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    public void shouldSendDataIfFileIsNotEmpty() throws Exception {
        System.out.println("@Test - Should Send data");

        Socket socket = mock(Socket.class);
        Object httpRequest = new HttpRequest(socket);

        // Get the private method of loadProperties
        Method sendBytes = httpRequest.getClass().getDeclaredMethod("sendBytes", FileInputStream.class, OutputStream.class);
        // Set it to public
        sendBytes.setAccessible(true);

        FileInputStream fis = mock(FileInputStream.class);

        int dataSize = 1024;
        when(fis.read(any(byte[].class))).thenReturn(1024).thenReturn(-1);
        OutputStream ois = mock(OutputStream.class);
        doNothing().when(ois).write(any(byte[].class), anyInt(), anyInt());

        sendBytes.invoke(httpRequest, fis, ois);
        verify(fis, times(2)).read(any(byte[].class));
        verify(ois, times(1)).write(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    public void shouldSplitQueryToMap() throws Exception {
        System.out.println("@Test - Should split query to a map");

        Socket socket = mock(Socket.class);
        Object httpRequest = new HttpRequest(socket);

        // Get the private method of loadProperties
        Method splitQuery = httpRequest.getClass().getDeclaredMethod("splitQuery", String.class);
        // Set it to public
        splitQuery.setAccessible(true);

        String query = "room=central-hall&temp=90";

        Map<String, String> keyValuePair = (Map<String, String>) splitQuery.invoke(httpRequest, query);
        assertNotNull(keyValuePair);
        assertEquals(keyValuePair.get("room"), "central-hall");
        assertEquals(keyValuePair.get("temp"), "90");
    }
}
