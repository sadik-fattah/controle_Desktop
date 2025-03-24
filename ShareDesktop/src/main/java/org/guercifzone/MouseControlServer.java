package org.guercifzone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MouseControlServer {
    private static Robot robot;

    public static void main(String[] args) {
        try {
            robot = new Robot();
            ServerSocket serverSocket = new ServerSocket(12345); // Listen on port 12345
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String command;
                while ((command = in.readLine()) != null) {
                    System.out.println("Command received: " + command);
                    handleCommand(command);
                }

                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleCommand(String command) {
        try {
            if (command.startsWith("MOVE")) {
                String[] parts = command.split(" ");
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                robot.mouseMove(x, y);
            } else if (command.equals("CLICK")) {
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
