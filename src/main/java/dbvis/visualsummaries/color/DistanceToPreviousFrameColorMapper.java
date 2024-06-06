package dbvis.visualsummaries.color;

import java.awt.Color;

import dbvis.visualsummaries.data.DataPoint;

public class DistanceToPreviousFrameColorMapper {

    private int max = 150;

    public DistanceToPreviousFrameColorMapper() {
    }

    public Color getColorByValue(DataPoint[][] data, int frame, int pos) throws Exception {
        if (frame == 0) {
            return Color.WHITE;
        }

        int idAtFrame = data[frame][pos].getId();
        int prevPos = -1;
        for (int i = 0; i < data[(frame - 1)].length; i++) {
            if (data[(frame - 1)][i].getId() == idAtFrame) {
                prevPos = i;
                break;
            }
        }

        int dif = pos - prevPos;

        for (int a = 1; a < data.length; a++) {
            for (int b = 0; b < data[0].length; b++) {

                for (int c = 0; c < data[0].length; c++) {
                }
            }
        }

        if (dif > max) {
            dif = max;
        }
        if (dif < -max) {
            dif = -max;
        }

        Color maxcol = new Color(255, 116, 0);
        Color mincol = new Color(163, 0, 255);

        if (dif == 0) {
            return Color.WHITE;
        }

        if (dif > 0) {
            float difr = dif / (max * 1.0F);
            return rgbint(Color.WHITE, maxcol, difr);
        }

        if (dif < 0) {
            float difr = Math.abs(dif) / (max * 1.0F);
            return rgbint(Color.WHITE, mincol, difr);
        }

        return Color.BLUE;
    }

    public Color rgbint(Color a, Color b, float t) {
        float newRed = a.getRed() + (b.getRed() - a.getRed()) * t;

        float newGreen = a.getGreen() + (b.getGreen() - a.getGreen()) * t;

        float newBlue = a.getBlue() + (b.getBlue() - a.getBlue()) * t;

        Color newCol = Color.BLACK;
        try {
            newCol = new Color(newRed / 255.0F, newGreen / 255.0F, newBlue / 255.0F);
        } catch (Exception e) {
            System.out.println("ERR t: " + t);
            System.out.println("ERRnew r: " + (a.getRed() + (b.getRed() - a.getRed()) * t));
            System.out.println("ERRnew g: " + (a.getGreen() + (b.getGreen() - a.getGreen()) * t));
            System.out.println("ERRnew b: " + (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
            System.out.println("ERRnew alpha: " + (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
        }
        return newCol;
    }
}
