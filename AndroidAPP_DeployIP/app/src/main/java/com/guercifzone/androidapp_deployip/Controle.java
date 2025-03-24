package com.guercifzone.androidapp_deployip;

package com.example.mousecontrol;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import java.io.PrintWriter;
import java.net.Socket;

public class Controle extends AppCompatActivity {

    private MouseControl mouseControl;
    private EditText xInput, yInput;
    private Button moveButton, clickButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        xInput = findViewById(R.id.xInput);
        yInput = findViewById(R.id.yInput);
        moveButton = findViewById(R.id.moveButton);
        clickButton = findViewById(R.id.clickButton);

        // Create a MouseControl instance with the server IP address and port
        mouseControl = new MouseControl("192.168.1.100", 12345); // Replace with your PC's IP address

        // Move button logic
        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int x = Integer.parseInt(xInput.getText().toString());
                    int y = Integer.parseInt(yInput.getText().toString());
                    mouseControl.moveMouse(x, y);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });

        // Click button logic
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mouseControl.clickMouse();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mouseControl.close();
    }

    private class MouseControl {



            private Socket socket;
            private PrintWriter out;

            public MouseControl(String ip, int port) {
                try {
                    socket = new Socket(ip, port);
                    out = new PrintWriter(socket.getOutputStream(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void moveMouse(int x, int y) {
                String command = "MOVE " + x + " " + y;
                out.println(command);
            }

            public void clickMouse() {
                out.println("CLICK");
            }

            public void close() {
                try {
                    out.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

