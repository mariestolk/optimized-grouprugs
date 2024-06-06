/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import dbvis.visualsummaries.data.DataPoint;

/**
 * First principal component strategy
 *
 * @author Jules Wulms, TU Eindhoven <j.j.h.m.wulms@tue.nl>
 */
public class PrincipalComponentStrategy implements Strategy {

    private double[] directions = null;
    private double[][] eigenvalues = null;

    @Override
    public String getName() {
        return "First principal component";
    }

    /**
     * Returns dataset ordered along the first principal component (at every
     * point in time)
     *
     * @param unsorted dataset
     * @return sorted dataset
     */
    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {
        DataPoint[][] result = new DataPoint[unsorted.length][unsorted[0].length];

        Vector previous = null;
        directions = new double[unsorted.length];
        eigenvalues = new double[unsorted.length][];

        // code to extract projections
        String userdir = System.getProperty("user.home");
        File orderingsfolder = new File(userdir + "/motionrugs/projections");
        if (!orderingsfolder.exists()) {
            orderingsfolder.mkdir();
        }

        File orderingfile = new File(orderingsfolder + "/" + dsName + "_First principal component_1D.csv");
        try {
            // writer for projections
            FileWriter writer = new FileWriter(orderingfile);

            writer.write("projections sorted on id, one frame per line\n");

            // find order per frame
            for (int x = 0; x < unsorted.length; x++) {

                // idx is an array of the point indexes
                Integer[] idx = new Integer[unsorted[x].length];
                // array to save where points are projected to principal component
                double pcScalars[] = new double[unsorted[x].length];
                // find line along principal component, with "origin" at mean of point set
                Line pc = getPrincipalComponent(unsorted[x]);

                if (previous != null) {
                    pc.adjustForFlip(previous);
                }
                previous = pc.getDirection();

                // stringbuilder for writing projections
                StringBuilder sb = new StringBuilder();

                // project points to principal component and store projection variable
                for (int y = 0; y < unsorted[x].length; y++) {
                    idx[y] = y;
                    Vector datapoint = new Vector(unsorted[x][y].getX(), unsorted[x][y].getY());

                    // the projection scalars all assume same origin of line
                    // we can use them to find order of projected points
                    pcScalars[y] = pc.projectionScalar(datapoint);

                    sb.append(pcScalars[y]);
                    sb.append(",");
                }

                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n");
                writer.write(sb.toString());

                // calculate the direction of the first principal component as an angle
                directions[x] = Math.atan2(pc.getDirection().getY(), pc.getDirection().getX());
                // calculate the eigenvalues for this time step
                eigenvalues[x] = getEigenvalues(unsorted[x]);

                // sort the index array with comparing the projection scalars
                Arrays.sort(idx, new Comparator<Integer>() {
                    @Override
                    public int compare(final Integer o1, final Integer o2) {
                        return Double.compare(pcScalars[o1], pcScalars[o2]);

                    }
                });

                // sort the result set after the projection order
                for (int y = 0; y < unsorted[x].length; y++) {
                    result[x][y] = unsorted[x][idx[y]];
                }
            }

            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(PrincipalComponentStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }

        writeOrderedToFile(result, dsName);

        return result;
    }

    private static void writeOrderedToFile(DataPoint[][] ordered, String dsName) {

        // code to extract 1D values
        String userdir = System.getProperty("user.home");
        File orderingsfolder = new File(userdir + "/motionrugs/ordering");
        if (!orderingsfolder.exists()) {
            orderingsfolder.mkdir();

        }

        File orderingfile = new File(orderingsfolder + "/" + dsName + "_First principal component_ordering.csv");

        try {

            FileWriter writer = new FileWriter(orderingfile);
            writer.write("frame,id\n");
            for (int i = 0; i < ordered.length; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(ordered[i][0].getId());
                for (int j = 1; j < ordered[i].length; j++) {
                    sb.append(",");
                    sb.append(ordered[i][j].getId());
                }
                sb.append("\n");
                writer.write(sb.toString());
            }

            writer.flush();
            writer.close();

        } catch (Exception ex) {
            Logger.getLogger(UMAPStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Returns a line along the first principal component of the dataset during
     * a single frame
     *
     * @param unsorted dataset at a single frame
     * @return line along the first principal component of the dataset
     */
    private Line getPrincipalComponent(DataPoint[] unsorted) {
        // prep data for singular value decomposition
        double[][] data = new double[unsorted.length][2];
        // also find mean of each coordinate
        double[] mean = new double[2];

        for (int i = 0; i < unsorted.length; i++) {
            data[i][0] = unsorted[i].getX();
            data[i][1] = unsorted[i].getY();

            mean[0] += unsorted[i].getX();
            mean[1] += unsorted[i].getY();
        }
        mean[0] = mean[0] / unsorted.length;
        mean[1] = mean[1] / unsorted.length;

        for (int i = 0; i < unsorted.length; i++) {
            data[i][0] -= mean[0];
            data[i][1] -= mean[1];
        }

        // get the singular value decomposition with diagonal matrix Sigma of singular
        // values
        // and matrix V containing a right singular vector (eigenvector) in each column
        Array2DRowRealMatrix datamatrix = new Array2DRowRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecomposition(datamatrix);

        // get the first principal component
        double[] pc = new double[2];
        pc = svd.getV().getColumn(0);

        Vector point = new Vector(mean[0], mean[1]);
        Vector direction = new Vector(pc[0], pc[1]);
        return new Line(point, direction);
    }

    private double[] getEigenvalues(DataPoint[] unsorted) {
        double[] eigenvalues = new double[2];

        // prep data for singular value decomposition
        double[][] data = new double[unsorted.length][2];
        // also find mean of each coordinate
        double[] mean = new double[2];

        for (int i = 0; i < unsorted.length; i++) {
            data[i][0] = unsorted[i].getX();
            data[i][1] = unsorted[i].getY();

            mean[0] += unsorted[i].getX();
            mean[1] += unsorted[i].getY();
        }
        mean[0] = mean[0] / unsorted.length;
        mean[1] = mean[1] / unsorted.length;

        for (int i = 0; i < unsorted.length; i++) {
            data[i][0] -= mean[0];
            data[i][1] -= mean[1];
        }

        // get the singular value decomposition with diagonal matrix Sigma of singular
        // values
        // and matrix V containing a right singular vector (eigenvector) in each column
        Array2DRowRealMatrix datamatrix = new Array2DRowRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecomposition(datamatrix);
        eigenvalues = svd.getSingularValues();

        return eigenvalues;
    }

    public double[] getDirections() {
        return directions;
    }

    public double[][] getEigenvalues() {
        return eigenvalues;
    }

}

// ATTEMPT AT FINDING FIRST PC BY HAND
// //normalize the data around mean
// double[][] normalized = new double[unsorted.length][2];
// for(int i = 0; i < unsorted.length; i++) {
// normalized[i][0] = unsorted[i].getX() - mean[0];
// normalized[i][1] = unsorted[i].getY() - mean[1];
// }
//
// //calculate covariance matrix
// double[][] covariance = new double[unsorted.length][2];
// for(int i = 0; i < unsorted.length; i++) {
// for(int j = 0; j < unsorted.length; j++) {
// for(int coordinate = 0; coordinate < 2; coordinate++) {
// covariance[i][j] += normalized[i][coordinate] * normalized[j][coordinate];
// }
// // this is unbiased estimate given that we calculated mean from the data
// (sample mean)
// // if we think this is the true mean then just devide by unsorted.length
// covariance[i][j] = covariance[i][j] / (unsorted.length-1);
// }
// }
