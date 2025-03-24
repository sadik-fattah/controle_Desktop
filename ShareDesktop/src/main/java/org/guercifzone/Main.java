package org.guercifzone;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public class Main extends JFrame {
private JButton btnDesktop,btnWebCam,btnFolder;
    public Main(){
        setTitle("Main Start the server");
        setSize(300,200);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setSize(250,150);
        btnDesktop = new JButton("Desktop share");
        btnDesktop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    new ScreenStreamer().setVisible(true);
            }
        });
        btnDesktop.setBounds(80,30,150,30);

        btnWebCam = new JButton("WebCam Shared");
        btnWebCam.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
          new WebcamStreamServer().setVisible(true);
            }
        });
        btnWebCam.setBounds(80,60,150,30);

        btnFolder = new JButton("Folder Shared");
        btnFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
new SimpleHTTPServer().setVisible(true);
            }
        });
        btnFolder.setBounds(80,90,150,30);

      setLayout(new FlowLayout());
        add(btnDesktop);
        add(btnWebCam);
        add(btnFolder);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
}
