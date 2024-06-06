package dbvis.visualsummaries.color;

import java.awt.Color;

/**
 * HSV Color mapper
 *
 * @author Juri Buchmüller, University of Konstanz
 * <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public class LinearHSVColorMapper implements ColorMapper {

    double min;
    double max;
    double interval;
    Color[] steps;

    /**
     * HSVColorMapper instantiation with color map anchors. The given anchors
     * are used without further sorting or sanity checking for order.
     *
     * @param min the minimum value of the color scale
     * @param max the maximum value of the color scale
     * @param steps A Color[] containing the color anchors.
     */
    public LinearHSVColorMapper(double min, double max, Color[] steps) {
        this.min = min;
        this.max = max;
        this.steps = steps;
        interval = 1.0 / (steps.length - 1);
    }

    /**
     * Returns the color interpolated for the given value according to the given
     * color anchors in HSV color space. The value will be normalized given the
     * set min and max values. The HSV color interpolation is linear with the
     * shortest distance between two colors.
     *
     * @param value The value for which the associated color is to be calculated
     * @return The color associated with the given value
     * @throws Exception Is thrown if the given value is outside of the set
     * min/max-range.
     */
    @Override
    public Color getColorByValue(double value) throws Exception {
        if (value < min || value > max) {
            throw new Exception("The given value " + value + " is outside the preset value range. The range is set from " + min + " to " + max);
        }
        double normalizedValue = (value - min) / (max - min);

        int specificInterval = (int) Math.floor(normalizedValue / interval);

        Color c = new Color(128, 128, 128);
        float[] hsv = new float[3];
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsv);

        Color ret;
        if (normalizedValue >= 1.0) {
            ret = interpolate(steps[specificInterval - 1], steps[specificInterval], normalizedValue);
        } else {
            ret = interpolate(steps[specificInterval], steps[specificInterval + 1], normalizedValue);
        }
        return ret;
    }

    public Color getColorByValueCapped(double value) {
        Color ret;

        if (value < min) {
            ret = steps[0];
        } else if (value > max) {
            ret = steps[steps.length - 1];
        } else {
            double normalizedValue = (value - min) / (max - min);

            int specificInterval = (int) Math.floor(normalizedValue / interval);

            Color c = new Color(128, 128, 128);
            float[] hsv = new float[3];
            Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsv);

            if (normalizedValue >= 1.0) {
                ret = interpolate(steps[specificInterval - 1], steps[specificInterval], normalizedValue);
            } else {
                ret = interpolate(steps[specificInterval], steps[specificInterval + 1], normalizedValue);
            }
        }
        
        return ret;
    }

    /**
     * Interpolates between two given Colors in the HSV colorspace.
     *
     * @param a The first color
     * @param b The second color
     * @param dvalue The fraction of the distance between the colors to be
     * interpolated
     * @return The interpolated color based on the given fraction
     */
    private Color interpolate(Color a, Color b, double dvalue) {
        float value = (float) dvalue;
        float[] hsva = new float[3];
        Color.RGBtoHSB(a.getRed(), a.getGreen(), a.getBlue(), hsva);
        float[] hsvb = new float[3];
        Color.RGBtoHSB(b.getRed(), b.getGreen(), b.getBlue(), hsvb);

        float h = 0;
        float d = hsvb[0] - hsva[0];
        if (hsva[0] > hsvb[0]) {
            float temp = hsvb[0];
            hsvb[0] = hsva[0];
            hsva[0] = temp;
            d = -d;
            value = 1.0f - value;
        }
        if (d > .5) {
            hsva[0] += 1.0f;
            h = (hsva[0] + value * (hsvb[0] - hsva[0])) % 1.0f;
        } else if (d <= .5) {
            h = hsva[0] + value * d;
        }
        return Color.getHSBColor(h, hsva[1] + value * (hsvb[1] - hsva[1]), hsva[2] + value * (hsvb[2] - hsva[2]));
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public Color[] getSteps() {
        return steps;
    }

    public void setSteps(Color[] steps) {
        this.steps = steps;
        interval = 1.0 / (steps.length - 1);
    }
}
