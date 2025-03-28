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

public class ScreenStreamer extends JFrame {

    private static boolean streaming = false;
    public ScreenStreamer(){
       setTitle("Screen Streamer");
       setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 100);
        Label label = new Label("click start to run server ");
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!streaming) {
                    startStreaming();
                    streaming = true;
                }
                label.setText("Server started at http://192.168.166.150:8080/");
            }
        });
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
        add(startButton);
        add(stopButton);
        add(label);

        setLayout(new FlowLayout());

    }
    public static void main(String[] args) throws IOException {

 SwingUtilities.invokeLater(new Runnable() {
     @Override
     public void run() {
         new ScreenStreamer().setVisible(true);
     }
 });

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

}
