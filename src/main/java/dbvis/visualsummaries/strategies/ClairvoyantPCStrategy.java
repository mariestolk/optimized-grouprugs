/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

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
public class ClairvoyantPCStrategy implements Strategy {

    private double[] directions = null;
    private double[][] eigenvalues = null;
    private double thresholdConstant = 1.0 / 1.9;
    private boolean chasing = false;
    private double chaseSpeed = Double.POSITIVE_INFINITY;

    /**
     * Initializes this strategy to always linearly interpolate when first PC is
     * not pronounced enough.
     *
     * @param thresholdConstant determines whether a PC is pronounced enough
     */
    public ClairvoyantPCStrategy(double thresholdConstant) {
        this.thresholdConstant = thresholdConstant;
    }

    /**
     * Initializes this strategy to chase by chasing angle by at most {@code k}
     * every time step, when first PC is not pronounced enough.
     *
     * @param thresholdConstant determines whether a PC is pronounced enough
     * @param k maximum chase speed of angle
     */
    public ClairvoyantPCStrategy(double thresholdConstant, double speed) {
        this.thresholdConstant = thresholdConstant;
        this.chaseSpeed = speed;
    }

    @Override
    public String getName() {
        return "Clairvoyant PC chasing";
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

        //code to extract projections
        String userdir = System.getProperty("user.home");
        File orderingsfolder = new File(userdir + "/motionrugs/ordering");
        if (!orderingsfolder.exists()) {
            orderingsfolder.mkdir();
        }

        File orderingfile = new File(orderingsfolder + "/lastuseddataset_Clairvoyant PC chasing " + thresholdConstant + "_1D.csv");
        try {
            //writer for projections
            FileWriter writer = new FileWriter(orderingfile);

            writer.write("projections sorted on id, one frame per line\n");

            makeFirstFrame(unsorted, result, writer);
            previous = new Vector(Math.cos(directions[0]), Math.sin(directions[0]));

            int x = 1;
            double deltaDir = 0.0;
            //find order per frame
            while (x < unsorted.length) {

                //idx is an array of the point indexes 
                Integer[] idx = new Integer[unsorted[x].length];
                //find line along principal component, with "origin" at mean of point set
                Line pc = getPrincipalComponent(unsorted[x]);

                // calculate the direction of the first principal component as an angle for this time step
                directions[x] = Math.atan2(pc.getDirection().getY(), pc.getDirection().getX());

                // find first one that is pronounced
                int nextPronounced = findFirstPronouncedIndex(x, unsorted);
                // find principal component when pronounced again
                Line newPC = getPrincipalComponent(unsorted[nextPronounced]);
                // check difference in frames (previous frame is last processed one)
                int deltaT = nextPronounced - (x - 1);
                // check difference in angle
                deltaDir += accumulateAngle(x, nextPronounced, unsorted);

                // order upcoming frames depending on different in frames and angle
                if (deltaT == 1) { // next frame has pronounced PC
                    if (!chasing) { // we are not chasing
                        // ensure direction does not 180 flip
                        pc.adjustForFlip(previous);
                        previous = pc.getDirection();
                        //sort the result set after the projection order 
                        sortIndicesAlongPC(x, unsorted, pc, result, writer);
                        deltaDir = 0.0;
                    } else { // chase an optimal solution
                        if (Math.abs(deltaDir) > chaseSpeed) {
//                        System.out.println("chasing one frame");
                            // find new angle by rotating at maximum speed
                            directions[x] = directions[x - 1] - (Math.signum(deltaDir) * chaseSpeed);
                            deltaDir -= Math.signum(deltaDir) * chaseSpeed;
//                        System.out.println("Angle difference left: " + deltaDir);
                        } else {
//                        System.out.println("interpolate one frame");
                            // find new angle by interpolating linearly
                            directions[x] = directions[x - 1] - deltaDir;
                            chasing = false;
                            deltaDir = 0.0;
                        }
                        newPC = new Line(newPC.getPoint(), new Vector(Math.cos(directions[x]), Math.sin(directions[x])));
                        // ensure direction does not 180 flip
                        newPC.adjustForFlip(previous);
                        previous = newPC.getDirection();
                        sortIndicesAlongPC(x, unsorted, newPC, result, writer);
                    }
                    // go to next frame
                    x++;
                } else if (Math.abs(deltaDir / deltaT) > chaseSpeed) { // next pronounced requires angle to change too much, so start chasing
//                System.out.println("chasing to next");
                    for (int frame = x; frame < nextPronounced + 1; frame++) {
                        // find new angle by rotating at maximum speed
                        directions[frame] = directions[frame - 1] - (Math.signum(deltaDir / deltaT) * chaseSpeed);
                        // update how much we still need to rotate
                        deltaDir -= Math.signum(deltaDir) * chaseSpeed;
//                    System.out.println("Angle difference left: " + deltaDir);
                        // find new line by taking first PC and overwriting angle
                        newPC = getPrincipalComponent(unsorted[frame]);
                        newPC = new Line(newPC.getPoint(), new Vector(Math.cos(directions[frame]), Math.sin(directions[frame])));
                        // ensure direction does not 180 flip
                        newPC.adjustForFlip(previous);
                        previous = newPC.getDirection();
                        //sort the result set by projecting to PC 
                        sortIndicesAlongPC(frame, unsorted, newPC, result, writer);
                    }
                    // we start chasing after next pronounced
                    chasing = true;
                    x = nextPronounced + 1;
                } else { // next pronounced requires moderate angle change, so we interpolate up to next pronounced
//                System.out.println("Change in angle per time step: " + (deltaDir / deltaT));
                    for (int frame = x; frame < nextPronounced + 1; frame++) {
                        // find new angle by interpolating linearly
                        directions[frame] = directions[frame - 1] - (deltaDir / deltaT);
                        // find new line by taking first PC and overwriting angle
                        newPC = getPrincipalComponent(unsorted[frame]);
                        newPC = new Line(newPC.getPoint(), new Vector(Math.cos(directions[frame]), Math.sin(directions[frame])));
                        // ensure direction does not 180 flip
                        newPC.adjustForFlip(previous);
                        previous = newPC.getDirection();
                        //sort the result set by projecting to PC 
                        sortIndicesAlongPC(frame, unsorted, newPC, result, writer);
                    }
                    // go to frame after interpolation
                    chasing = false;
                    deltaDir = 0.0;
                    x = nextPronounced + 1;
                }
            }

            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ClairvoyantPCStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    /**
     * Returns a line along the first principal component of the dataset during
     * a single frame
     *
     * @param unsorted dataset at a single frame
     * @return line along the first principal component of the dataset
     */
    private Line getPrincipalComponent(DataPoint[] unsorted) {
        //prep data for singular value decomposition
        double[][] data = new double[unsorted.length][2];
        //also find mean of each coordinate
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

        //get the singular value decomposition with diagonal matrix Sigma of singular values
        //and matrix V containing a right singular vector (eigenvector) in each column
        Array2DRowRealMatrix datamatrix = new Array2DRowRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecomposition(datamatrix);

        //get the first principal component
        double[] pc = new double[2];
        pc = svd.getV().getColumn(0);

        Vector point = new Vector(mean[0], mean[1]);
        Vector direction = new Vector(pc[0], pc[1]);
        return new Line(point, direction);
    }

    private double[] getEigenvalues(DataPoint[] unsorted) {
        double[] eigenvalues = new double[2];

        //prep data for singular value decomposition
        double[][] data = new double[unsorted.length][2];
        //also find mean of each coordinate
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

        //get the singular value decomposition with diagonal matrix Sigma of singular values
        //and matrix V containing a right singular vector (eigenvector) in each column
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

    /**
     * Set the threshold constant which determines whether a PC is pronounced
     *
     * @param constant threshold constant between 0 and 1
     */
    public void setThresHoldConstant(double constant) {
        this.thresholdConstant = constant;
    }

    /**
     * Order first frame along first PC and store in {@code result}
     *
     * @param unsorted data set
     * @param result result array
     */
    private void makeFirstFrame(DataPoint[][] unsorted, DataPoint[][] result) {
        //idx is an array of the point indexes 
        Integer[] idx = new Integer[unsorted[0].length];
        //array to save where points are projected to principal component
        double pcScalars[] = new double[unsorted[0].length];
        //find line along principal component, with "origin" at mean of point set
        Line pc = getPrincipalComponent(unsorted[0]);

        //project points to principal component and store projection variable
        for (int y = 0; y < unsorted[0].length; y++) {
            idx[y] = y;
            Vector datapoint = new Vector(unsorted[0][y].getX(), unsorted[0][y].getY());

            // the projection scalars all assume same origin of line
            // we can use them to find order of projected points
            pcScalars[y] = pc.projectionScalar(datapoint);
        }

        // calculate the direction of the first principal component as an angle
        directions[0] = Math.atan2(pc.getDirection().getY(), pc.getDirection().getX());
        // calculate the eigenvalues for this time step
        eigenvalues[0] = getEigenvalues(unsorted[0]);

        //sort the index array with comparing the projection scalars 
        Arrays.sort(idx, new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return Double.compare(pcScalars[o1], pcScalars[o2]);

            }
        });

        //sort the result set after the projection order 
        for (int y = 0; y < unsorted[0].length; y++) {
            result[0][y] = unsorted[0][idx[y]];
        }
    }

    /**
     * Order first frame along first PC and store in {@code result}
     *
     * @param unsorted data set
     * @param result result array
     */
    private void makeFirstFrame(DataPoint[][] unsorted, DataPoint[][] result, FileWriter writer) throws IOException {
        //idx is an array of the point indexes 
        Integer[] idx = new Integer[unsorted[0].length];
        //array to save where points are projected to principal component
        double pcScalars[] = new double[unsorted[0].length];
        //find line along principal component, with "origin" at mean of point set
        Line pc = getPrincipalComponent(unsorted[0]);

        //stringbuilder for writing projections
        StringBuilder sb = new StringBuilder();

        //project points to principal component and store projection variable
        for (int y = 0; y < unsorted[0].length; y++) {
            idx[y] = y;
            Vector datapoint = new Vector(unsorted[0][y].getX(), unsorted[0][y].getY());

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
        directions[0] = Math.atan2(pc.getDirection().getY(), pc.getDirection().getX());
        // calculate the eigenvalues for this time step
        eigenvalues[0] = getEigenvalues(unsorted[0]);

        //sort the index array with comparing the projection scalars 
        Arrays.sort(idx, new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return Double.compare(pcScalars[o1], pcScalars[o2]);

            }
        });

        //sort the result set after the projection order 
        for (int y = 0; y < unsorted[0].length; y++) {
            result[0][y] = unsorted[0][idx[y]];
        }
    }

    /**
     * Find index of first frame where first PC is pronounced enough.
     *
     * @param x index to start searching from
     * @param unsorted dataset to search through
     * @return index of first frame with pronounced first PC
     */
    private int findFirstPronouncedIndex(int x, DataPoint[][] unsorted) {
        eigenvalues[x] = getEigenvalues(unsorted[x]);
        double eigenratio = eigenvalues[x][1] / eigenvalues[x][0];
        double threshold = (eigenratio - thresholdConstant);

        int firstPronounced = x;

        while (threshold > 0 && firstPronounced < unsorted.length - 1) {
            firstPronounced++;

            // find eigenvalues for this frame
            eigenvalues[firstPronounced] = getEigenvalues(unsorted[firstPronounced]);

            eigenratio = eigenvalues[firstPronounced][1] / eigenvalues[firstPronounced][0];
            threshold = (eigenratio - thresholdConstant);
        }

        return firstPronounced;
    }

    /**
     * Return array with indices of a single frame sorted by projection to PC
     * line.
     *
     * @param x index of frame to sort
     * @param unsorted data set for which a frame will be sorted
     * @param pc PC line to project to
     * @param result result array
     */
    private void sortIndicesAlongPC(int x, DataPoint[][] unsorted, Line pc, DataPoint[][] result) {
        //idx is an array of the point indexes 
        Integer[] idx = new Integer[unsorted[0].length];
        //array to save where points are projected to principal component
        double pcScalars[] = new double[unsorted[x].length];

        //project points to principal component and store projection variable
        for (int y = 0; y < unsorted[x].length; y++) {
            idx[y] = y;
            Vector datapoint = new Vector(unsorted[x][y].getX(), unsorted[x][y].getY());

            // the projection scalars all assume same origin of line
            // we can use them to find order of projected points
            pcScalars[y] = pc.projectionScalar(datapoint);
        }

        //sort the index array with comparing the projection scalars 
        Arrays.sort(idx, new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return Double.compare(pcScalars[o1], pcScalars[o2]);

            }
        });

        for (int y = 0; y < unsorted[x].length; y++) {
            result[x][y] = unsorted[x][idx[y]];
        }
    }

    /**
     * Return array with indices of a single frame sorted by projection to PC
     * line.
     *
     * @param x index of frame to sort
     * @param unsorted data set for which a frame will be sorted
     * @param pc PC line to project to
     * @param result result array
     */
    private void sortIndicesAlongPC(int x, DataPoint[][] unsorted, Line pc, DataPoint[][] result, FileWriter writer) throws IOException {
        //idx is an array of the point indexes 
        Integer[] idx = new Integer[unsorted[0].length];
        //array to save where points are projected to principal component
        double pcScalars[] = new double[unsorted[x].length];

        //stringbuilder for writing projections
        StringBuilder sb = new StringBuilder();

        //project points to principal component and store projection variable
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

        //sort the index array with comparing the projection scalars 
        Arrays.sort(idx, new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return Double.compare(pcScalars[o1], pcScalars[o2]);

            }
        });

        for (int y = 0; y < unsorted[x].length; y++) {
            result[x][y] = unsorted[x][idx[y]];
        }
    }

    /**
     * Returns difference in angle in the range of -<i>pi</i> to <i>pi</i>.
     *
     * @param oldAngle
     * @param newAngle
     * @return oldAngle - newAngle (adjusted to be in the range of -<i>pi</i> to
     * <i>pi</i>)
     */
    private double angleDiff(double oldAngle, double newAngle) {
        double diff = oldAngle - newAngle;
//        if (diff < -2 * Math.PI) {
//            diff += 2 * Math.PI;
//        } else if (diff > 2 * Math.PI) {
//            diff -= 2 * Math.PI;
//        }

        return diff;
    }

    /**
     * Finds the difference in first PC angle between frame {@code x} and frame
     * {@code nextPronounced} in {@code unsorted}.
     *
     * @param x first frame index
     * @param nextPronounced final frame index
     * @param unsorted data set in which we find difference in angle
     * @return difference in first PC angle between frame {@code x} and frame
     * {@code nextPronounced}
     */
    private double accumulateAngle(int x, int nextPronounced, DataPoint[][] unsorted) {
        double deltaAngle = 0.0;
        double newAngle, oldAngle, diff;
        // find angle of pc in frame before accumulating up to nextPronounced
        Line pc = getPrincipalComponent(unsorted[x - 1]);
        Vector previous = pc.getDirection();
        oldAngle = Math.atan2(pc.getDirection().getY(), pc.getDirection().getX());
        for (int frame = x; frame < nextPronounced + 1; frame++) {
            // find angle of pc in next frame
            pc = getPrincipalComponent(unsorted[frame]);
            pc.adjustForFlip(previous);
            newAngle = Math.atan2(pc.getDirection().getY(), pc.getDirection().getX());
            // ensure we find the difference in angle correctly
            diff = oldAngle - newAngle;
            if (diff > Math.PI * 1.9) { // close to 360 degrees means jump from Pi to -Pi, 1.9 is enough for our data sets, make lower if {@code pc} changes quicker
                diff -= 2 * Math.PI;
            } else if (diff < -Math.PI * 1.9) { // close to - 360 degrees means jump from -Pi to Pi, 1.9 is enough for our data sets, make lower if {@code pc} changes quicker
                diff += 2 * Math.PI;
            }
            // accumulate difference in angle
            deltaAngle += diff;
            // set up next iteration
            oldAngle = newAngle;
            previous = pc.getDirection();
        }
        return deltaAngle;
    }

}
