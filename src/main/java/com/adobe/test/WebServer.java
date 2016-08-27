package com.adobe.test;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class WebServer {
	final static Logger logger = LogManager.getLogger(WebServer.class);
    // The key of the port of the web server in config properties
	public final static String WEBSERVER_PORT_KEY = "port";
	// The port of the web server
	private static int m_port = 80;
	// The flag to indicate if the web start is started
	public static boolean m_started = true;
	// The key of the count of the threads in the pool for the web server
	public final static String WEBSERVER_THREADS_KEY = "threads";
	// The count of the threads in the pool
	public static int m_threads = 40;

    // The key of the port of the web server in config properties
    public final static String WEBSERVER_ROOT_KEY = "root";
    // The port of the web server
    public static String m_root = ".";

	// The singleton instance of the web server
	private static WebServer m_instance = new WebServer();
	
	// The executor service to handle threads
	private ExecutorService m_executor;
	// The socket which serves the clients
	private ServerSocket m_websocket;
	/**
	 * Constructor without parameters for singleton
	 */
	private WebServer() {
		// initialize the web server
		init();
	}
	/**
	 * Initialize the web server
	 */
	public void init() {
		if (logger.isDebugEnabled())
		{
			logger.traceEntry("init@WebServer");
		}
		// load the config properties
		Properties prop = loadProperties();
		// get the port of the web server
		// TODO: need to handle the case if the value of the port is not int
		m_port = (Integer.parseInt((String)(prop.getOrDefault(WEBSERVER_PORT_KEY, "80"))));
		if (logger.isDebugEnabled())
		{
			logger.debug("The port of the web server is " + m_port);
		}
		// TODO: need to handle the case if the value of the threads is not int
		m_threads = (Integer.parseInt((String)(prop.getOrDefault(WEBSERVER_THREADS_KEY, "40"))));
        if (logger.isDebugEnabled())
        {
            logger.debug("The count of the threads in the pool is " + m_threads);
        }

        // TODO: need to handle the case if the value of the threads is not int
        m_root = (String)(prop.getOrDefault(WEBSERVER_ROOT_KEY, m_root));
        if (logger.isDebugEnabled())
        {
            logger.debug("The root of the web server: " + m_root);
        }
		// Initialize the thread pool
		m_executor = Executors.newFixedThreadPool(m_threads);
		if (logger.isDebugEnabled())
		{
			logger.traceExit("init@WebServer");
		}
	}
	/**
	 * Start the web server
	 */
	public void start() throws Exception {
        if (logger.isDebugEnabled())
        {
            logger.traceEntry("start@WebServer");
        }
		m_websocket = new ServerSocket(m_port);
		m_started = true;
		
		while (m_started) {
			// Listen for a TCP connection request.
			Socket connectionSocket = m_websocket.accept();
			// Construct object to process HTTP request message
			HttpRequest request = new HttpRequest(connectionSocket);

			Thread thread = new Thread(request);
			m_executor.execute(thread);
		}
		
		m_executor.shutdown();
		m_executor.awaitTermination(5, TimeUnit.SECONDS);
        if (logger.isDebugEnabled())
        {
            logger.traceExit("start@WebServer");
        }
	}
	/**
	 * TODO: need to stop the web server nicely
	 */
	public void stop() {
		m_started = true;
	}
	/**
	 * Load the config properties of the web server
	 * @return the properties of the config
	 */
	private Properties loadProperties() {
        if (logger.isDebugEnabled())
        {
            logger.traceEntry("loadProperties@WebServer");
        }
		InputStream inputStream = null;
		Properties prop = new Properties();
		
		try {
			String propFileName = "config.properties";
 
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);//new FileInputStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Properties are loaded successfully");
                }
			} else {
                if (logger.isDebugEnabled())
                {
                    logger.debug("property file '" + propFileName + "' not found in the classpath");
                }
				//throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			inputStream.close();
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}

        if (logger.isDebugEnabled())
        {
            logger.traceExit("Properties are loaded successfully");
        }
		return prop;
	}
	/**
	 * Get the singleton instance of the web server
	 * @return the insance of the web server
	 */
	public static WebServer getInstance() {
		return m_instance;
	}
	
	public static void main(String argv[]) throws Exception {
		WebServer webServer = WebServer.getInstance();
		webServer.start();
	}
}
