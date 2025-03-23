package org.guercifzone;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class SimpleHTTPServer {

    public static void main(String[] args) throws Exception {
        // Set the folder to be shared
        String folderPath = "/home/jknz0/Pictures"; // Replace with the folder you want to share

        // Create an HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Create a handler that will serve files from the folder
        server.createContext("/", new FileHandler(folderPath));

        // Start the server
        server.start();
        System.out.println("HTTP Server started on http://localhost:8080");
    }

    // FileHandler class to handle HTTP requests and serve files from the specified folder
    static class FileHandler implements HttpHandler {
        private final String folderPath;

        public FileHandler(String folderPath) {
            this.folderPath = folderPath;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Get the requested file path from the URL
            String requestedFile = exchange.getRequestURI().getPath();

            // If the requested file path is "/", serve the list of files in the directory
            if (requestedFile.equals("/")) {
                serveFileList(exchange);
            } else {
                serveRequestedFile(exchange, requestedFile);
            }
        }

        // Serve the list of files in the folder
        private void serveFileList(HttpExchange exchange) throws IOException {
            StringBuilder fileListHtml = new StringBuilder("<html><body>");
            fileListHtml.append("<h1>Shared Folder</h1><ul>");

            // List files in the folder
            File folder = new File(folderPath);
            for (File file : folder.listFiles()) {
                fileListHtml.append("<li><a href='/")
                        .append(file.getName())
                        .append("'>")
                        .append(file.getName())
                        .append("</a></li>");
            }
            fileListHtml.append("</ul></body></html>");

            // Send the response
            byte[] response = fileListHtml.toString().getBytes();
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

        // Serve the requested file
        private void serveRequestedFile(HttpExchange exchange, String requestedFile) throws IOException {
            // Get the file from the folder
            File file = new File(folderPath + requestedFile);
            if (file.exists() && file.isFile()) {
                // Read the file and send it in the response
                byte[] fileContent = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, fileContent.length);
                OutputStream os = exchange.getResponseBody();
                os.write(fileContent);
                os.close();
            } else {
                // If file doesn't exist, send 404 error
                String response = "File not found!";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}
