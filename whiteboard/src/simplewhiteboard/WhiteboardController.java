/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package simplewhiteboard;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Point;
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
    
    public WhiteboardController(){
        pointBuffer = new ArrayList<Point>();
    }
    
    public void sendControllerMessage(String mes) throws Exception {
        InetAddress remoteIP = InetAddress.getByName( "224.0.249.150" );
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
    
    public void startListening(){
        shouldListen = true;
        final WhiteboardController context = this;
        listenThread = new Thread(new Runnable(){
            @Override
            public void run() {
                System.out.println("MULTICAST NODE " + context.name + " STARTED LISTENING...");
                while(shouldListen){
                    try {
                        String msg = context.receiveControllerMessage();
                        System.out.println("RECEIVED MESSAGE: " + msg);
                        
                        if(msg.charAt(0) == 'r'){
                            System.out.println("SETUP TCP CONNECTION AND SEND " + context.pointBuffer.size() + " POINTS ");
                            context.sendControllerMessage("WILL SEND YOU " + context.pointBuffer.size() + " POINTS ");
                            Thread.sleep(3000);
                        }
                    } 
                    catch (Exception ex) {
                        System.out.println("ERROR");
                        Logger.getLogger(WhiteboardNode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        listenThread.start();
    }
    
    public void stopListening(){
        shouldListen = false;
        listenThread.interrupt();
        System.out.println("MULTICAST NODE " + name + " STOPPED LISTENING.");
    }
    
    public static void main(String[] args){
        WhiteboardController ctrlr = new WhiteboardController();
        ctrlr.startListening();
    }
    
}
