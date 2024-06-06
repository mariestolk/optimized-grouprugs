package dbvis.visualsummaries.motionrugs;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import dbvis.visualsummaries.color.BinnedPercentileColorMapper;
import dbvis.visualsummaries.color.DistanceToPreviousFrameColorMapper;
import dbvis.visualsummaries.color.LinearHSVColorMapper;
import dbvis.visualsummaries.color.TwoDColorMapper;
import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.data.StrategyStatistics;

/**
 * PNGWriter is responsible for the creation of the visualization images. It
 * applies Colormaps and returns BufferedImages. Also, saves the resulting
 * images.
 *
 * @author Juri Buchmüller, University of Konstanz
 *         <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public class PNGWriter {

    /**
     *
     * According to a chosen Colormapper, creates a BufferedImage of a Rugs
     * using one feature, saves it to the default project directory and returns
     * the image for display in the GUI
     *
     * @param da        the array with ordered values
     * @param min       min value of the feature values for the color mapping
     * @param max       max value of the feature values for the color mapping
     * @param decs      the percentiles (bins) for the colors (limited to 10
     *                  currently)
     * @param featureID the name of the displayed feature
     * @param dsname    the name of the displayed dataset
     * @param stratid   the name of the chosen strategy
     * @param cmName    the name of the chosen color mapper
     * @param stats     Strategy Statistics object
     * @return the MotionRug created from the ordered data
     */
    public static BufferedImage drawAndSaveRugs(SessionData sd, String featureID, String dsname, String stratid,
            String cmName, List<String> features) {
        DataSet current = sd.getCurrentDataSet();
        DataPoint[][] da = current.getData(stratid);
        BufferedImage awtImage = new BufferedImage(da.length, da[0].length, BufferedImage.TYPE_INT_RGB);

        double min = 0.0;
        double max = 0.0;
        Double[] decs = new Double[0];
        if (features.contains(featureID)) {
            min = current.getMin(featureID);
            max = current.getMax(featureID);
            decs = current.getDeciles(featureID);
        }

        LinearHSVColorMapper lb2rcm = new LinearHSVColorMapper(min, max, BlueToRedColors());
        BinnedPercentileColorMapper bpb2rcm = new BinnedPercentileColorMapper(decs, min, max, BlueToRedColors());
        LinearHSVColorMapper lgscm = new LinearHSVColorMapper(min, max, GrayscaleColors());
        DistanceToPreviousFrameColorMapper dtpf = new DistanceToPreviousFrameColorMapper();
        TwoDColorMapper twodcolormapper = new TwoDColorMapper();
        LinearHSVColorMapper kscm;

        int maxx = (int) Math.ceil(current.getMax("x"));
        int maxy = (int) Math.ceil(current.getMax("y"));
        System.out.println(maxx + " " + maxy);
        twodcolormapper.setScale(maxx, maxy);

        double[] minmax;

        Color bgc = new JPanel().getBackground();

        for (int x = 0; x < da.length; x++) {
            for (int y = 0; y < da[x].length; y++) {
                try {
                    if (!features.contains(featureID)) {
                        Color color = bgc;
                        StrategyStatistics stats = sd.getCurrentDataSet().getStatisticsOfStrategy(stratid);
                        double threshold = 0.0;
                        double threshold2 = Double.NEGATIVE_INFINITY;

                        if (x > 0) {
                            switch (featureID) {
                                case "input measure (dist)":
                                    threshold = (da[x].length / 2) - (10 * stats.getKSdistInputValues()[x - 1]);
                                    break;
                                case "input measure (rank)":
                                    threshold = (da[x].length / 2) - (40 * stats.getKSrankInputValues()[x - 1]);
                                    break;
                                case "projection measure":
                                    threshold = (da[x].length / 2) - (12 * stats.getKSprojValues()[x - 1]);
                                    break;
                                case "stability (dist)":
                                    threshold = (da[x].length / 2) - (stats.getKSprojValues()[x - 1]
                                            / (30 * stats.getKSdistInputValues()[x - 1]));
                                    break;
                                case "stability (rank)":
                                    threshold = (da[x].length / 2) - (stats.getKSprojValues()[x - 1]
                                            / (30 * stats.getKSrankInputValues()[x - 1]));
                                    break;
                                case "stability + spatial q. (dist)":
                                    threshold = (da[x].length / 2) - (12 * stats.getKSprojValues()[x - 1]);
                                    threshold2 = (da[x].length / 2) - (2 * stats.getKSdistValues()[x - 1]);
                                    break;
                                case "stability + spatial q. (rank)":
                                    threshold = (da[x].length / 2) - (12 * stats.getKSprojValues()[x - 1]);
                                    threshold2 = (da[x].length / 2) - (2 * stats.getKSrankValues()[x - 1]);
                                    break;
                            }

                            if (y > threshold && y < da[x].length - threshold) {
                                color = new Color(0, 102, 202);
                            }

                            if (threshold2 > Double.NEGATIVE_INFINITY && y < da[x].length / 2) {
                                color = bgc;
                                if (y > threshold2) {
                                    color = new Color(255, 190, 0);
                                }
                            }
                        }

                        awtImage.setRGB(x, y, color.getRGB());
                    } else {
                        if (da[x][y].getValue(featureID) < min) {
                            System.out.println("ERROR: " + featureID + " " + da[x][y].getValue(featureID) + "<" + min
                                    + ", id " + min + ", frame " + y);
                        }

                        switch (cmName) {
                            case "Binned percentile Blue to Red":
                                awtImage.setRGB(x, y, bpb2rcm.getColorByValue(da[x][y].getValue(featureID)).getRGB());
                                break;
                            case "Linear Blue to Red":
                                awtImage.setRGB(x, y, lb2rcm.getColorByValue(da[x][y].getValue(featureID)).getRGB());
                                break;
                            case "Linear Grayscale":
                                awtImage.setRGB(x, y, lgscm.getColorByValue(da[x][y].getValue(featureID)).getRGB());
                                break;
                            case "Direction":
                                double[] eigenvalues = sd.getCurrentDataSet().getEigenvalues(stratid, x);
                                double eigenratio = Math.min(5, eigenvalues[1] / eigenvalues[0]);

                                Color pixel = bgc;

                                double c1 = 1.0 / 1.9;
                                double c2 = 1.0 / 6;
                                double threshold = (eigenratio - c1) * Math.abs(1.0 / (1.0 - c1))
                                        * (da[x].length * (1.0 / 2 - c2));

                                // if(y == Math.round(da[x].length/2 * (1 - 0.34)) || y ==
                                // Math.round(da[x].length/2 * (1 - 0.39))
                                // || y == Math.round(da[x].length/2 * (1 - 0.43)) || y ==
                                // Math.round(da[x].length/2 * (1 - 0.46))
                                // || y == Math.round(da[x].length/2 * (1 - 0.59)) || y ==
                                // Math.round(da[x].length/2 * (1 - 0.61))) {
                                // if(y == Math.round(da[x].length/2 * (1 - 0.35)) || y ==
                                // Math.round(da[x].length/2 * (1 - 0.53))
                                // || y == Math.round(da[x].length/2 * (1 - 0.78)) ) {
                                // pixel = Color.BLACK;
                                // } else {
                                if (y > da[x].length / 2 * (1 - (eigenvalues[1] / eigenvalues[0]))
                                        && y < da[x].length / 2 * (1 + (eigenvalues[1] / eigenvalues[0]))) {
                                    double hue = (sd.getCurrentDataSet().getDirection(stratid, x) + Math.PI)
                                            / (Math.PI);
                                    pixel = Color.getHSBColor((float) hue, 0.5f, 1f);
                                }
                                // }
                                awtImage.setRGB(x, y, pixel.getRGB());
                                break;
                            case "Distance of position to previous frame":
                                awtImage.setRGB(x, y, dtpf.getColorByValue(da, x, y).getRGB());
                            case "Spatial Coloring":
                                // System.out.println((int)Math.floor(da[x][y].getX()) + "," +
                                // (int)Math.floor(da[x][y].getY()));
                                awtImage.setRGB(x, y, twodcolormapper
                                        .getColor1((int) Math.floor(da[x][y].getX()), (int) Math.floor(da[x][y].getY()))
                                        .getRGB());
                                break;
                            case "Spatial Feature Coloring":
                                awtImage.setRGB(x, y,
                                        twodcolormapper.getColor((int) Math.floor(da[x][y].getX()),
                                                (int) Math.floor(da[x][y].getY()), da[x][y].getValue(featureID), min,
                                                max).getRGB());
                                break;
                            case "KSdist Coloring":
                                // minmax = current.getPointKSdistMinMax(stratid);
                                kscm = new LinearHSVColorMapper(2.81, 37.5, BlackToYellowColors());
                                awtImage.setRGB(x, y,
                                        kscm.getColorByValueCapped(current.getPointKSdist(stratid, x)[da[x][y].getId()])
                                                .getRGB());
                                break;
                            case "KSrank Coloring":
                                // minmax = current.getPointKSrankMinMax(stratid);
                                kscm = new LinearHSVColorMapper(2.81, 37.5, BlackToBrownColors());
                                awtImage.setRGB(x, y,
                                        kscm.getColorByValueCapped(current.getPointKSrank(stratid, x)[da[x][y].getId()])
                                                .getRGB());
                                break;
                            case "KSproj Coloring":
                                // minmax = current.getPointKSprojMinMax(stratid);
                                kscm = new LinearHSVColorMapper(2.81, 6.25, BlackToBlueColors());
                                awtImage.setRGB(x, y,
                                        kscm.getColorByValueCapped(current.getPointKSproj(stratid, x)[da[x][y].getId()])
                                                .getRGB());
                                break;

                        }
                    }

                } catch (Exception ex) {
                    System.out.println(featureID);
                    Logger.getLogger(PNGWriter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        String userdir = System.getProperty("user.home");
        File imgfolder = new File(userdir + "/motionrugs");
        if (!imgfolder.exists()) {
            imgfolder.mkdir();
        }

        File outputfile = new File(
                userdir + "/motionrugs/" + dsname + "_" + featureID + "_" + stratid + "_" + cmName + ".png");
        try {
            ImageIO.write(awtImage, "png", outputfile);
            return awtImage;
        } catch (IOException ex) {
            Logger.getLogger(PNGWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return awtImage;
    }

    private static Color[] BlueToRedColors() {
        Color c1 = new Color(165, 0, 38);
        Color c2 = new Color(215, 48, 39);
        Color c3 = new Color(244, 109, 67);
        Color c4 = new Color(253, 174, 97);
        Color c5 = new Color(254, 224, 144);
        Color c6 = new Color(224, 243, 248);
        Color c7 = new Color(171, 217, 233);
        Color c8 = new Color(116, 173, 209);
        Color c9 = new Color(69, 117, 180);
        Color c10 = new Color(49, 54, 149);

        Color[] colors = { c10, c9, c8, c7, c6, c5, c4, c3, c2, c1 };
        return colors;
    }

    private static Color[] GreyToBlueColors() {
        Color c1 = new Color(0, 102, 202);
        Color c2 = new Color(10, 101, 190);
        Color c3 = new Color(20, 99, 177);
        Color c4 = new Color(30, 98, 165);
        Color c5 = new Color(40, 97, 152);
        Color c6 = new Color(50, 95, 140);
        Color c7 = new Color(60, 94, 127);
        Color c8 = new Color(70, 93, 115);
        Color c9 = new Color(80, 91, 102);
        Color c10 = new Color(90, 90, 90);

        Color[] colors = { c10, c9, c8, c7, c6, c5, c4, c3, c2, c1 };
        return colors;
    }

    private static Color[] GreyToYellowColors() {
        Color c1 = new Color(255, 190, 0);
        Color c2 = new Color(237, 179, 10);
        Color c3 = new Color(218, 168, 20);
        Color c4 = new Color(200, 157, 30);
        Color c5 = new Color(182, 146, 40);
        Color c6 = new Color(163, 134, 50);
        Color c7 = new Color(145, 123, 60);
        Color c8 = new Color(127, 112, 70);
        Color c9 = new Color(108, 101, 80);
        Color c10 = new Color(90, 90, 90);

        Color[] colors = { c10, c9, c8, c7, c6, c5, c4, c3, c2, c1 };
        return colors;
    }

    private static Color[] BlackToBlueColors() {
        Color c1 = new Color(0, 102, 202);
        Color c2 = new Color(6, 96, 185);
        Color c3 = new Color(11, 90, 168);
        Color c4 = new Color(17, 85, 151);
        Color c5 = new Color(22, 79, 134);
        Color c6 = new Color(28, 73, 118);
        Color c7 = new Color(33, 67, 101);
        Color c8 = new Color(39, 62, 84);
        Color c9 = new Color(44, 56, 67);
        Color c10 = new Color(50, 50, 50);

        Color[] colors = { c10, c9, c8, c7, c6, c5, c4, c3, c2, c1 };
        return colors;
    }

    private static Color[] BlackToYellowColors() {
        Color c1 = new Color(255, 190, 0);
        Color c2 = new Color(232, 174, 6);
        Color c3 = new Color(210, 159, 11);
        Color c4 = new Color(187, 143, 17);
        Color c5 = new Color(164, 128, 22);
        Color c6 = new Color(141, 112, 28);
        Color c7 = new Color(118, 97, 33);
        Color c8 = new Color(96, 81, 39);
        Color c9 = new Color(73, 66, 44);
        Color c10 = new Color(50, 50, 50);

        Color[] colors = { c10, c9, c8, c7, c6, c5, c4, c3, c2, c1 };
        return colors;
    }

    private static Color[] BlackToBrownColors() {
        Color c1 = new Color(197, 92, 40);
        Color c2 = new Color(181, 87, 41);
        Color c3 = new Color(164, 83, 42);
        Color c4 = new Color(148, 78, 43);
        Color c5 = new Color(132, 73, 44);
        Color c6 = new Color(115, 69, 46);
        Color c7 = new Color(99, 64, 47);
        Color c8 = new Color(83, 59, 48);
        Color c9 = new Color(66, 55, 49);
        Color c10 = new Color(50, 50, 50);

        Color[] colors = { c10, c9, c8, c7, c6, c5, c4, c3, c2, c1 };
        return colors;
    }

    private static Color[] GrayscaleColors() {
        Color c0 = new Color(255, 255, 255);
        Color c1 = new Color(229, 229, 229);
        Color c2 = new Color(204, 204, 204);
        Color c3 = new Color(178, 178, 178);
        Color c4 = new Color(153, 153, 153);
        Color c5 = new Color(127, 127, 127);
        Color c6 = new Color(102, 102, 102);
        Color c7 = new Color(76, 76, 76);
        Color c8 = new Color(51, 51, 51);
        Color c9 = new Color(25, 25, 25);

        Color[] colors = { c9, c8, c7, c6, c5, c4, c3, c2, c1 };
        return colors;
    }
}
