/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.color;

import com.github.ajalt.colormath.HSV;
import com.github.ajalt.colormath.RGB;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author buchmueller
 */
public class TwoDColorMapper {

    BufferedImage originalImage;
    BufferedImage scaledImage;

    int scaleX;
    int scaleY;

    public TwoDColorMapper() {

        try {
            originalImage = ImageIO.read(new File("colormaps.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setScale(int x, int y) {
        int scaleX = x + 1;
        int scaleY = y + 1;

        scaledImage = new BufferedImage(
                scaleX, scaleY, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.drawImage(originalImage, 0, 0, x, y, null);
        graphics2D.dispose();
    }

    public Color getColor1(int x, int y) {
        // System.out.println(x + "," + y);
        return new Color(scaledImage.getRGB(x, y));
    }

    public Color getColor(int x, int y, double value, double min, double max) {
        Color baseColor = new Color(scaledImage.getRGB(x, y));
        RGB rgbbasecolor = new RGB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(),
                baseColor.getAlpha() / 255);
        HSV hsvbasecolor = rgbbasecolor.toHSV();
        int hsvvvalue = hsvbasecolor.getV();

        double percent = value / (max - min);

        int targethsvvvalueMod = (int) Math.ceil(hsvvvalue * percent);
        int targethsvvvalue = (int) Math.floor(100 * percent);

        // System.out.println("CURRENT V: " + hsvvvalue + " FEATURE: " + percent);
        // System.out.println("CMODIFIED: " + targethsvvvalueMod + " VNEW: " +
        // targethsvvvalue );
        // System.out.println("-----------------------------------------");
        // System.out.println("VALUES withv: " + (int)Math.ceil(hsvvvalue*percent) + "
        // ohnev: " + targethsvvvalue + " DIFF=" +
        // (targethsvvvalue-(int)Math.ceil(hsvvvalue*percent)));
        HSV hsvtargetcolor = null;
        try {
            hsvtargetcolor = new HSV(hsvbasecolor.getH(), hsvbasecolor.getS(), targethsvvvalue, 1);
        } catch (IllegalArgumentException ex) {
            System.out.println("VVAL: " + targethsvvvalue);
            System.out.println(ex.getStackTrace());
        }
        // System.out.println((int)Math.floor(255*percent));
        // Color opacityModified = new Color(baseColor.getRed(), baseColor.getGreen(),
        // baseColor.getBlue(), 0);
        Color brightnessModified = new Color(hsvtargetcolor.toRGB().getR(), hsvtargetcolor.toRGB().getG(),
                hsvtargetcolor.toRGB().getB());

        return brightnessModified;
    }

}
