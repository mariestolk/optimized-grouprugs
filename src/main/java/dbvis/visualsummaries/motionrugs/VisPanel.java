package dbvis.visualsummaries.motionrugs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

import dbvis.visualsummaries.data.SessionData;

/**
 * VisPanel contains and transforms the rugs for display in the GUI
 *
 * @author Juri Buchmüller, University of Konstanz
 * <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public class VisPanel extends JPanel {

    private BufferedImage bf;
    SessionData sd = SessionData.getInstance();

    private int curX = 0;
    private String stratid;
    private final Font font = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    private List<RugMouseMotionListener> listeners = new ArrayList<>();

    public VisPanel(BufferedImage bf, String stratid) {
        super();

        this.bf = bf;
        this.stratid = stratid;
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                curX = e.getX();
                notifyXChange(e.getX());
                notifyRugVisualizable(stratid);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                curX = e.getX();
                notifyXChange(e.getX());
                notifyRugVisualizable(stratid);
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        this.setSize(bf.getWidth(), bf.getHeight());
        this.setPreferredSize(new Dimension(bf.getWidth(), this.getHeight()));
        this.setMaximumSize(new Dimension(bf.getWidth(), this.getHeight()));

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform af = new AffineTransform();
        af.scale(1, (double) this.getHeight() / bf.getHeight());
        AffineTransformOp afo = new AffineTransformOp(af, AffineTransformOp.TYPE_BICUBIC);

        g2.drawImage(bf, afo, 0, 0);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(curX, 0, curX, this.getHeight());
        drawStatLabel(g2, curX+5, 100, ""+sd.getCurrentDataSet().getStatisticsOfStrategy(stratid).getKSdistValues()[curX]);
        
        //draw a label over the rug to show which strategy was used
        drawStratLabel(g2, 20, 30, stratid);
    }

    public void adListener(RugMouseMotionListener toAdd) {
        listeners.add(toAdd);

    }

    public void notifyXChange(int x) {
        for (RugMouseMotionListener rml : listeners) {
            rml.rugXHasChanged(x);
        }
    }

    public void notifyRugVisualizable(String stratid) {
        boolean visualizable = false;
        if(stratid.equals("First principal component") || stratid.startsWith("Clairvoyant (interpolate)") || stratid.startsWith("Clairvoyant (slow chase)") ) {
            visualizable = true;
        }
        for (RugMouseMotionListener rml : listeners) {
            rml.rugStratVisualizable(visualizable, stratid);
        }
    }

    void setCurX(int x) {
        curX = x;
    }

    private void drawStatLabel(Graphics2D g2, int x, int y, String label) {
        g2.translate(x, y);
        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout tl = new TextLayout(label, font, frc);
        Shape shape = tl.getOutline(null);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(Color.black);
        g2.draw(shape);
        g2.setColor(Color.white);
        g2.fill(shape);
        g2.translate(-x, -y);
    }
    
    private void drawStratLabel(Graphics2D g2, int x, int y, String label) {
        g2.translate(x, y);
        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout tl = new TextLayout(label, font.deriveFont(20F), frc);
        Shape shape = tl.getOutline(null);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(Color.black);
        g2.draw(shape);
        g2.setColor(Color.white);
        g2.fill(shape);
        g2.translate(-x, -y);
    }

}
