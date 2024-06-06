/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.motionrugs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Set;
import javax.swing.JPanel;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.SessionData;

/**
 *
 * @author Juri Buchmueller <motionrugs@dbvis.inf.uni-konstanz.de>
 */
public class MovePanel extends JPanel {

    SessionData sd;
    int curX = 0;
    String stratid = "";
    boolean stratVis = false;
    boolean orderVis = false;
    int borderoffset = 20;

    public MovePanel() {
        sd = SessionData.getInstance();
    }

    @Override
    public void paintComponent(Graphics g) {

        DataPoint[] frame = sd.getCurrentDataSet().getSingleFrame(curX);
        
        if (!stratid.equals("")) {
            frame = sd.getCurrentDataSet().getData(stratid)[curX];
        }
        

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int height = this.getHeight();
        int width = this.getWidth();
        g2.setColor(Color.GRAY);
        g2.fillRect(0, 0, width, height);

        int data_minx = ((Double) Math.floor(sd.getDataset(sd.getCurrentDataSetID()).getMin("x"))).intValue();
        int data_miny = ((Double) Math.floor(sd.getDataset(sd.getCurrentDataSetID()).getMin("y"))).intValue();

        int data_maxx = ((Double) Math.ceil(sd.getDataset(sd.getCurrentDataSetID()).getMax("x"))).intValue();
        int data_maxy = ((Double) Math.ceil(sd.getDataset(sd.getCurrentDataSetID()).getMax("y"))).intValue();

        double data_width = data_maxx - data_minx;
        double data_height = data_maxy - data_miny;

        double heightratio = data_height / height;
        double viewHeight = height;
        double viewWidth = ((Double) (data_width / heightratio)).intValue();

        //System.out.println("DATA RATIO: " + data_width + "/" + data_height + " " + (data_width*1.0/data_height));
        //System.out.println("HEIGHTRATIO: " + heightratio + " (" + data_height + "/" + height + ") new width = " + viewWidth + " height = " + height);
        int viewX = ((Double) ((width * .5) - (.5 * viewWidth))).intValue();
        int viewY = 0;

        g2.setColor(Color.WHITE);
        g2.fillRect(viewX, viewY, ((Double) viewWidth).intValue(), ((Double) viewHeight).intValue());

        int w = ((Double) (viewWidth * 0.1)).intValue();
        int h = ((Double) (viewHeight * 0.1)).intValue();
        g2.setColor(Color.lightGray);
        g2.drawRect(viewX + w, viewY + h, 8 * w, 8 * h);

        g2.translate(viewX + w, viewY + h);

        double scalex = viewWidth*.8 / data_maxx;
        double scaley = viewHeight*.8 / data_maxy;

        g2.scale(scalex, scaley);

        if (stratVis) {
            //find mean of each coordinate
            double[] mean = new double[2];

            for (int i = 0; i < frame.length; i++) {
                mean[0] += frame[i].getX();
                mean[1] += frame[i].getY();
            }
            mean[0] = mean[0] / frame.length;
            mean[1] = mean[1] / frame.length;

            // draw the ellipse for PCA
            g2.setColor(Color.LIGHT_GRAY);
            g2.rotate(sd.getCurrentDataSet().getDirection(stratid, curX), mean[0], mean[1]);

            double[] eigenvalues = sd.getCurrentDataSet().getEigenvalues(stratid, curX);
            Ellipse2D.Double ellipse = new Ellipse2D.Double(mean[0] - eigenvalues[0] / 4, mean[1] - eigenvalues[1] / 4, eigenvalues[0] / 2, eigenvalues[1] / 2);
            g2.fill(ellipse);
            g2.draw(ellipse);

            g2.setColor(Color.BLUE);
            Line2D.Double shape2 = new Line2D.Double(mean[0] - eigenvalues[0] / 4, mean[1], mean[0] + eigenvalues[0] / 4, mean[1]);
            g2.draw(shape2);
            g2.rotate(-sd.getCurrentDataSet().getDirection(stratid, curX), mean[0], mean[1]);
        }
        
        
        
        int prevx = Integer.MIN_VALUE, prevy = Integer.MIN_VALUE;
        Color orderColor;
        float brightness = 0.3f;
        for (DataPoint dp : frame) {
            int x = ((Double) dp.getX()).intValue();
            int y = ((Double) dp.getY()).intValue();
            
            if (orderVis && prevx != Integer.MIN_VALUE && prevy != Integer.MIN_VALUE) {
                orderColor = Color.getHSBColor(0.58f, 0.75f, brightness);
                brightness += 0.7f / frame.length;
                g2.setColor(orderColor);
                g2.drawLine(prevx, prevy, x, y);
            } else {
                g2.setColor(Color.BLACK);
            }
            
            g2.fillOval(x - (int) (3 / scalex), y - (int) (3 / scalex), (int) (6 / scalex), (int) (6 / scalex));
            prevx = x;
            prevy = y;
        }
        
//
//        // draw extra rectangles to crop ellipse and line outside frame
//        g2.scale(1 / scalex, 1 / scaley);
//        g2.translate(-neworiginx, -neworiginy);
//        g2.setColor(Color.GRAY);
//        g2.fillRect(0, 0, neworiginx, height);
//        g2.fillRect(neworiginx + (int) Math.round(maxx * scalex), 0, neworiginx, height);
    }

    public void updateMovePanel(int curX) {
        this.setBackground(Color.WHITE);
        this.curX = curX;
        this.repaint();
        this.getParent().setBackground(Color.GRAY);
    }

    public void updateMovePanel(boolean stratVis, String stratid, boolean orderVis) {
        this.setBackground(Color.WHITE);
        this.stratVis = stratVis;
        this.stratid = stratid;
        this.orderVis = orderVis;
        this.repaint();
        this.getParent().setBackground(Color.GRAY);
    }
    
    public void resetMovePanel(boolean orderVis) {
        this.setBackground(Color.WHITE);
        this.stratVis = false;
        this.stratid = "";
        this.orderVis = orderVis;
        this.curX = 0;
        this.repaint();
        this.getParent().setBackground(Color.GRAY);
    }
    
    public void updateMovePanel(boolean orderVis) {
        updateMovePanel(this.stratVis, this.stratid, orderVis);
    }
}
