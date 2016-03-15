/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package simplewhiteboard;

import java.awt.Color;
import java.awt.List;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Odie
 */
public class Utility {
    
    public static final int TCP_PORT = 56666;
    
    public static class PointBundle{
        ArrayList<Point> points;
        ArrayList<Color> colors;
    }
    
    public static class TextBundle{
        ArrayList<String> texts;
        ArrayList<Point> textPoints;
    }
    
    public static class ViewBundle{
        PointBundle pointBundle;
        TextBundle textBundle;
        ArrayList<WhiteboardController.DrawMode> actionOrderList;
    }
    
    public static String convertPointsToString(ArrayList<Point> points){
        StringBuilder bldr = new StringBuilder();
        
        for(int i = 0; i < points.size(); i++){
            Point p = points.get(i);
            
            bldr.append(p.getX());
            bldr.append(",");
            bldr.append(p.getY());
            
            if(i != points.size()-1){
                bldr.append(":");
            }
        }
        
        return bldr.toString();
    }
    
    public static ArrayList<Point> convertStringToPoints(String input){
        ArrayList<Point> points = new ArrayList<Point>();
        
        String[] pointStrings = input.split(":");
        for(String pointString : pointStrings){
            String[] pointComponents = pointString.split(",");
            if(pointComponents != null && pointComponents.length >= 2){
                double xComponent = Double.parseDouble(pointComponents[0]);
                double yComponent = Double.parseDouble(pointComponents[1]);
                Point p = new Point((int)xComponent, (int)yComponent);
                points.add(p);
            }
        }
        
        return points;
    }
    
    public static String generateStringForPointsAndColors(ArrayList<Point> points,ArrayList<Color> colors){
        StringBuilder bldr = new StringBuilder();
        
        for(int i = 0; i < points.size(); i++){
            Point p = points.get(i);
            Color color = colors.get(i);
            
            bldr.append(p.getX());
            bldr.append(",");
            bldr.append(p.getY());
            bldr.append(":");
            bldr.append(color.getRGB());
            
            if(i != points.size()-1){
                bldr.append(";");
            }
        }
        
        return bldr.toString();
    }
    
    
    public static PointBundle convertStringToPointsAndColors(String input){
        PointBundle bundle = new PointBundle();
        ArrayList<Point> points = new ArrayList<Point>();
        ArrayList<Color> colors = new ArrayList<Color>();
        
        String[] bundleStrings = input.split(";");
        for(String bundleString : bundleStrings){
            String[] bundleComponents = bundleString.split(":");
            String[] pointComponents = bundleComponents[0].split(",");
            
            if(pointComponents != null && pointComponents.length >= 2){
                double xComponent = Double.parseDouble(pointComponents[0]);
                double yComponent = Double.parseDouble(pointComponents[1]);
                Point p = new Point((int)xComponent, (int)yComponent);
                points.add(p);
            }
            
            colors.add(new Color(Integer.parseInt(bundleComponents[1])));
        }
        
        bundle.points = points;
        bundle.colors = colors;
        
        return bundle;
    }
    
    public static String generateStringForText(ArrayList<String> texts, ArrayList<Point> textPoints){
        StringBuilder bldr = new StringBuilder();
        
        for(int i = 0; i < texts.size(); i++){
            String text = texts.get(i);
            Point textPoint = textPoints.get(i);
            
            bldr.append(text);
            bldr.append(":");
            bldr.append(textPoint.getX());
            bldr.append(",");
            bldr.append(textPoint.getY());
            
            
            if(i != texts.size()-1){
                bldr.append(";");
            }
        }
        
        return bldr.toString();
    }
    
    public static String generateStringForDrawingOrder(ArrayList<WhiteboardController.DrawMode> orderList){
        StringBuilder bldr = new StringBuilder();
        
        for(int i = 0; i < orderList.size(); i++){
            String action = null;
            
            if(orderList.get(i) == WhiteboardController.DrawMode.LINE){
                action = "p";
            }
            else{
                action = "s";
            }
            
            bldr.append(action);
                 
            if(i != orderList.size()-1){
                bldr.append(";");
            }
        }
        
        return bldr.toString();
    }
    
    public static TextBundle convertStringToTextBundle(String input){
        TextBundle bundle = new TextBundle();
        
        ArrayList<String> texts = new ArrayList<String>();
        ArrayList<Point> textPoints = new ArrayList<Point>();
        
        String[] bundleStrings = input.split(";");
        
        for(String bundleString : bundleStrings){
            String[] bundleComponents = bundleString.split(":");
            String text = bundleComponents[0];
            String[] textPointComponents = bundleComponents[1].split(",");
            
            if(textPointComponents != null && textPointComponents.length >= 2){
                double xComponent = Double.parseDouble(textPointComponents[0]);
                double yComponent = Double.parseDouble(textPointComponents[1]);
                Point p = new Point((int)xComponent, (int)yComponent);
                textPoints.add(p);
            }
            
            texts.add(text);
        }
        
        bundle.texts = texts;
        bundle.textPoints = textPoints;
        
        return bundle;
    }
    
    public static ArrayList<WhiteboardController.DrawMode> convertStringToOrderList(String input){
        ArrayList<WhiteboardController.DrawMode> orderList = new ArrayList<WhiteboardController.DrawMode>();
        
        String[] actionStrings = input.split(";");
        
        for(String actionString : actionStrings){
            if(actionString.equalsIgnoreCase("p")){
                orderList.add(WhiteboardController.DrawMode.LINE);
            }
            else{
                orderList.add(WhiteboardController.DrawMode.TEXT);
            }
        }
        
        return orderList;
    }
    
    /**
     * 
     * 
     * @param points
     * @param colors
     * @param texts
     * @param textPoints
     * @return Returns string of the format 
     * <point_buffer_representation>%<text_buffer_representation>%<order_list_representation>
     */
    public static String generateStringForViewComponents(
      ArrayList<Point> points,ArrayList<Color> colors,ArrayList<String> texts, ArrayList<Point> textPoints, ArrayList<WhiteboardController.DrawMode> orderList){
        
        String pointsRepresentationString = Utility.generateStringForPointsAndColors(points, colors);
        String textsRepresentationString = Utility.generateStringForText(texts, textPoints);
        String drawingOrderRepresentation = Utility.generateStringForDrawingOrder(orderList);
        
        return pointsRepresentationString + "%" + textsRepresentationString + "%" + drawingOrderRepresentation;
    }
    
    public static ViewBundle convertStringToViewBundle(String input){
        ViewBundle viewBundle = new ViewBundle();
        
        String[] stringComponents = input.split("%");
        String pointsRepresentationString = stringComponents[0];
        String textsRepresentationString = stringComponents[1];
        String drawingOrderRepresentation = stringComponents[2];
        
        viewBundle.pointBundle = Utility.convertStringToPointsAndColors(pointsRepresentationString);
        viewBundle.textBundle = Utility.convertStringToTextBundle(textsRepresentationString);
        viewBundle.actionOrderList = Utility.convertStringToOrderList(drawingOrderRepresentation);
        
        return viewBundle;
    }
    
    public static Point convertStringToPoint(String input) {
        Point p = null;
        String[] pointComponents = input.split(",");
        
        if (pointComponents != null && pointComponents.length == 2) {
            double xComponent = Double.parseDouble(pointComponents[0]);
            double yComponent = Double.parseDouble(pointComponents[1]);
            p = new Point((int) xComponent, (int) yComponent);
        }

        return p;
    }
    
    public static void writeTcpMessage(String msg, DataOutputStream output){
        try {
            // Write length
            output.writeInt(msg.length());
            
            // Write message
            output.writeBytes(msg);
        } 
        catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).
                    log(Level.SEVERE, null, ex);
            System.out.println(ex);
        }
    }
    
    public static String readTcpMessage(DataInputStream input){
        String msg = null;
        
        try {
            // If a msg length and more has been sent
            if (input.available() >= 1) {
                // Read length
                int msgLength = input.readInt();

                // Read message
                byte[] msgData = new byte[msgLength];
                input.readFully(msgData);
                msg = new String(msgData);
                
                if(msg.length() <= 0){
                    msg = null;
                }
            } 
            else {
                msg = null;
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        
        return msg;
    }
    
}
