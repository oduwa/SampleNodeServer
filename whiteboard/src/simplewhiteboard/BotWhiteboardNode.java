/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplewhiteboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author Odie
 */
public class BotWhiteboardNode extends WhiteboardNode {

    public BotWhiteboardNode(String nodename, int width, int height) {
        super(nodename, width, height);
    }

    /**
     * 
     * @param points
     * @param colors
     * @param texts
     * @param textPoints
     * @param orderList
     * @param isSlowMotion 
     */
    public void replaceScreen(ArrayList<Point> points, ArrayList<Color> colors,
            ArrayList<String> texts, ArrayList<Point> textPoints,
            ArrayList<WhiteboardController.DrawMode> orderList, boolean isSlowMotion) {

        String data = Utility.generateStringForViewComponents(points, colors, texts, textPoints, orderList);
        Utility.ViewBundle viewBundle = Utility.convertStringToViewBundle(data);
        
        ArrayList<Point> pointList = viewBundle.pointBundle.points;
        ArrayList<Color> colorList = viewBundle.pointBundle.colors;
        ArrayList<String> textList = viewBundle.textBundle.texts;
        ArrayList<Point> textPointList = viewBundle.textBundle.textPoints;
        
        int linePointIndex = 0;
        int textPointIndex = 0;
        for (int i = 0; i < viewBundle.actionOrderList.size(); i++) {
            System.out.println("ACTION: " + viewBundle.actionOrderList.get(i).name());
            if (viewBundle.actionOrderList.get(i) == WhiteboardController.DrawMode.LINE) {
                Point point = pointList.get(linePointIndex);
                Color color = viewBundle.colors.get(i);
                this.whiteboardControls.color = color;
                this.whiteboardControls.drawLineInView(point);
                
                if (isSlowMotion) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BotWhiteboardNode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                linePointIndex++;
            } else {
                Point point = textPointList.get(textPointIndex);
                String text = textList.get(textPointIndex);
                Color color = viewBundle.colors.get(i);
                this.whiteboardControls.point = point;
                this.whiteboardControls.color = color;
                this.whiteboardControls.drawStringInView(text);
                this.whiteboardControls.point = null;
                
                if (isSlowMotion) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BotWhiteboardNode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                textPointIndex++;
            }
        }
    }
    
    public static void main(String[] args){
        JFrame.setDefaultLookAndFeelDecorated(true);
        BotWhiteboardNode n = new BotWhiteboardNode("Bot1", 1000, 600);
        
        n.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        n.setPreferredSize(new Dimension(400, 300));
        n.pack();
        n.setVisible(true);
        
        
        n.whiteboardControls.point = null;
        n.whiteboardControls.color = null;
        automateDrawing(n, false);
        n.whiteboardControls.point = null;
        n.whiteboardControls.color = null;
        n.startListening();
    }
    
    private static void automateDrawing(BotWhiteboardNode node, boolean isSlowMotion){
        ArrayList<Point> pointList = new ArrayList<Point>();
        ArrayList<Color> colorList = new ArrayList<Color>();
        ArrayList<String> textList = new ArrayList<String>();
        ArrayList<Point> textPointList = new ArrayList<Point>();
        ArrayList<WhiteboardController.DrawMode> orderList = 
                new ArrayList<WhiteboardController.DrawMode>();
        String text = "robot drawing circle with Midpoint Algorithm";
        Color[] color = {Color.blue, Color.red, Color.MAGENTA, Color.PINK, Color.GREEN};
        Random rand = new Random();
        
        
        pointList = drawCircle(150, 150, 25);
        
        // Write text
        for(int i = 0; i < text.length(); i++){
            colorList.add(color[rand.nextInt(color.length)]);
            textList.add(String.valueOf(text.charAt(i)));
            textPointList.add(new Point(10+(i*10),25));
            orderList.add(WhiteboardController.DrawMode.TEXT);
        }
        
        // Draw circle
        for(int i = 0; i < pointList.size(); i++){
            colorList.add(color[rand.nextInt(color.length)]);
            orderList.add(WhiteboardController.DrawMode.LINE);
        }
        
        node.replaceScreen(pointList, colorList, textList, textPointList, orderList, true);
    }
    
    public static ArrayList<Point> drawCircle(final int centerX, final int centerY, final int radius) {
        ArrayList<Point> pointList = new ArrayList<Point>();
        
		int d = (5 - radius * 4)/4;
		int x = 0;
		int y = radius;
		Color circleColor = Color.white;
 
		do {
			pointList.add(new Point(centerX + x, centerY + y));
                        pointList.add(new Point(centerX + x, centerY - y));
                        pointList.add(new Point(centerX - x, centerY + y));
                        pointList.add(new Point(centerX - x, centerY - y));
                        pointList.add(new Point(centerX + y, centerY + x));
                        pointList.add(new Point(centerX + y, centerY - x));
                        pointList.add(new Point(centerX - y, centerY + x));
                        pointList.add(new Point(centerX - y, centerY - x));

			if (d < 0) {
				d += 2 * x + 1;
			} else {
				d += 2 * (x - y) + 1;
				y--;
			}
			x++;
		} while (x <= y);
                
                return pointList;
 
	}
}
