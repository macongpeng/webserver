package com.adobe.test;

import java.io.*;
import java.net.*;
import java.security.cert.CRL;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public final class HttpRequest implements Runnable {

	final static Logger logger = LogManager.getLogger(HttpRequest.class);
	final static String CRLF = "\r\n";// For convenience
    final static String TEMP_KEY = "temp";

    public static final int HTTP_BAD_METHOD = 405;
	Socket socket;

	// Constructor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}

	// Implement the run() method of the Runnable interface.
	public void run() {
	    if (logger.isDebugEnabled())
        {
            logger.entry("run@HttpRequest");
        }
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
		finally {
            if (logger.isDebugEnabled())
            {
                logger.traceExit("run@HttpRequest");
            }
        }
    }

	private void processRequest() throws Exception {
        if (logger.isDebugEnabled())
        {
            logger.traceEntry("processRequest@HttpRequest");
        }
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());

		// Set up input stream filters.
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // In http 1.1, by default keep alive is on
        boolean keepAlive = true;

        do {


            String requestLine = br.readLine();

            if (logger.isDebugEnabled()) {
                logger.debug("The coming request is :" + requestLine);
            }
            if (requestLine == null || requestLine.equals(""))
            {
                // If the coming request is null, stop serving this request and move on to the next one
                break;
            }

            // The following obtains the IP address of the incoming connection.
            InetAddress incomingAddress = socket.getInetAddress();
            String ipString = incomingAddress.getHostAddress();

            if (logger.isDebugEnabled()) {
                logger.debug("The incoming address is:   " + ipString);
            }

            // TODO: current model doesn't support space in the parameters
            // String Tokenizer is used to extract file name from this class.
            StringTokenizer tokens = new StringTokenizer(requestLine);
            // Get the request type
            String requestType = tokens.nextToken();
            if (logger.isDebugEnabled()) {
                logger.debug("Request type is :" + requestType);
            }

            if (!requestType.equalsIgnoreCase("GET") && !(requestType.equalsIgnoreCase("HEAD"))) {
                os.writeBytes("HTTP/1.0 " + HTTP_BAD_METHOD + " unsupported method type: ");
                os.writeBytes(requestLine.substring(0, 5));
                os.writeBytes(CRLF);
                os.flush();
                socket.close();
            }

            String fileName = tokens.nextToken();
            String query = "";
            // Check if the fileName contains ?
            int questionMarkIndex = fileName.indexOf('?');
            if (questionMarkIndex != -1)
            {
                // Just take the file name for now and drop the query parameters
                query = fileName.substring(questionMarkIndex + 1);
                fileName = fileName.substring(0, questionMarkIndex);

                if (logger.isDebugEnabled()) {
                    logger.debug("Request fileName is :" + fileName + " query is :" + query);
                }

            }
            // Process the header to check if keep alive is disabled
            String headerLine = null;
            while ((headerLine = br.readLine()).length() != 0) { // While the header
                if (logger.isDebugEnabled()) {
                    logger.debug("the headerLine is :" + headerLine);
                }
                if (headerLine.equalsIgnoreCase("Connection: close")) {
                    keepAlive = false;
                }
            }

            // Open the requested file.
            File fileRequested = null;
            FileInputStream fis = null;
            boolean fileExists = false;
            String fileWithPath = WebServer.m_root + File.separator + fileName;
            try {
                fileRequested = new File(fileWithPath);
                fis = new FileInputStream(fileRequested);
                fileExists = true;

                if (query != null && !query.equals(""))
                {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Process the query of " + query);
                    }
                    // Store the query to the file before send it back
                    processQuery(fileWithPath, query);
                }
            } catch (FileNotFoundException e) {
                fileExists = false;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("The file :" + fileWithPath + " exits:" + fileExists);
            }
            // Construct the response message
            StringBuffer header = new StringBuffer();
            String statusLine = null; // Set initial values to null
            String contentTypeLine = null;
            String entityBody = null;

            String contentLength = null;
            if (fileExists) {
                statusLine = "HTTP/1.1 200 OK" + CRLF;
                contentTypeLine = "Content-Type: " + contentType(fileName) + CRLF;

            } else {
                statusLine = "HTTP/1.1 404 Not Found" + CRLF;
                contentTypeLine = "Content-Type: text/html" + CRLF;
                entityBody = "<HTML> <HEAD><TITLE>Not Found</TITLE></HEAD> <BODY>Not Found on the java WebServer</BODY></HTML>";
            }
            // End of response message construction

            // Send the status line.
            header.append(statusLine);
            // Send the content type line.
            header.append(contentTypeLine);
            //
            if (keepAlive) {
                header.append("Connection: keep-alive" + CRLF);
            } else {
                header.append("Connection: close" + CRLF);
            }

            // Send the entity body.
            if (fileExists) {
                header.append("Content-Length: " + fileRequested.length() + CRLF);
                // Send a blank line to indicate the end of the header lines.
                header.append(CRLF);
                os.writeBytes(header.toString());
                sendBytes(fis, os);
                os.flush();
                fis.close();
            } else {
                header.append("Content-Length: " + entityBody.length() + CRLF);
                // Send a blank line to indicate the end of the header lines.
                header.append(CRLF);
                os.writeBytes(header.toString());
                os.writeBytes(entityBody);
                os.flush();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("header sent is");
                logger.debug(header.toString());
            }
            if (!keepAlive) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Keep-alive is disabled. Close the socket");
                }
                os.close(); // Close streams and socket.
                br.close();
                socket.close();

            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("Keep-alive is enabled. Keep the socket for 10 seconds to be able to reuse");
                }
                //socket.setSoTimeout(10000);
            }
        }
        while (keepAlive);
        //socket.close();

        if (logger.isDebugEnabled())
        {
            logger.traceExit("processRequest@HttpRequest");
        }
	}

	// Process the query and store the value to the file
    // For now, only process tempature
    private void processQuery(String file, String query){
        // Only process when the fileName is with json extension
        if (FilenameUtils.getExtension(file).equalsIgnoreCase("json")) {
            Map<String, String> queryPairs = splitQuery(query);
            // Get the temp value from the query
            String key = queryPairs.get("key");
            if (logger.isDebugEnabled()) {
                logger.debug("The key in the query is  " + key);
            }
            // TODO should validate temp
            if (key != null && TEMP_KEY.equals(key)) {
                String temp = queryPairs.get("value");
                if (logger.isDebugEnabled()) {
                    logger.debug("The value in the query is  " + temp);
                }
                // Load JsObjects from the file
                JSONParser parser = new JSONParser();
                try {

                    Object obj = parser.parse(new FileReader(file));
                    JSONObject jsonObject = (JSONObject) obj;
                    jsonObject.put(TEMP_KEY, temp);

                    if (logger.isDebugEnabled()) {
                        logger.debug("The new json is:" + jsonObject.toJSONString());
                    }
                    //jsonObject.writeJSONString(new PrintWriter(file));
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(jsonObject.toJSONString());

                    fileWriter.flush();
                    fileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Process the query and split it to a map with key and value pairs
	private Map<String, String> splitQuery(String query) {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
            catch(UnsupportedEncodingException e)
            {
                // If unsupported encoding Exception happens, just ignore the key value pair
                continue;
            }
        }
        return query_pairs;
    }
    // Need this one for sendBytes function called in processRequest
	private static void sendBytes(FileInputStream fis, OutputStream os)
			throws Exception {
        if (logger.isDebugEnabled())
        {
            logger.traceEntry("sendBytes@HttpRequest");
        }
		// Construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[1024];
		int bytes = -1;

		// Copy requested file into the socket’s output stream.
		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
        if (logger.isDebugEnabled())
        {
            logger.traceExit("sendBytes@HttpRequest");
        }
	}

	// TODO: to support more types
	private static String contentType(String fileName) {
        if (logger.isDebugEnabled())
        {
            logger.traceEntry("contentType@HttpRequest with fileName:" + fileName);
        }
        String contentType = "application/octet-stream";
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            contentType = "text/html";
        } else if (fileName.endsWith(".jpg")) {
            contentType = "text/jpg";
        } else if (fileName.endsWith(".gif")) {
            contentType = "text/gif";
        } else if (fileName.endsWith(".svg")) {
            contentType = "image/svg+xml";
        }

        if (logger.isDebugEnabled())
        {
            logger.traceEntry("contentType@HttpRequest return :" + contentType);
        }
		return contentType;
	}
}