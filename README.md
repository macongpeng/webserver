# Java based Webserver
A multi-threaded file-based web server with thread pooling implemented in java.

It is based the sample from [Shubhs Blog](https://www.shubhsblog.com/programming/multithreaded-webserver-java.html)
 
### Features added by Denny Ma:
* Thread pooling by leveraging Java Executor [Oracle official website](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html)
* Add proper HTTP/1.1keep-alive behavior based on the http-client's capabilities exposed through its request headers.
* Load web server config properties from config.properties
* Log4j to manage the logging
* Maven based project which manage the dependencies more easily
* Junit for unit tests
* The code coverage for the project by jacoco
* Support SVG file type
* Support http request for static file with parameters (won't process parameters for now)
* Cleaner logging code using Java 8 lambdas

### Instructions on how to run the web server
* Create a folder for the web server
* Copy javawebserver-1.0-SNAPSHOT.jar to the folder
* Optional: create your own [log4j2.xml](https://github.com/macongpeng/webserver/blob/master/src/main/resources/log4j2.xml) and [config.properties](https://github.com/macongpeng/webserver/blob/master/src/main/resources/config.properties)

Assume you have your own log4j2.xml and the OS is Mac OS or linux. 

Run `sudo java -classpath .:./javawebserver-1.0-SNAPSHOT.jar -Dlog4j.configurationFile=log4j2.xml com.adobe.test.WebServer`

otherwise

Run `sudo java -jar javawebserver-1.0-SNAPSHOT.jar`

### Todo list:

* Support most popular file types
* Coverage is over the quality gate 85%

![Code Coverage](https://github.com/macongpeng/webserver/blob/master/images/codecoverage.png)
