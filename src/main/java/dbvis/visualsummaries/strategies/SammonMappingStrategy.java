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
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import dbvis.visualsummaries.data.DataPoint;

/**
 *
 * @author jwulms
 */
public class SammonMappingStrategy implements Strategy {

    private boolean stable = false;
    private final int maxIterations = 1000;
    private double gamma = 0.5;
    private final double precision = 0.00001;
    private String name = "Sammon mapping";

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

    }

    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {
        DataPoint[][] result = new DataPoint[unsorted.length][unsorted[0].length];

        ArrayList<Projection1D> prevProjections = new ArrayList<>();

        // code to extract 1D values
        String userdir = System.getProperty("user.home");
        File orderingsfolder = new File(userdir + "/motionrugs/projections");
        if (!orderingsfolder.exists()) {
            orderingsfolder.mkdir();
        }

        File orderingfile;
        if (stable) {
            orderingfile = new File(orderingsfolder + "/" + dsName + "_Stable sammon mapping_1D.csv");
        } else {
            orderingfile = new File(orderingsfolder + "/" + dsName + "_Sammon mapping_1D.csv");
        }

        try {
            // writer for 1D values
            FileWriter writer = new FileWriter(orderingfile);
            writer.write("projections sorted on id, one frame per line\n");

            // find order per frame
            for (int x = 0; x < unsorted.length; x++) {

                DataPoint[] frame = unsorted[x];
                // arraylist to save remaining clusters in
                ArrayList<Projection1D> projections = new ArrayList<>();
                // matrix containing distances in original space
                ArrayList<ArrayList<Double>> distances = new ArrayList<>();

                // initialize projections for this frame
                if (stable && prevProjections.size() > 0) {
                    // all but first frame use previous projections as starting point (for
                    // stability)
                    projections = prevProjections;
                } else {
                    for (int y = 0; y < frame.length; y++) {
                        // first frame sorts fishes on identifier
                        projections.add(new Projection1D(y, frame[y].getId()));
                    }
                }

                double sumOfDistances = 0.0;
                // calculate distances in original space
                for (int y = 0; y < frame.length; y++) {
                    distances.add(new ArrayList());

                    // store distances in "bottom" triangle of matrix (index 0-y for y-th element of
                    // frame)
                    for (int i = 0; i < y; i++) {
                        double dist = getDistance(frame[i], frame[y]);
                        distances.get(y).add(dist);
                        sumOfDistances += dist;
                    }
                    // add diagonal value in adjacency matrix
                    distances.get(y).add(0.0);
                }

                gamma = 0.5;

                for (int iteration = 0; iteration < maxIterations; iteration++) {

                    // store partial deriviatives to update all projections later
                    double[] partialDerivatives = new double[projections.size()];

                    // calculate the partial derivative for each dimension of the stress function -
                    // so for all projections of the points
                    for (int node = 0; node < frame.length; node++) {

                        // we are going to calculate the partial derivative of the stress function for
                        // this node
                        // add variable part depending on current projection of two nodes
                        for (int toNode = 0; toNode < unsorted[x].length; toNode++) {
                            if (node == toNode) {
                                continue;
                            }

                            double nodeProj = projections.get(node).getProjection();
                            double toNodeProj = projections.get(toNode).getProjection();
                            double dist;
                            if (node < toNode) {
                                dist = distances.get(toNode).get(node);
                            } else { // toNode < node
                                dist = distances.get(node).get(toNode);
                            }

                            partialDerivatives[node] += 2 * (dist - Math.abs(nodeProj - toNodeProj))
                                    * (toNodeProj - nodeProj) / (dist * Math.abs(nodeProj - toNodeProj));
                        }

                        // multiply partial derivative by constant part of formula
                        partialDerivatives[node] *= 1 / sumOfDistances;
                    }

                    // check how close we are compared to last iteration and update {@code gamma}
                    // accordingly by either halving
                    while (computeStress(frame, projections, distances, sumOfDistances) <= computeStress(frame,
                            projections, distances, sumOfDistances, gamma, partialDerivatives)) {
                        // System.out.println("Gamma halved to " + gamma);
                        gamma = gamma / 2;

                        if (gamma < precision) {
                            break;
                        }
                    }

                    // calculate the new projection for each node using gradient descent to minimize
                    // the stress function
                    for (int i = 0; i < projections.size(); i++) {
                        projections.get(i).addToProjection(-1 * gamma * partialDerivatives[i]);
                    }

                    if (iteration == maxIterations - 1 || gamma < precision) {
                        // System.out.println("Stopped at iteration " + iteration);
                        break;
                    }

                    // gamma can also increase if we are just going down towards minimum
                    gamma = 2 * gamma;
                }

                // save the current projections as a starting point for the next iteration
                // (sTaBiLiTyYyYyY)
                prevProjections = deepCopy(projections);

                // sort the projections according to their projection to get the 1D ordering
                Collections.sort(projections);

                // sort the result set after the cluster order
                for (int y = 0; y < unsorted[x].length; y++) {
                    result[x][y] = unsorted[x][projections.get(y).getID()];
                }

                if (x % 100 == 0) {
                    System.out.println("Frame " + x + " done!");
                }

                // writing the 1D values
                StringBuilder sb = new StringBuilder();
                double[] values = new double[projections.size()];

                // create array that stores ranking per id, instead of ids per rank
                for (int y = 0; y < projections.size(); y++) {
                    values[projections.get(y).getID()] = projections.get(y).getProjection();
                }

                // output ranking in order of ids
                sb.append(values[0]);
                for (int y = 1; y < values.length; y++) {
                    sb.append(",");
                    sb.append(values[y]);
                }

                sb.append("\n");
                writer.write(sb.toString());

            }
            writer.flush();
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(SammonMappingStrategy.class.getName()).log(Level.SEVERE, null, ex);
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

        File orderingfile = new File(orderingsfolder + "/" + dsName + "_Stable sammon mapping_ordering.csv");

        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(orderingfile));
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

    public void setStability(boolean stable) {
        if (stable) {
            setName("Stable sammon mapping");
        } else {
            setName("Sammon mapping");
        }
        this.stable = stable;
    }

    private ArrayList<Projection1D> deepCopy(ArrayList<Projection1D> projections) {
        ArrayList<Projection1D> result = new ArrayList();

        for (Projection1D element : projections) {
            result.add(element);
        }

        return result;
    }

    private double getDistance(DataPoint from, DataPoint to) {
        return Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getY() - from.getY(), 2));
    }

    private double computeStress(DataPoint[] frame, ArrayList<Projection1D> projections,
            ArrayList<ArrayList<Double>> distances, double sumOfDistances) {
        double result = 0;

        for (int i = 0; i < frame.length - 1; i++) {
            for (int j = i + 1; j < frame.length; j++) {
                double nodeProj = projections.get(i).getProjection();
                double toNodeProj = projections.get(j).getProjection();
                double dist = distances.get(j).get(i);

                result += Math.pow(dist - Math.abs(nodeProj - toNodeProj), 2) / dist;
            }
        }

        result *= 1 / sumOfDistances;

        // System.out.println("Stress now is " + result);
        return result;
    }

    private double computeStress(DataPoint[] frame, ArrayList<Projection1D> projections,
            ArrayList<ArrayList<Double>> distances, double sumOfDistances, double epsilon,
            double[] partialDerivatives) {
        double result = 0;

        for (int i = 0; i < frame.length - 1; i++) {
            for (int j = i + 1; j < frame.length; j++) {
                double nodeProj = projections.get(i).getProjection() - (epsilon * partialDerivatives[i]);
                double toNodeProj = projections.get(j).getProjection() - (epsilon * partialDerivatives[j]);
                double dist = distances.get(j).get(i);

                result += Math.pow(dist - Math.abs(nodeProj - toNodeProj), 2) / dist;
            }
        }

        result *= 1 / sumOfDistances;

        // System.out.println("Stress with gamma " + epsilon + " is " + result);
        return result;
    }
}
