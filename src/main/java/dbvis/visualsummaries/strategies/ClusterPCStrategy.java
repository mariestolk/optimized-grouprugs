/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import dbvis.visualsummaries.data.DataPoint;

/**
 * Complete-linkage clustering strategy with optimal ordering
 *
 * @author Jules Wulms, TU Eindhoven <j.j.h.m.wulms@tue.nl>
 */
public class ClusterPCStrategy implements Strategy {

    @Override
    public String getName() {
        return "CLC + PCA";
    }

    /**
     * Returns dataset ordered using PC strategy on partitioning based on
     * complete-linkage clustering of the point set
     *
     * @param unsorted dataset
     * @return sorted dataset
     */
    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {
        DataPoint[][] result = new DataPoint[unsorted.length][unsorted[0].length];

        //arraylist to remember pc lines per point, and in last slot overall pc line
        Line[] prevPCs = new Line[unsorted.length + 1];

        //code to extract projections
        String userdir = System.getProperty("user.home");
        File orderingsfolder = new File(userdir + "/motionrugs/ordering");
        if (!orderingsfolder.exists()) {
            orderingsfolder.mkdir();
        }

        File orderingfile = new File(orderingsfolder + "/lastuseddataset_CLC+PCA_1D.csv");
        try {
            //writer for projections
            FileWriter writer = new FileWriter(orderingfile);

            writer.write("single timestep per line, per timestep id*projection, new cluster indicated (at start) with \"cluster\" \n");

            //find order per frame
            for (int x = 0; x < unsorted.length; x++) {

                //idx is an array of the point indexes
                Integer[] idx = new Integer[unsorted[x].length];
                //arraylist to save remaining clusters in
                ArrayList<Cluster> clusters = new ArrayList();
                //matrix containing distances between clusters
                ArrayList<ArrayList<Double>> distances = new ArrayList();

                //put points in separate clusters and find distances between them
                for (int y = 0; y < unsorted[x].length; y++) {
                    CompLinkCluster singleElement = new CompLinkCluster(unsorted[x][y], y);
                    clusters.add(singleElement);
                    distances.add(new ArrayList());

                    //store distances in "bottom" triangle of matrix (index 0-y for y-th element of unsorted)
                    for (int i = 0; i < y; i++) {
                        distances.get(y).add(clusters.get(i).getDistance(singleElement));
                    }
                    // add diagonal value in adjacency matrix
                    distances.get(y).add(0.0);
                }

                //make clustering
                clusters = completeLinkageClusterPartitioning(clusters, distances);

                //calculate PC strategy on clusters
                idx = findOrdering(unsorted[x], clusters, prevPCs, writer);

                //sort the result set after the cluster order 
                for (int y = 0; y < unsorted[x].length; y++) {
                    result[x][y] = unsorted[x][idx[y]];
                }
                System.out.println("Frame " + x + " done, with " + clusters.size() + " clusters.");
            }

            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(PrincipalComponentStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public ArrayList<Cluster> completeLinkageClusterPartitioning(ArrayList<Cluster> clusters, ArrayList<ArrayList<Double>> distances) {
        double prevClosest = Double.MAX_VALUE;

        while (clusters.size() > 1) {
            double closest = Double.MAX_VALUE;
            int first = 0;
            int second = clusters.size() - 1;

            //find two clusters that are closest to each other
            for (int y = 0; y < clusters.size(); y++) {
                for (int i = 0; i < y; i++) {
                    double cost = distances.get(y).get(i);
                    if (Math.abs(cost - closest) < 0.0001) {
                        if (tiebreakNewCostCloser(clusters, i, y, first, second)) {
                            closest = cost;
                            first = i;
                            second = y;
                        }
                    } else if (cost < closest) {
                        closest = cost;
                        first = i;
                        second = y;
                    }
                }
            }

            if (clusters.size() < 50 && closest > 2 * prevClosest) { // we have found significant clusters
                break;
            } else {
                prevClosest = closest; // remember previous distance to see whether significant in next step
            }

            //merge the two closest clusters
            Cluster merged;
            if (clusters.get(first) instanceof SNNCluster && clusters.get(second) instanceof SNNCluster) {
                SNNCluster child1 = (SNNCluster) clusters.get(first);
                SNNCluster child2 = (SNNCluster) clusters.get(second);
                merged = new SNNCluster(child1, child2);
            } else { // clusters.get(first) instanceof CompLinkCluster
                merged = new CompLinkCluster(clusters.get(first), clusters.get(second));
            }
            clusters.add(merged);
            //remove merged clusters
            clusters.remove(first);
            clusters.remove(second - 1);

            //add new distances distances
            distances.add(new ArrayList());
            int newList = distances.size() - 1;
            //find new distances by checking existing ones, and remove distances to merged clusters
            for (int i = 0; i < distances.size() - 1; i++) {
                if (i != first && i != second) {
                    //this is a distance between the new cluster and one of the remaining clusters
                    double newDistance;
                    // the new distance is the maximum distance between one of the two merged clusters and another cluster
                    newDistance = findMaxDistanceAndRemove(distances, i, first, second);
                    distances.get(newList).add(newDistance);
                }
            }
            // add diagonal value in adjacency matrix
            distances.get(newList).add(0.0);
            //remove merged clusters from distances matrix
            distances.remove(first);
            distances.remove(second - 1);
        }

        return clusters;
    }

    private double findMaxDistanceAndRemove(ArrayList<ArrayList<Double>> distances, int old, int firstMerged, int secondMerged) {
        double maxDistance;

        if (old < firstMerged) {
            maxDistance = Math.max(distances.get(firstMerged).get(old), distances.get(secondMerged).get(old));
        } else if (old < secondMerged) {
            maxDistance = Math.max(distances.get(old).get(firstMerged), distances.get(secondMerged).get(old));
            //update row for old by removing distance to one merged cluster
            distances.get(old).remove(firstMerged);
        } else {
            maxDistance = Math.max(distances.get(old).get(firstMerged), distances.get(old).get(secondMerged));
            //update row for old by removing distance to both merged clusters
            distances.get(old).remove(firstMerged);
            distances.get(old).remove(secondMerged - 1);
        }

        return maxDistance;
    }

    protected ArrayList<ArrayList<Double>> deepCopy(ArrayList<ArrayList<Double>> listOfLists) {
        ArrayList<ArrayList<Double>> result = new ArrayList();

        int i = 0;
        for (ArrayList<Double> list : listOfLists) {
            result.add(new ArrayList());
            for (double value : list) {
                result.get(i).add(value);
            }
            i++;
        }

        return result;
    }

    protected Vector[] deepCopy(Vector[] array) {
        Vector[] result = new Vector[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }

        return result;
    }

    protected boolean tiebreakNewCostCloser(ArrayList<Cluster> clusters, int newFirst, int newSecond, int oldFirst, int oldSecond) {
        return false;
    }

    private Integer[] findOrdering(DataPoint[] unsorted, ArrayList<Cluster> clusters, Line[] prevPCs, FileWriter writer) throws IOException {

        //idx is an array of the point indices, idc an array of cluster indices
        Integer[][] idx = new Integer[clusters.size()][];
        Integer[] idc = new Integer[clusters.size()];
        //array to save where points are projected to principal component of cluster, last row stores pc for overall data set
        double pcScalars[][] = new double[clusters.size() + 1][];

        //find line along principal component, with "origin" at mean of point set
        Line[] pcs = getPrincipalComponent(clusters, unsorted.length);

        //ensure overal pc line is not flipped
        if (prevPCs[prevPCs.length - 1] != null) {
            pcs[pcs.length - 1].adjustForFlip(prevPCs[prevPCs.length - 1].getDirection());
        }
        prevPCs[prevPCs.length - 1] = pcs[pcs.length - 1];

        boolean firstframe = false;
        if (prevPCs[0] == null) {
            firstframe = true;
        }

        //stringbuilder for writing projections
        StringBuilder sb = new StringBuilder();

        sb.append("Clusters,");
        sb.append(clusters.size());
        sb.append(",");

        //project mean of clusters to principal component of whole data set and project points to pc per cluster
        pcScalars[clusters.size()] = new double[clusters.size()];
        for (int c = 0; c < clusters.size(); c++) {
            idc[c] = c;
            ArrayList<DataPoint> cluster = clusters.get(c).getElements();
            Vector clusterMean = new Vector(pcs[c].getPoint().getX(), pcs[c].getPoint().getY());

            //ensure pca line does not flip w.r.t. previous frame
            if (!firstframe) {
                // get majority vote on previous pc line
                int max = Integer.MIN_VALUE;
                Line majority = pcs[c];
                HashMap<Line, Integer> votes = new HashMap<>();
                for (int i = 0; i < cluster.size(); i++) {
                    int id = cluster.get(i).getId();
                    Line vote = prevPCs[id];
                    Integer votecount = votes.get(vote);

                    // add one more vote
                    if (votecount == null) {
                        votecount = 1;
                    } else {
                        votecount++;
                    }
                    // store the votes
                    votes.put(vote, votecount);

                    //check maximum and update
                    if (votecount > max) {
                        max = votecount;
                        majority = vote;
                    }
                }

                // "soft-align" with majority vote of previous pcs per point
                pcs[c].alignAngle(majority.getDirection());
            }

            // the projection scalars all assume same origin of line
            // we can use them to find order of projected points
            pcScalars[clusters.size()][c] = pcs[clusters.size()].projectionScalar(clusterMean);

            sb.append("cluster,");
            sb.append(pcScalars[clusters.size()][c]);
            sb.append(",");

            // prep index and scalar array for cluster {@code c}
            idx[c] = new Integer[cluster.size()];
            pcScalars[c] = new double[cluster.size()];

            for (int y = 0; y < cluster.size(); y++) {
                idx[c][y] = y;

                DataPoint point = cluster.get(y);
                Vector datapoint = new Vector(point.getX(), point.getY());
                int id = point.getId();

                // store pc line per point for next frame
                prevPCs[id] = pcs[c];

                // the projection scalars all assume same origin of line
                // we can use them to find order of projected points
                pcScalars[c][y] = pcs[c].projectionScalar(datapoint);

                sb.append(id + "*" + pcScalars[c][y]);
                sb.append(",");
            }

            double[] dummy = pcScalars[c];
            //sort the index array with comparing the projection scalars 
            Arrays.sort(idx[c], new Comparator<Integer>() {
                @Override
                public int compare(final Integer o1, final Integer o2) {
                    return Double.compare(dummy[o1], dummy[o2]);

                }
            });
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n");
        writer.write(sb.toString());

        //sort the cluster array with comparing the projection scalars 
        Arrays.sort(idc, new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return Double.compare(pcScalars[clusters.size()][o1], pcScalars[clusters.size()][o2]);

            }
        });

        Integer[] result = new Integer[unsorted.length];
        int pointer = 0;

        // sort on projected cluster means
        for (int i = 0; i < idc.length; i++) {
            int c = idc[i];
            ArrayList<Integer> cluster = clusters.get(c).getIndices();
            //per cluster sort on projected points

//            //adjust angle to be aligned with overal pc line
//            boolean backwards = false;
//            double diff = pcs[pcs.length-1].angleDifference(pcs[c].getDirection());
//            System.out.println("Diff: " + diff);
//            if (diff > Math.PI/2 && diff < Math.PI*3/2) {
//                System.out.println("Backwards");
//                backwards = true;
//            }
            int clusterpointer = pointer + cluster.size() - 1;
            for (int index = 0; index < cluster.size(); index++) {
//                if (backwards) {
//                    result[clusterpointer - index] = cluster.get(idx[c][index]);
//                } else {
                result[pointer] = cluster.get(idx[c][index]);
//                }
                pointer++;
            }
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
    private Line[] getPrincipalComponent(ArrayList<Cluster> clusters, int totalSize) {
        Line[] result = new Line[clusters.size() + 1];

        //prep data for singular value decompositions
        double[][][] data = new double[clusters.size() + 1][][];
        //prep for final pca on overall data set
        data[clusters.size()] = new double[totalSize][2];
        int index = 0;
        //also find mean of each cluster and the overall data set 
        double[][] mean = new double[clusters.size() + 1][2];
        //principal components of clusters and data set
        double[][] pc = new double[clusters.size() + 1][2];

        //pca per cluster
        for (int c = 0; c < clusters.size(); c++) {
            ArrayList<DataPoint> cluster = clusters.get(c).getElements();
            data[c] = new double[cluster.size()][2];

            for (int i = 0; i < cluster.size(); i++) {
                DataPoint point = cluster.get(i);
                //for cluster {@code c}
                data[c][i][0] = point.getX();
                data[c][i][1] = point.getY();

                mean[c][0] += point.getX();
                mean[c][1] += point.getY();

                //for overall data set in last row of {@code data}
                data[clusters.size()][index][0] = point.getX();
                data[clusters.size()][index][1] = point.getY();
                index++;
            }

            //maintain overal mean in last row of {@code data}
            mean[clusters.size()][0] += mean[c][0];
            mean[clusters.size()][1] += mean[c][1];

            mean[c][0] = mean[c][0] / cluster.size();
            mean[c][1] = mean[c][1] / cluster.size();

            for (int i = 0; i < cluster.size(); i++) {
                data[c][i][0] -= mean[c][0];
                data[c][i][1] -= mean[c][1];
            }

            //get the singular value decomposition with diagonal matrix Sigma of singular values
            //and matrix V containing a right singular vector (eigenvector) in each column
            Array2DRowRealMatrix datamatrix = new Array2DRowRealMatrix(data[c]);
            SingularValueDecomposition svd = new SingularValueDecomposition(datamatrix);

            //get the first principal component
            pc[c] = svd.getV().getColumn(0);

            Vector point = new Vector(mean[c][0], mean[c][1]);
            Vector direction = new Vector(pc[c][0], pc[c][1]);

            result[c] = new Line(point, direction);
        }

        //pca on overall data set
        mean[clusters.size()][0] = mean[clusters.size()][0] / totalSize;
        mean[clusters.size()][1] = mean[clusters.size()][1] / totalSize;

        for (int i = 0; i < totalSize; i++) {
            data[clusters.size()][i][0] -= mean[clusters.size()][0];
            data[clusters.size()][i][1] -= mean[clusters.size()][1];
        }

        //get the singular value decomposition with diagonal matrix Sigma of singular values
        //and matrix V containing a right singular vector (eigenvector) in each column
        Array2DRowRealMatrix datamatrix = new Array2DRowRealMatrix(data[clusters.size()]);
        SingularValueDecomposition svd = new SingularValueDecomposition(datamatrix);

        //get the first principal component
        pc[clusters.size()] = svd.getV().getColumn(0);

        Vector point = new Vector(mean[clusters.size()][0], mean[clusters.size()][1]);
        Vector direction = new Vector(pc[clusters.size()][0], pc[clusters.size()][1]);

        result[clusters.size()] = new Line(point, direction);

        return result;
    }

    double getDistance(Vector a, Vector b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }
}
