package org.guercifzone;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class SimpleHTTPServer extends JFrame {
    private String folderPath = "/home/jknz0/Pictures";
    private HttpServer server; // Declare server as a class member

    public SimpleHTTPServer() {
        setTitle("Folder Shared");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Label label = new Label("Click start to run server ");

        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Start the server
                    if (server == null) { // Check if server is already started
                        server = HttpServer.create(new InetSocketAddress(8080), 0);
                        server.createContext("/", new FileHandler(folderPath));
                        server.start();
                        System.out.println("HTTP Server started on http://localhost:8080");
                        label.setText("HTTP Server started on http://localhost:8080");

                    } else {
                        System.out.println("Server is already running.");
                    }
                } catch (Exception r) {
                    r.printStackTrace();
                }
            }
        });

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (server != null) {
                        server.stop(0);  // Stop the server gracefully
                        System.out.println("HTTP Server stopped.");
                        label.setText("HTTP Server stopped");
                    } else {
                        System.out.println("Server is not running.");
                    }
                } catch (Exception r) {
                    r.printStackTrace();
                }
            }
        });

        add(startButton);
        add(stopButton);
        add(label);

        setLayout(new FlowLayout());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimpleHTTPServer().setVisible(true);
            }
        });
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
