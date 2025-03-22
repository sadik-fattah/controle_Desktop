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
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ScreenStreamer {
    private static Webcam webcam;
    private static boolean streaming = false; // Flag to control streamin
    public static void main(String[] args) throws IOException {
        // Create the frame for the buttons
        JFrame frame = new JFrame("the server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        Label label = new Label("click start to run server ");
        // Create Start Button
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!streaming) {
                    startStreaming();
                    streaming = true;
                }
                label.setText("Server started on port 8080");
            }
        });
        // Create an HTTP server

        // Create Stop Button
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (streaming) {
                    streaming = false;
                    System.out.println("Streaming stopped.");
                }
                label.setText("Streaming stopped.");
            }

        });
JButton stratWebcam = new JButton("webcam");
stratWebcam.addActionListener(e -> {
    webcam = Webcam.getDefault();
    if (webcam == null) {
        System.out.println("No webcam detected");
        return;
    }
    webcam.open();
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


});

JButton closeWebcm = new JButton("webcam close");
closeWebcm.addActionListener(e -> {
    webcam.close();
});
        // Add buttons to the frame
        frame.add(startButton);
        frame.add(stopButton);
        frame.add(stratWebcam);
        frame.add(closeWebcm);
        frame.add(label);

        // Set frame size and visibility
        frame.setSize(300, 100);
        frame.setVisible(true);
    }

    private static void startStreaming() {
        // Start the server and streaming in a new thread so the GUI remains responsive
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
}
