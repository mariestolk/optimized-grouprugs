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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import dbvis.visualsummaries.data.DataPoint;

/**
 *
 * @author jwulms
 */
public class TSNESimpleStrategy implements Strategy {

    private boolean stable = false;
    private final int maxIterations = 6000;
    private double epsilonStart = 0.5;
    private final double precision = 0.00001;
    private final double perplexity = 40.0;
    private final double sigmaLB = 5.0;

    @Override
    public String getName() {
        return "t-SNE (simple)";
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
            orderingfile = new File(orderingsfolder + "/" + dsName + "_Stable t-SNE (simple)_1D.csv");
        } else {
            orderingfile = new File(orderingsfolder + "/" + dsName + "_t-SNE (simple)_1D.csv");
        }

        try {
            // writer for 1D values
            FileWriter writer = new FileWriter(orderingfile);
            writer.write("projections sorted on id, one frame per line\n");

            // find order per frame
            for (int x = 0; x < unsorted.length; x++) {

                DataPoint[] frame = unsorted[x];
                // matrix containing distances in original space
                ArrayList<ArrayList<Double>> distances = new ArrayList<>();
                // matrix containing conditional probabilities
                ArrayList<ArrayList<Double>> conditionalPs = new ArrayList<>();
                // matrix containing similarities
                ArrayList<ArrayList<Double>> similarities = new ArrayList<>();
                // arraylist to save projected points in
                ArrayList<Projection1D> projections = new ArrayList<>();

                computeDistances(frame, distances);

                computeConditionalPs(frame, distances, sigmaLB, perplexity, conditionalPs);

                computeSimilarities(frame, conditionalPs, similarities);

                // initialize projections for this frame
                if (stable && prevProjections.size() > 0) {
                    // all but first frame use previous projections as starting point (for
                    // stability)
                    projections = prevProjections;
                } else {
                    for (int y = 0; y < frame.length; y++) {
                        // first frame sorts fishes randomly via Gaussian around 0
                        projections.add(new Projection1D(y, initializeSolution()));
                    }
                }

                double epsilon = epsilonStart;

                for (int iteration = 0; iteration < maxIterations; iteration++) {

                    // matrix storing low-dimensional affinities
                    ArrayList<ArrayList<Double>> affinities = new ArrayList<>();
                    // matrix storing (1 + distance^2)^-1 in projection
                    ArrayList<ArrayList<Double>> projDists = new ArrayList<>();
                    // store partial deriviatives to update all projections later
                    double[] partialDerivatives = new double[projections.size()];

                    // calculate (1 + distance^2)^-1 in projection
                    double sumOfQDistributions = 0;
                    for (int i = 0; i < frame.length; i++) {
                        projDists.add(new ArrayList());

                        // store projection distributions in "bottom" triangle of matrix (index 0-y for
                        // y-th element of frame)
                        for (int j = 0; j < i; j++) {
                            double qdistance = getDistance(projections.get(i).getProjection(),
                                    projections.get(j).getProjection());
                            double qdistribution = 1.0 / (1 + Math.pow(qdistance, 2));
                            // System.out.println("qdistribution is " + qdistribution);
                            projDists.get(i).add(qdistribution);
                            sumOfQDistributions += 2 * qdistribution;
                        }
                    }

                    // System.out.println("sumOfQDistributions is " + sumOfQDistributions);
                    computeAffinities(frame, projDists, sumOfQDistributions, affinities);

                    computePartialDerivatives(frame, similarities, affinities, projections, projDists,
                            partialDerivatives);

                    // check how close we are compared to last iteration and update {@code epsilon}
                    // accordingly by either halving
                    while (computeCost(frame, similarities, affinities) <= computeCost(frame, similarities, projections,
                            epsilon, partialDerivatives)) {
                        // System.out.println("Epsilon halved to " + epsilon);
                        epsilon = epsilon / 2;

                        if (epsilon < precision) {
                            break;
                        }
                    }

                    // calculate the new projection for each node using gradient descent to minimize
                    // the cost function
                    for (int i = 0; i < projections.size(); i++) {
                        projections.get(i).addToProjection(-1 * epsilon * partialDerivatives[i]);
                    }

                    if (iteration == maxIterations - 1 || epsilon < precision) {
                        // System.out.println("Stopped at iteration " + iteration);
                        break;
                    }

                    // gamma can also increase if we are just going down towards minimum
                    epsilon = 2 * epsilon;
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

                // System.out.println("Frame " + x + " done!");

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

                System.out.println("Computed frame " + x + " / " + unsorted.length + ".");

            }
            writer.flush();
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(TSNESimpleStrategy.class.getName()).log(Level.SEVERE, null, ex);
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

        File orderingfile = new File(orderingsfolder + "/" + dsName + "_t-SNE (simple)_ordering.csv");

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

    private double getDistance(double from, double to) {
        return Math.abs(from - to);
    }

    private void computeDistances(DataPoint[] frame, ArrayList<ArrayList<Double>> distances) {
        for (int y = 0; y < frame.length; y++) {
            distances.add(new ArrayList());

            // store distances in "bottom" triangle of matrix (index 0-y for y-th element of
            // frame)
            for (int i = 0; i < y; i++) {
                double dist = getDistance(frame[i], frame[y]);
                // System.out.println("Distance between " + i + " and " + y + " is " + dist);
                distances.get(y).add(dist);
            }

            // add diagonal value in adjacency matrix
            distances.get(y).add(0.0);
        }
    }

    private void computeConditionalPs(DataPoint[] frame, ArrayList<ArrayList<Double>> distances, double sigmaLowerBound,
            double perplexity, ArrayList<ArrayList<Double>> conditionalPs) {
        for (int i = 0; i < frame.length; i++) {
            conditionalPs.add(new ArrayList());

            double sigma = binarySearchSigma(sigmaLowerBound, perplexity, distances, i);

            for (int j = 0; j < frame.length; j++) {
                if (i == j) { // conditional probability is 0 for
                    conditionalPs.get(i).add(0.0);
                } else {
                    conditionalPs.get(i).add(computeCondP(sigma, distances, i, j));
                }
            }
        }
    }

    private double binarySearchSigma(double lowerBound, double perplexity, ArrayList<ArrayList<Double>> distances,
            int i) {
        double upperBound = lowerBound + 1.0;

        // check if lower bound is too low
        // double entropy = 0.0;
        // double Pji;
        // for (int j = 0; j < distances.size(); j++) {
        // Pji = computeCondP(lowerBound, distances, i, j);
        // entropy += -1 * Pji * Math.log(Pji) / Math.log(2);
        // }
        // double perp = Math.pow(2, entropy);
        //
        // if(perp > perplexity) {
        // throw new Exception("Incorrect lower bound of " + lowerBound + " given.");
        // }
        // calculate initial perplexity with upper bound
        double perp = computePerplexity(upperBound, distances, i);

        // System.out.println("first perp is " + perp);
        // System.out.println("Upper and lower bound are " + upperBound + " and " +
        // lowerBound);
        // find upper bound by exponentially growing (and increase lower bound to keep
        // small interval for binary search)
        // perp grows monotonically as upperBound grows, so we eventually find a value
        // for perp such that perplexity < perp
        while (perplexity >= perp) {
            lowerBound = upperBound;
            upperBound *= 2;

            perp = computePerplexity(upperBound, distances, i);
            // System.out.println("perp is " + perp);
            // System.out.println("In loop: Upper and lower bound are " + upperBound + " and
            // " + lowerBound);
        }

        // binary search for right value of sigma (will be in {@code lowerBound} at end
        // of loop)
        while (upperBound - lowerBound > precision) {
            // System.out.println("We get here");
            double h = (lowerBound + upperBound) / 2;

            perp = computePerplexity(h, distances, i);
            // System.out.println("perp is " + perp);

            if (perp <= perplexity) {
                lowerBound = h;
            } else {
                upperBound = h;
            }
        }

        // System.out.println("Sigma is " + lowerBound);
        return lowerBound;
    }

    private double computePerplexity(double sigma, ArrayList<ArrayList<Double>> distances, int i) {
        double entropy = 0.0;
        double Pji;
        for (int j = 0; j < distances.size(); j++) {

            Pji = computeCondP(sigma, distances, i, j);
            // System.out.println("Pji is for sigma " + sigma + " and i " + i + " and j " +
            // j + " is " + Pji);

            if (Pji != 0.0) {
                entropy += -1 * Pji * Math.log(Pji) / Math.log(2);
            }
            // System.out.println("entropy is " + entropy);
        }

        return Math.pow(2, entropy);
    }

    private double computeCondP(double sigma, ArrayList<ArrayList<Double>> distances, int i, int j) {
        double num;

        if (j < i) {
            num = Math.exp(-1 * Math.pow(distances.get(i).get(j), 2) / (2 * Math.pow(sigma, 2)));
        } else if (i < j) {
            num = Math.exp(-1 * Math.pow(distances.get(j).get(i), 2) / (2 * Math.pow(sigma, 2)));
        } else {
            num = 0;
        }

        // System.out.println("num is " + num);
        double denom = 0.0;
        for (int k = 0; k < distances.size(); k++) {
            if (k < i) {
                denom += Math.exp(-1 * Math.pow(distances.get(i).get(k), 2) / (2 * Math.pow(sigma, 2)));
                if (i == 12) {
                    // System.out.println("Fraction " + (-1 * Math.pow(distances.get(i).get(k), 2) /
                    // (2 * Math.pow(sigma, 2))));
                    // System.out.println("Denom term " + (Math.expm1(-1 *
                    // Math.pow(distances.get(i).get(k), 2) / (2 * Math.pow(sigma, 2))) + 1) );
                }
            } else if (i < k) {
                denom += Math.exp(-1 * Math.pow(distances.get(k).get(i), 2) / (2 * Math.pow(sigma, 2)));
                if (i == 12) {
                    // System.out.println("Fraction " + (-1 * Math.pow(distances.get(k).get(i), 2) /
                    // (2 * Math.pow(sigma, 2))));
                    // System.out.println("Denom term " + (Math.expm1(-1 *
                    // Math.pow(distances.get(k).get(i), 2) / (2 * Math.pow(sigma, 2))) + 1) );
                }
            }
        }

        // System.out.println("denom is " + denom);
        double result;

        if (num == 0.0) {
            result = num;
        } else {
            result = num / denom;
        }

        return result;
    }

    private void computeSimilarities(DataPoint[] frame, ArrayList<ArrayList<Double>> conditionalPs,
            ArrayList<ArrayList<Double>> similarities) {
        for (int i = 0; i < frame.length; i++) {
            similarities.add(new ArrayList());

            // store distances in "bottom" triangle of matrix (index 0-y for y-th element of
            // frame)
            for (int j = 0; j < i; j++) {
                double similarity = (conditionalPs.get(i).get(j) + conditionalPs.get(j).get(i)) / (2 * frame.length);
                similarities.get(i).add(similarity);
            }
        }
    }

    private double initializeSolution() {
        Random r = new Random();
        return r.nextGaussian() * 0.0001;
    }

    private void computeAffinities(DataPoint[] frame, ArrayList<ArrayList<Double>> projDists,
            double sumOfQDistributions, ArrayList<ArrayList<Double>> affinities) {
        for (int i = 0; i < frame.length; i++) {
            affinities.add(new ArrayList());

            for (int j = 0; j < i; j++) {
                double affinity = projDists.get(i).get(j) / sumOfQDistributions;
                // System.out.println("Affinity is " + affinity);
                affinities.get(i).add(affinity);
            }
        }
    }

    private void computePartialDerivatives(DataPoint[] frame, ArrayList<ArrayList<Double>> similarities,
            ArrayList<ArrayList<Double>> affinities, ArrayList<Projection1D> projections,
            ArrayList<ArrayList<Double>> projDists, double[] partialDerivatives) {
        for (int i = 0; i < frame.length; i++) {
            double partDeriv = 0;
            for (int j = 0; j < frame.length; j++) {
                if (j < i) {
                    partDeriv += (similarities.get(i).get(j) - affinities.get(i).get(j))
                            * (projections.get(i).getProjection() - projections.get(j).getProjection())
                            * projDists.get(i).get(j);
                } else if (i < j) {
                    partDeriv += (similarities.get(j).get(i) - affinities.get(j).get(i))
                            * (projections.get(i).getProjection() - projections.get(j).getProjection())
                            * projDists.get(j).get(i);
                }
                // System.out.println("Intermediate partial derivative for " + i + " is " +
                // partDeriv);
            }

            partialDerivatives[i] = 4 * partDeriv;
            // System.out.println("Partial derivative for " + i + " is " +
            // partialDerivatives[i]);
        }
    }

    private double computeCost(DataPoint[] frame, ArrayList<ArrayList<Double>> Ps, ArrayList<ArrayList<Double>> Qs) {
        double result = 0;

        for (int i = 0; i < frame.length; i++) {
            for (int j = 0; j < frame.length; j++) {
                double intermediate = 0;
                double intermediatep = 0;
                double intermediateq = 0;
                if (j < i) {
                    intermediate = Ps.get(i).get(j) * Math.log(Ps.get(i).get(j) / Qs.get(i).get(j));
                    intermediatep = Ps.get(i).get(j);
                    intermediateq = Qs.get(i).get(j);
                    result += intermediate;
                } else if (i < j) {
                    intermediate = Ps.get(j).get(i) * Math.log(Ps.get(j).get(i) / Qs.get(j).get(i));
                    intermediatep = Ps.get(j).get(i);
                    intermediateq = Qs.get(j).get(i);
                    result += intermediate;
                }
                // System.out.println("Cost intermediate value with Pij " + intermediatep + "
                // and Qij " + intermediateq + " is " + intermediate);
            }
        }

        // System.out.println("Cost now is " + result);
        return result;
    }

    private double computeCost(DataPoint[] frame, ArrayList<ArrayList<Double>> Ps, ArrayList<Projection1D> projections,
            double epsilon, double[] partialDerivatives) {
        double result = 0;

        ArrayList<ArrayList<Double>> distributions = new ArrayList<>();
        ArrayList<ArrayList<Double>> newQs = new ArrayList<>();

        // calculate (1 + distance^2)^-1 in new projection
        double sumOfQDistributions = 0;
        for (int i = 0; i < frame.length; i++) {
            distributions.add(new ArrayList());

            // store distances in "bottom" triangle of matrix (index 0-y for y-th element of
            // frame)
            for (int j = 0; j < i; j++) {
                double newProji = projections.get(i).getProjection() - (epsilon * partialDerivatives[i]);
                double newProjj = projections.get(j).getProjection() - (epsilon * partialDerivatives[j]);
                double qdistance = getDistance(newProji, newProjj);
                double qdistribution = Math.pow(1 + Math.pow(qdistance, 2), -1);
                distributions.get(i).add(qdistribution);
                sumOfQDistributions += 2 * qdistribution;
            }
        }

        // calculate low-dimensional affinities with distributions of projections
        for (int i = 0; i < frame.length; i++) {
            newQs.add(new ArrayList());

            for (int j = 0; j < i; j++) {
                double affinity = distributions.get(i).get(j) / sumOfQDistributions;
                newQs.get(i).add(affinity);
            }
        }

        // calculate cost function using new affinities
        for (int i = 0; i < frame.length; i++) {
            for (int j = 0; j < frame.length; j++) {
                double intermediate = 0;
                double intermediatep = 0;
                double intermediateq = 0;
                if (j < i) {
                    intermediate = Ps.get(i).get(j) * Math.log(Ps.get(i).get(j) / newQs.get(i).get(j));
                    intermediatep = Ps.get(i).get(j);
                    intermediateq = newQs.get(i).get(j);
                    result += intermediate;
                } else if (i < j) {
                    intermediate = Ps.get(j).get(i) * Math.log(Ps.get(j).get(i) / newQs.get(j).get(i));
                    result += intermediate;
                    intermediatep = Ps.get(j).get(i);
                    intermediateq = newQs.get(j).get(i);
                }
                // System.out.println("Cost intermediate value with Pij " + intermediatep + "
                // and Qij " + intermediateq + " is " + intermediate);
            }
        }

        // System.out.println("Cost with epsilon " + epsilon + " is " + result);
        return result;
    }

}
