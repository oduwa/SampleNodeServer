/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplewhiteboard;

import java.awt.Color;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Odie
 */
public class WhiteboardController {

    public String name = "WHITEBOARD_CONTROLLER";
    boolean shouldListen = false;
    Thread listenThread;

    ArrayList<Point> pointBuffer;
    ArrayList<Color> colourBuffer;

    public WhiteboardController() {
        pointBuffer = new ArrayList<Point>();
        colourBuffer = new ArrayList<Color>();
    }

    public void sendControllerMessage(String mes) throws Exception {
        InetAddress remoteIP = InetAddress.getByName("224.0.249.150");
        DatagramSocket socket = new DatagramSocket(55558);
        byte[] sendMes = mes.getBytes();
        DatagramPacket packet = new DatagramPacket(sendMes, sendMes.length, remoteIP, 55550);
        socket.send(packet);
        socket.close();
    }

    public String receiveControllerMessage() throws Exception {
        InetAddress ia = InetAddress.getByName("224.0.249.150");
        MulticastSocket mcs = new MulticastSocket(55550);
        mcs.joinGroup(ia);
        byte[] buffer = new byte[40];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        mcs.receive(packet);
        return new String(buffer);
    }

    public void startListening() {
        shouldListen = true;
        final WhiteboardController context = this;
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("MULTICAST NODE " + context.name + " STARTED LISTENING...");
                while (shouldListen) {
                    try {
                        String msg = context.receiveControllerMessage();
                        //System.out.println("RECEIVED MESSAGE: " + msg);

                        if (msg.charAt(0) == 'r') {
                            Thread.sleep(3000);
                            System.out.println("SETUP TCP CONNECTION AND SEND " + context.pointBuffer.size() + " POINTS ");
                            context.sendControllerMessage("WILL SEND YOU " + context.pointBuffer.size() + " POINTS ");

                            // send all preexisting points to new node over tcp
                            System.out.println("XX:" + msg);
                            String ipAddress = msg.split(":")[2];
                            Socket connectionSocket = new Socket(ipAddress, Utility.TCP_PORT);
                            DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
                            Utility.writeTcpMessage(Utility.generateStringForPointsAndColors(context.pointBuffer, context.colourBuffer), output);

                            Thread.sleep(3000);
                        } else if (msg.charAt(0) == 'p') {
                            // new point drawn by one of the nodes. Add it to list
                            String pointString = msg.split(":")[1];
                            String colourString = msg.split(":")[2];
                            Point newPoint = Utility.convertStringToPoint(pointString);
                            context.pointBuffer.add(newPoint);
                            context.colourBuffer.add(new Color(Integer.parseInt(colourString)));
                            System.out.println("ADDED POINT (" + pointString + ") to point buffer");
                        }
                    } catch (Exception ex) {
                        System.out.println("ERROR");
                        Logger.getLogger(WhiteboardNode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        });
        listenThread.start();
    }

    public void stopListening() {
        shouldListen = false;
        listenThread.interrupt();
        System.out.println("MULTICAST NODE " + name + " STOPPED LISTENING.");
    }

    public static void main(String[] args) {
        WhiteboardController ctrlr = new WhiteboardController();
        ctrlr.startListening();
    }

}
