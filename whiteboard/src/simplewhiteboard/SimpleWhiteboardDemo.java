package simplewhiteboard;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class SimpleWhiteboardDemo implements Runnable
{
  private SimpleWhiteboard simpleWhiteboard;
  private String nodename;


  public SimpleWhiteboardDemo(String nodename)
  {
    this.nodename = nodename;
    //this.simpleWhiteboard = new SimpleWhiteboard(this.nodename, 1000, 600);
    this.simpleWhiteboard = new WhiteboardNode(this.nodename, 1000, 600);
  }

  public void run()
  {
    this.simpleWhiteboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.simpleWhiteboard.setPreferredSize(new Dimension(400, 300));
    this.simpleWhiteboard.pack();
    this.simpleWhiteboard.setVisible(true);
  }

  public static void main(String[] args) throws Exception
  {
    JFrame.setDefaultLookAndFeelDecorated(true);
    String nodename = "defaultnode3";
    if (args.length > 0)
    {
      nodename = args[0];
    }
    SimpleWhiteboardDemo simpleWhiteboardDemo = new SimpleWhiteboardDemo(nodename);
    javax.swing.SwingUtilities.invokeLater(simpleWhiteboardDemo);
  }
}
