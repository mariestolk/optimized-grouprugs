/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.motionrugs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 *
 * @author Juri Buchmueller <motionrugs@dbvis.inf.uni-konstanz.de>
 */
public class RugPanel extends JPanel{
    
    VisPanel vp = null;
    
    public RugPanel(BufferedImage bf, String stratid){
        
        StatPanel sp = new StatPanel(bf.getHeight(), stratid);
        this.setPreferredSize(new Dimension(bf.getWidth()+sp.getWidth(), bf.getHeight()));
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setBorder(BorderFactory.createLineBorder(Color.red));
        this.add(sp);
        vp = new VisPanel(bf, stratid);
        this.add(vp);
        
        //this.revalidate();
        //this.repaint();
    }

    void setCurX(int x) {
        vp.setCurX(x);
    }

    void addListener(RugMouseMotionListener listener) {
        vp.adListener(listener);
    }
    
}
