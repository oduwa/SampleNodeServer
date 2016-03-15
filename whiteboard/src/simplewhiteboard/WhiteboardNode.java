/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplewhiteboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

/**
 *
 * @author Odie
 */

class WhiteboardNodeControls extends SimpleWhiteboardControls {

    WhiteboardNode node;
    
//    public WhiteboardNodeControls(SimpleWhiteboardPanel simpleWhiteboardPanel) {
//        super(simpleWhiteboardPanel);
//    }
    
    public WhiteboardNodeControls(SimpleWhiteboardPanel simpleWhiteboardPanel, WhiteboardNode node) {
        super(simpleWhiteboardPanel);
        this.node = node;
    }
    
    @Override
    public void drawLine(Point newPoint)
  {
      super.drawLine(newPoint);
      try {
            // Send new point to controller
          // p:x,y:<colour>>
          node.sendControllerMessage("p:" + newPoint.x + "," + newPoint.y + ":" + color.getRGB() + ":");

            // Send new point to all connected nodes
          // p:<sender_none_name>:(x,y):<colour>>
          node.sendMessage("p:" + node.name + ":" + newPoint.x + "," + newPoint.y + ":" + color.getRGB() + ":", 55555);
      } catch (Exception ex) {
          Logger.getLogger(WhiteboardNodeControls.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
    
    public void drawLineInView(Point newPoint){
        super.drawLine(newPoint);
    }
    
    @Override
    public void drawString(String s){
        super.drawString(s);
        
        if (this.point != null) {
            try {
            // Send new string to controller
                // s:<TEXT>:<TEXT_POINT>
                node.sendControllerMessage("s:" + s + ":" + this.point.x +"," + this.point.y + ":");

            // Send new string to all connected nodes
                // t:<sender_none_name>:<TEXT>:<TEXT_POINT>
                node.sendMessage("s:" + node.name + ":" + s + ":" + this.point.x + "," + this.point.y + ":", 55555);
            } catch (Exception ex) {
                Logger.getLogger(WhiteboardNodeControls.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void drawStringInView(String s){
        super.drawString(s);
    }
}


class SimpleWhiteboardMenuActionListener implements ActionListener {

    private WhiteboardNode node;
    
    public SimpleWhiteboardMenuActionListener(WhiteboardNode node){
        this.node = node;
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        System.err.println(String.format("menu action by odie: %s", actionEvent.getActionCommand()));
        
        if(actionEvent.getActionCommand().equalsIgnoreCase("Connect")){
            node.startListening();
        }
        else if(actionEvent.getActionCommand().equalsIgnoreCase("Disconnect")){
            node.stopListening();
        }
    }
}

public class WhiteboardNode extends SimpleWhiteboard {

    private JScrollPane scrollPane;
    private SimpleWhiteboardPanel simpleWhiteboardPanel;
    private WhiteboardNodeControls whiteboardControls;
    private JMenuBar menuBar;
    private SimpleWhiteboardMenuActionListener menuActionListener;
    
    public String name;
    boolean shouldListen = false;
    Thread listenThread;
    
    private ServerSocket tcpServerSocket;

    public WhiteboardNode(String nodename, int width, int height) {
        super(nodename, width, height);

        this.simpleWhiteboardPanel = new SimpleWhiteboardPanel(width, height);
        this.scrollPane = new JScrollPane(this.simpleWhiteboardPanel);
        this.getContentPane().add(this.scrollPane);
        this.whiteboardControls = new WhiteboardNodeControls(this.simpleWhiteboardPanel, this);
        this.getContentPane().add(this.whiteboardControls, BorderLayout.SOUTH);
        this.menuBar = new JMenuBar();
        this.menuActionListener = new SimpleWhiteboardMenuActionListener(this);
        JMenu networkMenu = new JMenu("Network");
        JMenuItem connectItem = new JMenuItem("Connect");
        connectItem.addActionListener(this.menuActionListener);
        JMenuItem disconnectItem = new JMenuItem("Disconnect");
        disconnectItem.addActionListener(this.menuActionListener);
        networkMenu.add(connectItem);
        networkMenu.add(disconnectItem);
        this.menuBar.add(networkMenu);
        this.setJMenuBar(this.menuBar);
        
        this.name = nodename;
    }
    
    public void sendMessage(String mes, int destPort) throws Exception {
        InetAddress remoteIP = InetAddress.getByName( "224.0.249.100" );
        DatagramSocket socket = new DatagramSocket(55556);
        byte[] sendMes = mes.getBytes();
        DatagramPacket packet = new DatagramPacket(sendMes, sendMes.length, remoteIP, destPort);
        socket.send(packet);
        socket.close();
    }

    public String receiveMessage() throws Exception {
        InetAddress ia = InetAddress.getByName("224.0.249.100");
        MulticastSocket mcs = new MulticastSocket(55555);
        mcs.joinGroup(ia);
        byte[] buffer = new byte[40];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        mcs.receive(packet);
        return new String(buffer);
    }
    
    public void sendControllerMessage(String mes) throws Exception {
        InetAddress remoteIP = InetAddress.getByName( "224.0.249.150" );
        DatagramSocket socket = new DatagramSocket(55557);
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
        
        // TODO: Clear points
            
        // Send message to controller to request all existing points
        try {
            // start tcp server to await receipt of existing points
            tcpServerSocket = new ServerSocket(Utility.TCP_PORT);
            TcpServerThread serverThread = new TcpServerThread(tcpServerSocket, this);
            serverThread.start();
            
            // send request
            sendControllerMessage("r" + ":" + name + ":" + InetAddress.getLocalHost().getHostAddress() + ":");
            String ctrlrMsg = receiveControllerMessage();
            System.out.println("CONTROLLER SAID: " + ctrlrMsg);
            
        } catch (Exception ex) {
            Logger.getLogger(WhiteboardNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        final WhiteboardNode context = this;
        listenThread = new Thread(new Runnable(){
            @Override
            public void run() {
                System.out.println("MULTICAST NODE " + context.name + " STARTED LISTENING...");
                while(shouldListen){
                    try {
                        String msg = context.receiveMessage();
                        
                        if(msg.charAt(0) == 'p'){
                            /*
                             * Received message of the form 
                             * p:<sender_none_name>:(x,y):<colour>
                             */
                            String[] msgComponents = msg.split(":");
                            String senderName = msgComponents[1];
                            System.out.println("RECEIVED MESSAGE: " + msgComponents[2]);
                            if (!senderName.equalsIgnoreCase(context.name)) {
                                context.whiteboardControls.color = new Color(Integer.parseInt(msgComponents[3]));
                                context.whiteboardControls.drawLineInView(Utility.convertStringToPoint(msgComponents[2]));
                            }
                        }
                        else if(msg.charAt(0) == 's'){
                            /*
                             * Received message of the form 
                             * t:<sender_none_name>:<TEXT>:<TEXT_POINT>
                             */
                            String[] msgComponents = msg.split(":");
                            String senderName = msgComponents[1];
                            System.out.println("RECEIVED MESSAGE: " + msgComponents[2]);
                            if (!senderName.equalsIgnoreCase(context.name)) {
                                context.whiteboardControls.point = Utility.convertStringToPoint(msgComponents[3]);
                                context.whiteboardControls.drawStringInView(msgComponents[2]);
                            }
                        }
                    } 
                    catch (Exception ex) {
                        System.out.println("ERROR LISTENING FOR MESSAGE");
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
    
    private class TcpServerThread extends Thread {

        private ServerSocket serverSocket;
        private DataInputStream input;
        private DataOutputStream output;
        private PrintWriter printOutput;
        
        WhiteboardNode node;

        public TcpServerThread(ServerSocket serverSocket, WhiteboardNode node) {
            this.serverSocket = serverSocket;
            this.node = node;
        }

        @Override
        public void run() {
            //while (true) {
                try {
                    Socket socket = serverSocket.accept();

                    input = new DataInputStream(socket.getInputStream());
                    output = new DataOutputStream(socket.getOutputStream());
                    
                    String data = Utility.readTcpMessage(input);
                    if (data != null && !data.equalsIgnoreCase("%%")) {
                        System.out.println(data);
                        Utility.ViewBundle viewBundle = Utility.convertStringToViewBundle(data);
                        
                        ArrayList<Point> pointList = viewBundle.pointBundle.points;
                        ArrayList<Color> colorList = viewBundle.pointBundle.colors;
                        for (int i = 0; i < pointList.size(); i++) {
                            Point point = pointList.get(i);
                            Color color = colorList.get(i);
                            node.whiteboardControls.color = color;
                            node.whiteboardControls.drawLineInView(point);
                        }
                        
                        ArrayList<String> textList = viewBundle.textBundle.texts;
                        ArrayList<Point> textPointList = viewBundle.textBundle.textPoints;
                        for (int i = 0; i < textPointList.size(); i++) {
                            Point point = textPointList.get(i);
                            String text = textList.get(i);
                            node.whiteboardControls.point = point;
                            node.whiteboardControls.drawStringInView(text);
                        }
                    }

                    
                    socket.close();
                    serverSocket.close();

                } catch (IOException ex) {
                    Logger.getLogger(Utility.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            //}
        }

        

    }

}
