package dbvis.visualsummaries.grouprugs.metrics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.Component;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.MapperUtils;
import dbvis.visualsummaries.strategies.Strategy;

public class MetricsUtil {

    /**
     * Function returns the average metric value over all frames.
     * 
     * @param metric The metric scores for each frame.
     * @return The average metric score.
     */
    public static double getAverageScore(double[] metric) {

        double sum = 0;

        for (int frame = 0; frame < metric.length; frame++) {
            sum += metric[frame];
        }

        return sum / metric.length;
    }

    /**
     * Function returns the standard deviation of the metric values over all frames.
     * 
     * @param metric The metric scores for each frame.
     * @return The standard deviation of the metric scores.
     */
    public static double getStandardDeviation(double[] metric) {

        double average = getAverageScore(metric);
        double sum = 0;

        for (int frame = 0; frame < metric.length; frame++) {
            sum += Math.pow(metric[frame] - average, 2);
        }

        return Math.sqrt(sum / metric.length);
    }

    /**
     * Function returns the maximum metric value over all frames.
     * 
     * @param metric The metric scores for each frame.
     * @return The maximum metric score.
     */
    public static double getMaxScore(double[] metric) {

        double max = metric[0];

        for (int frame = 1; frame < metric.length; frame++) {
            if (metric[frame] > max) {
                max = metric[frame];
            }
        }

        return max;
    }

    /**
     * Function returns the minimum metric value over all frames.
     * 
     * @param metric The metric scores for each frame.
     * @return The minimum metric score.
     */
    public static double getMinScore(double[] metric) {

        double min = metric[0];

        for (int frame = 1; frame < metric.length; frame++) {
            if (metric[frame] < min) {
                min = metric[frame];
            }
        }

        return min;
    }

    /**
     * Function to get the metrics for a specific configuration. If the metrics were
     * already computed and saved to a file, they are read from the file. Otherwise
     * the metrics are computed and saved to a file. Used for experimental setup.
     * 
     * @param selectedDataset       name of the dataset.
     * @param selectedStrategy      id of the dimension reduction strategy used.
     * @param selectedImageStrategy id of the image strategy used.
     * @param epsilon               epsilon value used.
     * @param originalComponents    list of components.
     * @param etpMap                the EntityToPosition map.
     * @param awtImage              the image of the group rug.
     * @return metrics HashMap containing the metrics.
     * @throws IOException if the file could not be read or written.
     */
    public static HashMap<String, double[]> getMetrics(
            String selectedDataset,
            String selectedStrategy,
            Strategy strategy,
            String selectedImageStrategy,
            double epsilon,
            List<Component> originalComponents,
            Integer[][] etpMap,
            Double[][] projections,
            boolean ML,
            BufferedImage awtImage) throws IOException {

        HashMap<String, double[]> metrics = new HashMap<String, double[]>();

        DataPoint[][] orderedDataPoints = MapperUtils.readOrderedPoint(SessionData.getInstance(), selectedDataset,
                strategy);
        // File file = new File("plots/metrics/" + selectedDataset + "_" +
        // selectedStrategy + "_"
        // + selectedImageStrategy + "_" + epsilon + ".txt");
        //
        // if (file.exists()) {
        //
        // // Read metrics from file
        // metrics = readMetrics("plots/metrics/" + selectedDataset + "_"
        // + selectedStrategy + "_" + selectedImageStrategy + "_" + epsilon + ".txt");
        // } else {

        // Compute metrics
        metrics = computeMetrics(
                originalComponents,
                etpMap,
                SessionData.getInstance().getDataset(selectedDataset).getBaseData(),
                orderedDataPoints,
                projections,
                ML,
                awtImage.getHeight());

        // Save metrics to file
        saveMetrics(metrics, selectedDataset, selectedStrategy, selectedImageStrategy, epsilon);
        // }

        return metrics;
    }

    /**
     * Function to save metrics to a file.
     * 
     * @param metrics     HashMap containing the metrics.
     * @param datasetName name of the dataset.
     * @param stratid     id of the strategy used.
     * @param imageStrat  id of the image strategy used.
     * @param epsilon     epsilon value used.
     */
    private static void saveMetrics(HashMap<String, double[]> metrics, String datasetName, String stratid,
            String imageStrat, double epsilon) {

        if (stratid == "ClairvoyantPCStrategy") {
            stratid = "PrincipalComponentStrategy";
        }

        String filename = "plots/metrics/" + datasetName + "_" + stratid + "_" + imageStrat + "_" + epsilon + ".txt";

        try {

            // If file does not exist, create it
            java.io.File file = new java.io.File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            java.io.PrintWriter output = new java.io.PrintWriter(file);

            output.println("Silhouette Score:");
            for (int i = 0; i < metrics.get("Silhouette Score:").length; i++) {
                output.println(metrics.get("Silhouette Score:")[i]);
            }

            output.println("Spatial Quality Dist:");
            for (int i = 0; i < metrics.get("Spatial Quality Dist:").length; i++) {
                output.println(metrics.get("Spatial Quality Dist:")[i]);
            }

            output.println("Spatial Quality Enc:");
            for (int i = 0; i < metrics.get("Spatial Quality Enc:").length; i++) {
                output.println(metrics.get("Spatial Quality Enc:")[i]);
            }

            output.println("Stability Dist:");
            for (int i = 0; i < metrics.get("Stability Dist:").length; i++) {
                output.println(metrics.get("Stability Dist:")[i]);
            }

            output.println("Crossings:");
            for (int i = 0; i < metrics.get("Crossings:").length; i++) {
                output.println(metrics.get("Crossings:")[i]);
            }

            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Function to read metrics from a file if they were previously saved.
     * 
     * @param filename name of the file to read metrics from.
     * @return metrics HashMap containing the metrics.
     */
    public static HashMap<String, double[]> readMetrics(String filename) {

        HashMap<String, double[]> metrics = new HashMap<String, double[]>();

        try {
            java.io.File file = new java.io.File(filename);
            java.util.Scanner input = new java.util.Scanner(file);

            String metric = "";
            ArrayList<Double> values = new ArrayList<Double>();

            while (input.hasNext()) {
                String line = input.nextLine();

                if (line.contains(":")) {
                    if (!metric.equals("")) {
                        double[] valuesArray = new double[values.size()];
                        for (int i = 0; i < values.size(); i++) {
                            valuesArray[i] = values.get(i);
                        }
                        metrics.put(metric, valuesArray);
                        values.clear();
                    }
                    metric = line;
                } else {
                    values.add(Double.parseDouble(line));
                }
            }

            double[] valuesArray = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                valuesArray[i] = values.get(i);
            }
            metrics.put(metric, valuesArray);

            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return metrics;
    }

    private static HashMap<String, double[]> computeMetrics(
            List<Component> maximalGroups,
            Integer[][] etpMap,
            DataPoint[][] baseData,
            DataPoint[][] orderedDataPoints,
            Double[][] projections,
            boolean ML,
            int height) throws IOException {

        // Compute & print metrics
        double[] silhouetteScores = SilhouetteScore
                .computeSilhouette(maximalGroups, etpMap, height);
        double[] spatial_dist_metric = SpatialQuality_QS.computeKS(baseData, etpMap);
        double[] spatial_dist_enc_metric = SpatialQuality_SS.computeSS(baseData, orderedDataPoints, projections, ML,
                etpMap);
        double[] stability_dist_metric = Stability_dist.computeKS(etpMap);
        double[] crossings = Crossings.count(etpMap);

        // Create Hashmaps for the metrics
        HashMap<String, double[]> metrics = new HashMap<String, double[]>();
        metrics.put("Silhouette Score:", silhouetteScores);
        metrics.put("Spatial Quality Dist:", spatial_dist_metric);
        metrics.put("Spatial Quality Enc:", spatial_dist_enc_metric);
        metrics.put("Stability Dist:", stability_dist_metric);
        metrics.put("Crossings:", crossings);

        return metrics;
    }

}
