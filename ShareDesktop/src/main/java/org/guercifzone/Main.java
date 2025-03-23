package org.guercifzone;

import com.github.sarxos.webcam.Webcam;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class Main {
    private static TextArea textArea;
    private static Webcam webcam;
    private static boolean streaming = false; // Flag to control streamin
    public static void main(String[] args) throws IOException {
        // Create the frame for the buttons
        JFrame frame = new JFrame("the server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
         textArea = new TextArea("Select from the button list");
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(200, 100));

        JButton startButton = new JButton("Start Streaming");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!streaming) {
                    startStreaming();
                    streaming = true;
                }
                textArea.setText("Server started on port 8080");
            }
        });
        JButton stopButton = new JButton("StopS Streaming");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (streaming) {
                    streaming = false;
                    System.out.println("Streaming stopped.");
                }
                textArea.setText("Streaming stopped.");
            }

        });
        JButton stratWebcam = new JButton("webcam start");
        stratWebcam.addActionListener(e -> {

            OpenTheWebcam();


        });
        JButton closeWebcm = new JButton("webcam close");
        closeWebcm.addActionListener(e -> {
            webcam.close();
            textArea.setText("WebCam Start.");
        });
        JButton sharFolder = new JButton("Share folder");
        sharFolder.addActionListener(e -> {
            textArea.setText("Folder is shared:\nhttp://localhost:8080");
            String folderPath = "/home/jknz0/Pictures"; // Replace with the folder you want to share

            // Create an HTTP server on port 8080
            HttpServer server = null;
            try {
                server = HttpServer.create(new InetSocketAddress(8080), 0);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            // Create a handler that will serve files from the folder
            server.createContext("/", new SimpleHTTPServer.FileHandler(folderPath));

            // Start the server
            server.start();
            System.out.println("HTTP Server started on http://localhost:8080");

        });
        // Add buttons to the frame
        frame.add(startButton);
        frame.add(stopButton);
        frame.add(stratWebcam);
        frame.add(closeWebcm);
        frame.add(sharFolder);
        frame.add(textArea);

        // Set frame size and visibility
        frame.setSize(200, 400);
        frame.setVisible(true);
    }

    private static void OpenTheWebcam() {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            System.out.println("No webcam detected");
            return;
        }
        webcam.open();
        textArea.setText("WebCam Start.");
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        server.createContext("/stream", new WebcamStreamServer.WebcamStreamHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("Server started at http://192.168.166.150:8080/stream");

    }

    private static void startStreaming() {

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(8080);
                System.out.println("Server started on port 8080");
                while (streaming) {
                    try (Socket clientSocket = serverSocket.accept()) {
                        OutputStream os = clientSocket.getOutputStream();
                        os.write("HTTP/1.0 200 OK\r\n".getBytes());
                        os.write("Content-Type: multipart/x-mixed-replace; boundary=--myboundary\r\n\r\n".getBytes());

                        while (streaming) {
                            BufferedImage image = captureScreen();
                            if (image != null) {
                                os.write("--myboundary\r\n".getBytes());
                                os.write("Content-Type: image/jpeg\r\n".getBytes());
                                os.write("Content-Length: ".getBytes());
                                os.write(String.valueOf(image.getWidth() * image.getHeight()).getBytes());
                                os.write("\r\n\r\n".getBytes());
                                ImageIO.write(image, "jpeg", os);
                                os.write("\r\n\r\n".getBytes());
                                os.flush();
                            }
                            Thread.sleep(100); // Adjust for frame rate
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /// ##########################
    private static BufferedImage captureScreen() {
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            return robot.createScreenCapture(screenRect);
        } catch (AWTException e) {
            e.printStackTrace();
            return null;
        }
    }
    static class WebcamStreamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "multipart/x-mixed-replace; boundary=--myboundary");
            exchange.sendResponseHeaders(200, 0);

            // Stream webcam frames in MJPEG format
            while (true) {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    // Convert image to JPEG byte array
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(image, "JPEG", byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    // Send MJPEG response with boundary markers
                    String boundary = "--myboundary\r\n";
                    String header = "Content-Type: image/jpeg\r\nContent-Length: " + imageBytes.length + "\r\n\r\n";
                    exchange.getResponseBody().write(boundary.getBytes());
                    exchange.getResponseBody().write(header.getBytes());
                    exchange.getResponseBody().write(imageBytes);
                    exchange.getResponseBody().write("\r\n".getBytes());
                    exchange.getResponseBody().flush();
                }
                try {
                    Thread.sleep(100); // wait for next frame (10 FPS)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
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
