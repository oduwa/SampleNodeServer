package simplewhiteboard;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

enum DrawMode
{
  LINE, TEXT;
}

class SimpleWhiteboardControls extends JPanel implements ActionListener, MouseListener, KeyListener
{
  private SimpleWhiteboardPanel simpleWhiteboardPanel;
  private JComboBox drawModeComboBox;
  private JButton colorButton;
  private static String[] drawModeName = {"line", "text"};
  private DrawMode drawMode;
  private Color color;
  private Point point;
  private String fontname;
  private int fontsize;

  public SimpleWhiteboardControls(SimpleWhiteboardPanel simpleWhiteboardPanel)
  {
    super();
    this.drawMode = DrawMode.LINE;
    this.simpleWhiteboardPanel = simpleWhiteboardPanel;
    this.drawModeComboBox = new JComboBox(drawModeName);
    this.drawModeComboBox.addActionListener(this);
    this.add(this.drawModeComboBox);
    this.colorButton = new JButton("set colour");
    this.colorButton.addActionListener(this);
    this.add(this.colorButton);
    this.syncState();
    this.simpleWhiteboardPanel.addMouseListener(this);
    this.simpleWhiteboardPanel.addKeyListener(this);
    this.color = Color.BLACK;
    this.fontname = "Monospaced";
    this.fontsize = 20;
  }

  public void drawLine(Point newPoint)
  {
    if (this.point == null)
    {
      this.point = newPoint;
    }
    else
    {
      this.point = this.simpleWhiteboardPanel.drawLine(this.point, newPoint, this.color);
    }
  }

  public void drawString(String s)
  {
    if (this.point != null)
    {
      this.point = this.simpleWhiteboardPanel.drawString(s, this.point, this.fontname, this.fontsize, this.color);
    }
  }

  public void syncState()
  {
    switch (this.drawMode)
    {
    case LINE:
      this.drawModeComboBox.setSelectedIndex(0);
      break;
    case TEXT:
      this.drawModeComboBox.setSelectedIndex(1);
      break;
    default:
      throw new RuntimeException("unknown draw mode");
    }
  }

  private void drawModeActionPerformed(ActionEvent actionEvent)
  {
    String cmd = (String) this.drawModeComboBox.getSelectedItem();
    if (cmd.equals("line"))
    {
      this.drawMode = DrawMode.LINE;
    }
    else if (cmd.equals("text"))
    {
      this.drawMode = DrawMode.TEXT;
    }
    else
    {
      throw new RuntimeException(String.format("unknown command: %s", cmd));
    }
  }

  private void colorActionPerformed(ActionEvent actionEvent)
  {
    color = JColorChooser.showDialog(this.simpleWhiteboardPanel, "choose colour", this.color);
    if (color != null)
    {
      this.color = color;
    }
  }

  public void actionPerformed(ActionEvent actionEvent)
  {
    if (actionEvent.getSource() == this.drawModeComboBox)
    {
      this.drawModeActionPerformed(actionEvent);
    }
    else if (actionEvent.getSource() == this.colorButton)
    {
      this.colorActionPerformed(actionEvent);
    }
  }

  @Override
  public void keyPressed(KeyEvent keyEvent)
  {
  }

  @Override
  public void keyReleased(KeyEvent keyEvent)
  {
  }

  @Override
  public void keyTyped(KeyEvent keyEvent)
  {
    switch (this.drawMode)
    {
    case TEXT:
      String s = Character.toString(keyEvent.getKeyChar());
      this.drawString(s);
      break;
    default:
      // ignore event if not in text mode
      break;
    }
  }

  @Override
  public void mouseEntered(MouseEvent mouseEvent)
  {
  }

  @Override
  public void mouseExited(MouseEvent mouseEvent)
  {
  }

  @Override
  public void mousePressed(MouseEvent mouseEvent)
  {
  }

  @Override
  public void mouseReleased(MouseEvent mouseEvent)
  {
  }

  @Override
  public void mouseClicked(MouseEvent mouseEvent)
  {
    // make sure panel gets focus when clicked
    this.simpleWhiteboardPanel.requestFocusInWindow();
    Point newPoint = mouseEvent.getPoint();
    switch (this.drawMode)
    {
    case TEXT:
      this.point = newPoint;
      break;
    case LINE:
      switch (mouseEvent.getButton())
      {
      case MouseEvent.BUTTON1:
	//System.err.println(mouseEvent);
	this.drawLine(newPoint);
	break;
      case MouseEvent.BUTTON3:
	this.point = null;
	break;
      default:
	System.err.println(String.format("got mouse button %d", mouseEvent.getButton()));
	break;
      }
      break;
    default:
      throw new RuntimeException("unknown drawing mode");
    }
  }
}
/*
class SimpleWhiteboardMenuActionListener implements ActionListener
{
  public void actionPerformed(ActionEvent actionEvent)
  {
    System.err.println(String.format("menu action: %s", actionEvent.getActionCommand()));
  }
}
*/

public class SimpleWhiteboard extends JFrame
{
  
    /*
    private JScrollPane scrollPane;
    private SimpleWhiteboardPanel simpleWhiteboardPanel;
    private SimpleWhiteboardControls simpleWhiteboardControls;
    private JMenuBar menuBar;
    private SimpleWhiteboardMenuActionListener menuActionListener;
    */
  
    private SimpleWhiteboardPanel simpleWhiteboardPanel;
    
  public SimpleWhiteboard(String nodename, int width, int height)
  {
    super(String.format("<student id> whiteboard: %s", nodename));
    /*
    this.simpleWhiteboardPanel = new SimpleWhiteboardPanel(width, height);
    this.scrollPane = new JScrollPane(this.simpleWhiteboardPanel);
    this.getContentPane().add(this.scrollPane);
    this.simpleWhiteboardControls = new SimpleWhiteboardControls(this.simpleWhiteboardPanel);
    this.getContentPane().add(this.simpleWhiteboardControls, BorderLayout.SOUTH);
    this.menuBar = new JMenuBar();
    this.menuActionListener = new SimpleWhiteboardMenuActionListener();
    JMenu networkMenu = new JMenu("Network");
    JMenuItem connectItem = new JMenuItem("Connect");
    connectItem.addActionListener(this.menuActionListener);
    JMenuItem disconnectItem = new JMenuItem("Disconnect");
    disconnectItem.addActionListener(this.menuActionListener);
    networkMenu.add(connectItem);
    networkMenu.add(disconnectItem);
    this.menuBar.add(networkMenu);
    this.setJMenuBar(this.menuBar);
    */
    // this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  }

  public SimpleWhiteboardPanel getWhiteboardPanel()
  {
    return (this.simpleWhiteboardPanel);
  }
}
