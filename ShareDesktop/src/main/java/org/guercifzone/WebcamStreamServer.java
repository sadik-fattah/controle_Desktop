package org.guercifzone;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

import javax.imageio.ImageIO;

public class WebcamStreamServer {

    private static Webcam webcam;

    public static void main(String[] args) throws Exception {
        // Initialize the webcam
        webcam = Webcam.getDefault();
        if (webcam == null) {
            System.out.println("No webcam detected");
            return;
        }
        webcam.open();

        // Create an HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/stream", new WebcamStreamHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("Server started at http://192.168.166.150:8080/stream");
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
