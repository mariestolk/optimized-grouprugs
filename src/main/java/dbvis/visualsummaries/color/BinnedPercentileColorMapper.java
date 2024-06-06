package dbvis.visualsummaries.color;

import java.awt.Color;

/**
 * Provides value-color mapping according to a given set of percentiles
 *
 * @author Juri Buchmüller, University of Konstanz
 * <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public class BinnedPercentileColorMapper implements ColorMapper {

    private double min;
    private double max;
    private Double[] percentiles;
    private Color[] colors;

    public BinnedPercentileColorMapper(Double[] percentiles, double min, double max, Color[] colors) {
        this.min = min;
        this.max = max;
        this.percentiles = percentiles;
        this.colors = colors;
    }

    @Override
    public Color getColorByValue(double value) throws Exception {
        if (value < min || value > max) {
            throw new Exception("The given value " + value + " is outside the preset value range. The range is set from " + min + " to " + max);
        }
        //Todo: Normalization if quantiles and colors differ.
        return colors[searchBin(value)];
    }

    public int searchBin(double value) throws Exception {
        if (value <= percentiles[0]) {
            return 0;
        }
        if (value > percentiles[percentiles.length - 1]) {
            return percentiles.length;
        }

        for (int i = 0; i < percentiles.length; i++) {

            if (value > percentiles[i] && value <= percentiles[i + 1]) {
                return i + 1;
            }
        }
        return -1;
    }

}
