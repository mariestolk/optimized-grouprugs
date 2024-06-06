package dbvis.visualsummaries.grouprugs.tgs;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import dbvis.visualsummaries.color.TwoDColorMapper;
import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.StatusGraph;

/**
 * Utility class containing various helper functions.
 */
public class Utils {

    /**
     * Function sets the color of a pixel in the image based on the color of the
     * corresponding data point. The function also updates the entityToPosition
     * array.
     * 
     * @param twodcolormapper  The color mapper.
     * @param awtImage         The image.
     * @param entityToPosition The entity to position array.
     * @param imageX           The x-coordinate of the pixel in the image.
     * @param imageY           The y-coordinate of the pixel in the image.
     * @param dataX            The x-coordinate of the data point.
     * @param dataY            The y-coordinate of the data point.
     * @throws Exception If the color cannot be set.
     */
    public static void setColor(
            TwoDColorMapper twodcolormapper,
            BufferedImage awtImage,
            Integer[][] entityToPosition,
            int imageX,
            int imageY,
            int dataX,
            int dataY) throws Exception {

        DataSet current = SessionData.getInstance().getCurrentDataSet();
        DataPoint[][] data = current.getBaseData();

        awtImage.setRGB(imageX, imageY,
                twodcolormapper
                        .getColor1((int) Math.floor(data[imageX][dataY].getX()),
                                (int) Math.floor(data[dataX][dataY].getY()))
                        .getRGB());

        entityToPosition[dataX][dataY] = imageY;

    }

    /**
     * Function returns the euclidean distance between two datapoints.
     * 
     * @param x1 The x-coordinate of the first datapoint.
     * @param y1 The y-coordinate of the first datapoint.
     * @param x2 The x-coordinate of the second datapoint.
     * @param y2 The y-coordinate of the second datapoint.
     * @return Euclidean distance between p1 and p2
     */
    public static double getEuclideanDistance(StatusGraph.Vertex v1, StatusGraph.Vertex v2) {
        double x1 = v1.getX();
        double y1 = v1.getY();
        double x2 = v2.getX();
        double y2 = v2.getY();

        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Function returns the euclidean distance between two datapoints.
     * 
     * @param p1 The first datapoint.
     * @param p2 The second datapoint.
     * @return Euclidean distance between p1 and p2
     */
    public static double getEuclideanDistance(DataPoint p1, DataPoint p2) {
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();

        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static BufferedImage createHistogramSpatialDist(double[] metric) throws IOException {

        Color yellow = Color.decode("#ffb81e");

        double maxMetricValue = 25; // Max visualized metric value
        int imageHeight = 40; // Based on Stable Visual Summaries paper.

        BufferedImage histogram = new BufferedImage(metric.length, imageHeight, BufferedImage.TYPE_INT_RGB);

        for (int frame = 0; frame < metric.length; frame++) {
            // int value = (int) (metric[frame] - min);
            int value = (int) ((metric[frame] / maxMetricValue) * (((imageHeight - 1))));

            for (int metric_height = 0; metric_height < value; metric_height++) {

                int y = (int) ((imageHeight) - (value - metric_height));

                // Value was higher than cap HEIGHT (37)
                if (y < 0) {
                    System.out.println("Spatial Metric value was too high for histogram. Frame: " + frame + " Value: "
                            + metric[frame] + " Metric Height: " + metric_height + " Y: " + y + " Value: " + value);
                    continue;
                }

                // decode color
                histogram.setRGB(frame, y, yellow.getRGB());
            }
        }

        // JFrame frame = new JFrame();
        // frame.setSize(800, 600);
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.add(new javax.swing.JLabel(new javax.swing.ImageIcon(histogram)));
        // frame.setVisible(true);

        saveHistogram("SpatialHistogramDist", histogram);

        return histogram;

    }

    public static BufferedImage createHistogramStabilityDist(double[] metric) throws IOException {

        int maxMetricValue = 5; // Max visualized metric value
        int imageHeight = 40;

        Color blue = Color.decode("#5ccce4");

        BufferedImage histogram = new BufferedImage(metric.length, imageHeight, BufferedImage.TYPE_INT_RGB);

        for (int frame = 0; frame < metric.length; frame++) {

            int value = (int) ((metric[frame] / maxMetricValue) * (((imageHeight - 1))));

            for (int metric_height = 0; metric_height < value; metric_height++) {

                int y = (int) ((imageHeight) - (value - metric_height));

                if (y < 0) {
                    System.out.println("Stability Metric value was too high for histogram. Frame: " + frame + " Value: "
                            + metric[frame] + " Metric Height: " + metric_height + " Y: " + y + " Value: " + value);
                    continue;
                }

                histogram.setRGB(frame, y, blue.getRGB());
            }

        }

        saveHistogram("StabilityDistHistogram", histogram);

        return histogram;

    }

    public static BufferedImage createHistogramStability(double[] metric) throws IOException {

        int maxMetricValue = 5; // Max visualized metric value
        int imageHeight = 40;

        Color blue = Color.decode("#5ccce4");

        BufferedImage histogram = new BufferedImage(metric.length, imageHeight, BufferedImage.TYPE_INT_RGB);

        for (int frame = 0; frame < metric.length; frame++) {

            int value = (int) ((metric[frame] / maxMetricValue) * (((imageHeight - 1))));

            for (int metric_height = 0; metric_height < value; metric_height++) {

                int y = (int) ((imageHeight) - (value - metric_height));

                if (y < 0) {
                    System.out.println("Stability Metric value was too high for histogram. Frame: " + frame + " Value: "
                            + metric[frame] + " Metric Height: " + metric_height + " Y: " + y + " Value: " + value);
                    continue;
                }

                histogram.setRGB(frame, y, blue.getRGB());
            }

        }

        saveHistogram("StabilityHistogram", histogram);

        return histogram;

    }

    /**
     * Create a histogram for the silhouette metric.
     * 
     * @param metric The silhouette metric list for each frame.
     * @return A BufferedImage of the histogram, ranging from -1 to 1.
     * @throws IOException
     */
    public static BufferedImage createHistogramSilhouette(double[] metric) throws IOException {

        Color green = Color.decode("#41db6a");

        int HEIGHT = 50; // HEIGHT arbitrarily chosen.

        BufferedImage histogram = new BufferedImage(metric.length, HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (int frame = 0; frame < metric.length; frame++) {

            int value = (int) (metric[frame] * (0.5 * (HEIGHT - 1)));

            if (value < 0) {
                for (int metric_height = 0; metric_height > value; metric_height--) {

                    int y = (int) ((0.5 * HEIGHT) + (metric_height - value));

                    histogram.setRGB(frame, y, green.getRGB());

                }
                continue;
            } else {
                for (int metric_height = 0; metric_height < value; metric_height++) {

                    int y = (int) ((0.5 * HEIGHT) - (value - metric_height));

                    histogram.setRGB(frame, y, green.getRGB());
                }
            }
        }

        // Draw the line y = 25 grey
        int y = (int) (0.5 * HEIGHT);
        for (int x = 0; x < histogram.getWidth() - 1; x++) {
            histogram.setRGB(x, y, Color.GRAY.getRGB());
        }

        saveHistogram("SilhouetteHistogram", histogram);

        return histogram;
    }

    /**
     * Function saves the image.
     * 
     * @param img BufferedImage object.
     * @throws IOException
     */
    private static void saveHistogram(String histogramName, BufferedImage img) throws IOException {

        // Save image
        String userdir = System.getProperty("user.home");
        File imgfolder = new File(userdir + "/motionrugs/histograms");
        if (!imgfolder.exists()) {
            imgfolder.mkdir();
        }

        File outputfile = new File(
                imgfolder + "/" +
                        histogramName + ".png");
        ImageIO.write(img, "png", outputfile);

    }

}
