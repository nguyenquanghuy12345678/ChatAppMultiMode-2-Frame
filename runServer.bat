@echo off
rem Added sqlite-jdbc to classpath so DatabaseManager can load org.sqlite.JDBC
java -cp "bin;lib\webcam-capture-0.3.12.jar;lib\slf4j-api-1.7.2.jar;lib\bridj-0.6.2.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar;lib\sqlite-jdbc-3.50.3.0.jar" server.ServerUI
