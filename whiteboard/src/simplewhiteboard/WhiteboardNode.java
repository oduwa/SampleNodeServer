/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplewhiteboard;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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
            node.sendMessage("HELLO THUR", 55555);
        } catch (Exception ex) {
            Logger.getLogger(WhiteboardNodeControls.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            sendControllerMessage("r" + name);
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
                        System.out.println("RECEIVED MESSAGE: " + msg);
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

}
