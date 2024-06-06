package dbvis.visualsummaries.grouprugs.metrics;

import java.util.Arrays;

import dbvis.visualsummaries.data.DataPoint;
import javafx.scene.chart.PieChart.Data;

public class Pearsons {

    public static void main(String[] args) {
        // Example 2D data points
        double[][] points2D = {
                { 1.0, 2.0 },
                { 4.0, 6.0 },
                { 7.0, 8.0 },
                { 2.0, 3.0 }
        };

        // Example 1D representation
        double[] points1D = { 1.5, 5.0, 7.5, 2.5 };

        double[][] distanceMatrix2D = computeDistanceMatrix2D(points2D);
        double[][] distanceMatrix1D = computeDistanceMatrix1D(points1D);

        double[] distances2D = flattenDistanceMatrix(distanceMatrix2D);
        double[] distances1D = flattenDistanceMatrix(distanceMatrix1D);

        double correlation = pearsonCorrelation(distances2D, distances1D);

        System.out.println("Pearson Correlation Coefficient: " + correlation);
    }

    // Compute pairwise Euclidean distances for 2D points
    public static double[][] computeDistanceMatrix2D(double[][] points) {
        int n = points.length;
        double[][] distances = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double distance = Math
                        .sqrt(Math.pow(points[i][0] - points[j][0], 2) + Math.pow(points[i][1] - points[j][1], 2));
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }
        return distances;
    }

    // Compute pairwise absolute differences for 1D points
    public static double[][] computeDistanceMatrix1D(double[] points) {
        int n = points.length;
        double[][] distances = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double distance = Math.abs(points[i] - points[j]);
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }
        return distances;
    }

    // Flatten the upper triangular part of the distance matrix into a 1D array
    public static double[] flattenDistanceMatrix(double[][] distanceMatrix) {
        int n = distanceMatrix.length;
        double[] distances = new double[(n * (n - 1)) / 2];
        int k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                distances[k++] = distanceMatrix[i][j];
            }
        }
        return distances;
    }

    // Compute Pearson correlation coefficient between two arrays
    public static double pearsonCorrelation(double[] x, double[] y) {
        if (x.length != y.length)
            throw new IllegalArgumentException("Arrays must have the same length");
        int n = x.length;

        double meanX = Arrays.stream(x).average().orElse(0);
        double meanY = Arrays.stream(y).average().orElse(0);

        double sumXY = 0, sumX2 = 0, sumY2 = 0;
        for (int i = 0; i < n; i++) {
            double deltaX = x[i] - meanX;
            double deltaY = y[i] - meanY;
            sumXY += deltaX * deltaY;
            sumX2 += deltaX * deltaX;
            sumY2 += deltaY * deltaY;
        }

        return sumXY / Math.sqrt(sumX2 * sumY2);
    }

    public static double printScore(
            double[][] points2D,
            double[] points1D) {

        double[][] distanceMatrix2D = computeDistanceMatrix2D(points2D);
        double[][] distanceMatrix1D = computeDistanceMatrix1D(points1D);

        double[] distances2D = flattenDistanceMatrix(distanceMatrix2D);
        double[] distances1D = flattenDistanceMatrix(distanceMatrix1D);

        double correlation = pearsonCorrelation(distances2D, distances1D);

        System.out.println("Pearson Correlation Coefficient: " + correlation);

        return correlation;
    }

    public static double[] computeScores(Integer[][] etp, DataPoint[][] data) {

        double[] scores = new double[etp.length];

        for (int frame = 0; frame < etp.length; frame++) {

            double[][] points2D = new double[etp[frame].length][2];
            double[] points1D = new double[etp[frame].length];

            for (int i = 0; i < etp[frame].length; i++) {
                points2D[i][0] = data[frame][i].getX();
                points2D[i][1] = data[frame][i].getY();
                points1D[i] = etp[frame][i];
            }

            double score = printScore(points2D, points1D);

            scores[frame] = score;

        }

        return scores;
    }

    public static void save(double[] scores, String datasetName, String stratid,
            String imageStrat, double epsilon) {
        // Save the score to a file

        String filename = "plots/pearsons/" + datasetName + "_" + stratid + "_" + imageStrat + "_" + epsilon
                + ".txt";

        try {

            // If file does not exist, create it
            java.io.File file = new java.io.File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            java.io.PrintWriter output = new java.io.PrintWriter(file);

            output.println("Pearson Correlation Coefficient:");

            for (int i = 0; i < scores.length; i++) {
                output.println(scores[i]);
            }

            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}