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
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import dbvis.visualsummaries.data.DataPoint;

/**
 *
 * @author jwulms
 */
public class ClairvoyantClusterPCStrategy implements Strategy {

    private ClusteredFrame[] clusterinfo;
    private double thresholdConstant = 1.0;
    private boolean chasing = false;
    private double chaseSpeed = Double.POSITIVE_INFINITY;

    /**
     * Initializes this strategy to always linearly interpolate when first PC is
     * not pronounced enough.
     *
     * @param thresholdConstant determines whether a PC is pronounced enough
     */
    public ClairvoyantClusterPCStrategy(double thresholdConstant) {
        this.thresholdConstant = thresholdConstant;
    }

    /**
     * Initializes this strategy to chase by chasing angle by at most {@code k}
     * every time step, when first PC is not pronounced enough.
     *
     * @param thresholdConstant determines whether a PC is pronounced enough
     * @param speed             maximum chase speed of angle
     */
    public ClairvoyantClusterPCStrategy(double thresholdConstant, double speed) {
        this.thresholdConstant = thresholdConstant;
        this.chaseSpeed = speed;
    }

    public LinkedHashMap<Integer, Vector> getDirections(int frame) {
        return clusterinfo[frame].getDirections();
    }

    public LinkedHashMap<Integer, double[]> getEigenvalues(int frame) {
        return clusterinfo[frame].getEigenvalues();
    }

    /**
     * Set the threshold constant which determines whether a PC is pronounced
     *
     * @param constant threshold constant between 0 and 1
     */
    public void setThresHoldConstant(double constant) {
        this.thresholdConstant = constant;
    }

    @Override
    public String getName() {
        return "CLC + SPC";
    }

    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {
        DataPoint[][] result = new DataPoint[unsorted.length][unsorted[0].length];

        // store info about clusters/directions/eigenvalues
        clusterinfo = new ClusteredFrame[unsorted.length];

        // per cluster store the frame where interpolation/chasing started
        LinkedHashMap<Integer, Integer> lastKeyframes = new LinkedHashMap<>();
        // store the clusters at keyframe lookup via representative ids
        LinkedHashMap<Integer, Cluster> keyframeClusters = new LinkedHashMap<>();

        // find eigenvalues and directions by clustering and store everything for first
        // frame --------------------------------------------------------------------
        makeFirstFrame(unsorted, result, lastKeyframes, keyframeClusters);

        int x = 1;
        double deltaDir = 0.0;
        // find directions per frame
        while (x < unsorted.length) {
            // find clustering using CLC
            ArrayList<Cluster> clusters = makeClustering(unsorted[x]);
            clusterinfo[x] = new ClusteredFrame(clusters);

            // find first frame where at least one cluster can interpolate/chase to
            ArrayList<Integer> needChanging = findFirstPronouncedIndex(x, unsorted, keyframeClusters);

            // remove last entry containing timestep to interpolate to
            int nextTimestep = needChanging.get(needChanging.size() - 1);
            needChanging.remove(needChanging.size() - 1);

            // for all other entries we have to update the corresponding cluster
            for (int repID : needChanging) {
                // interpolate up until nextTimestep for positive repID and nextTimestep-1 for
                // negative repID ----------------------------------------------------

                int deltaT = 0;
                int start = 0;
                boolean sameCluster = false;
                // check repID
                if (repID < 0) {
                    repID *= -1;
                    // retrieve where to start the changing
                    start = lastKeyframes.get(repID);
                    // check difference in angle
                    deltaDir += accumulateAngle(start, nextTimestep - 1, repID, unsorted);
                    // check difference in frames (previous frame is last processed one)
                    deltaT = nextTimestep - start;
                } else {
                    sameCluster = true;
                    // retrieve where to start the changing
                    start = lastKeyframes.get(repID);
                    // check difference in angle
                    deltaDir += accumulateAngle(start, nextTimestep, repID, unsorted);
                    // check difference in frames (previous frame is last processed one)
                    deltaT = nextTimestep - (start - 1);
                }

                // find directions for upcoming frames depending on different in frames and
                // angle
                if (deltaT == 1) { // next frame has pronounced PC
                    if (!chasing) { // we are not chasing
                        // we already calculated the directions correctly
                        deltaDir = 0.0;
                    } else { // chase an optimal solution

                        if (Math.abs(deltaDir) > chaseSpeed) {
                            // System.out.println("chasing one frame");
                            // find new angle by rotating at maximum speed
                            Vector previous = clusterinfo[x - 1].getDirections().get(repID);
                            double angle = Math.atan2(previous.getY(), previous.getX())
                                    - (Math.signum(deltaDir) * chaseSpeed);
                            clusterinfo[x].getDirections().put(repID, new Vector(Math.cos(angle), Math.sin(angle)));
                            deltaDir -= Math.signum(deltaDir) * chaseSpeed;
                            // System.out.println("Angle difference left: " + deltaDir);
                        } else {
                            // System.out.println("interpolate one frame");
                            // find new angle by interpolating linearly
                            Vector previous = clusterinfo[x - 1].getDirections().get(repID);
                            double angle = Math.atan2(previous.getY(), previous.getX()) - deltaDir;
                            clusterinfo[x].getDirections().put(repID, new Vector(Math.cos(angle), Math.sin(angle)));
                            chasing = false;
                            deltaDir = 0.0;
                        }
                    }
                } else if (Math.abs(deltaDir / deltaT) > chaseSpeed) { // next pronounced requires angle to change too
                                                                       // much, so start chasing
                    // System.out.println("chasing to next");
                    for (int frame = start; frame < start + deltaT; frame++) {
                        // find new angle by rotating at maximum speed
                        Vector previous = clusterinfo[frame - 1].getDirections().get(repID);
                        double angle = Math.atan2(previous.getY(), previous.getX())
                                - (Math.signum(deltaDir / deltaT) * chaseSpeed);
                        clusterinfo[frame].getDirections().put(repID, new Vector(Math.cos(angle), Math.sin(angle)));
                        // update how much we still need to rotate
                        deltaDir -= Math.signum(deltaDir) * chaseSpeed;
                        // System.out.println("Angle difference left: " + deltaDir);
                    }
                    // we start chasing after next pronounced
                    chasing = true;
                } else { // next pronounced requires moderate angle change, so we interpolate up to next
                         // pronounced
                    // System.out.println("Change in angle per time step: " + (deltaDir / deltaT));
                    for (int frame = start; frame < start + deltaT; frame++) {
                        // find new angle by interpolating linearly
                        Vector previous = clusterinfo[frame - 1].getDirections().get(repID);
                        double angle = Math.atan2(previous.getY(), previous.getX()) - (deltaDir / deltaT);
                        clusterinfo[frame].getDirections().put(repID, new Vector(Math.cos(angle), Math.sin(angle)));
                    }
                    // go to frame after interpolation
                    chasing = false;
                    deltaDir = 0.0;
                }

                // update support structures
                // ------------------------------------------------------------------------------------------------
                if (sameCluster) {
                    lastKeyframes.put(repID, nextTimestep + 1);
                } else {
                    lastKeyframes.remove(repID);
                    keyframeClusters.remove(repID);
                }
            }

            x = nextTimestep + 1;

            // further update support structures
            // -----------------------------------------------------------------------------------------------
            clusters = clusterinfo[nextTimestep].getClusters();

            // set up keyframes and save clustering
            for (int c = 0; c < clusters.size(); c++) {
                Cluster clust = clusters.get(c);
                // use maximum index as representative
                int max = Integer.MIN_VALUE;
                for (int index : clust.getIndices()) {
                    if (index > max) {
                        max = index;
                    }
                }
                if (!lastKeyframes.containsKey(max)) {
                    lastKeyframes.put(max, x);
                    keyframeClusters.put(max, clust);
                }
            }
        }

        // find the ordering based on calculated PC directions
        // -------------------------------------------------------------------------------------
        // arraylist to remember pc lines per point, and in last slot overall pc line
        Line[] prevPCs = new Line[unsorted.length + 1];

        // code to extract projections
        String userdir = System.getProperty("user.home");
        File orderingsfolder = new File(userdir + "/motionrugs/ordering");
        if (!orderingsfolder.exists()) {
            orderingsfolder.mkdir();
        }

        File orderingfile = new File(orderingsfolder + "/lastuseddataset_CLC+PCA_1D.csv");
        try {
            // writer for projections
            FileWriter writer = new FileWriter(orderingfile);

            writer.write(
                    "single timestep per line, per timestep id*projection, new cluster indicated (at start) with \"cluster\" \n");

            // find order per frame
            for (x = 0; x < unsorted.length; x++) {

                // idx is an array of the point indexes
                Integer[] idx = new Integer[unsorted[x].length];

                // calculate PC strategy on clusters
                idx = findOrdering(x, unsorted[x], prevPCs, writer);

                // sort the result set after the cluster order
                for (int y = 0; y < unsorted[x].length; y++) {
                    result[x][y] = unsorted[x][idx[y]];
                }
                System.out.println("Frame " + x + " done, with " + clusterinfo[x].getClusters().size() + " clusters.");
            }

            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(PrincipalComponentStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    /**
     * Order first frame using CLC to cluster and SPC to order, subsequently
     * store in {@code result}
     *
     * @param unsorted data set
     * @param result   result array
     */
    private void makeFirstFrame(DataPoint[][] unsorted, DataPoint[][] result,
            LinkedHashMap<Integer, Integer> lastKeyframes, LinkedHashMap<Integer, Cluster> keyframeClusters) {

        // find clustering using CLC
        ArrayList<Cluster> clusters = makeClustering(unsorted[0]);
        clusterinfo[0] = new ClusteredFrame(clusters);

        // set up keyframes and save clustering
        for (int c = 0; c < clusters.size(); c++) {
            Cluster clust = clusters.get(c);
            // use maximum index as representative
            int max = Integer.MIN_VALUE;
            for (int index : clust.getIndices()) {
                if (index > max) {
                    max = index;
                }
            }
            lastKeyframes.put(max, 1);
            keyframeClusters.put(max, clust);
        }

        // idx is an array of the point indices, idc an array of cluster indices
        Integer[][] idx = new Integer[clusters.size()][];
        Integer[] idc = new Integer[clusters.size()];
        // array to save where points are projected to principal component of cluster,
        // last row stores pc for overall data set
        double pcScalars[][] = new double[clusters.size() + 1][];

        // find line along principal component, with "origin" at mean of point set
        Line[] pcs = getPrincipalComponents(clusters, unsorted[0].length, 0);

        // project mean of clusters to principal component of whole data set and project
        // points to pc per cluster
        pcScalars[clusters.size()] = new double[clusters.size()];
        for (int c = 0; c < clusters.size(); c++) {
            idc[c] = c;
            ArrayList<DataPoint> cluster = clusters.get(c).getElements();
            Vector clusterMean = new Vector(pcs[c].getPoint().getX(), pcs[c].getPoint().getY());

            // the projection scalars all assume same origin of line
            // we can use them to find order of projected points
            pcScalars[clusters.size()][c] = pcs[clusters.size()].projectionScalar(clusterMean);

            // prep index and scalar array for cluster {@code c}
            idx[c] = new Integer[cluster.size()];
            pcScalars[c] = new double[cluster.size()];

            for (int y = 0; y < cluster.size(); y++) {
                idx[c][y] = y;

                DataPoint point = cluster.get(y);
                Vector datapoint = new Vector(point.getX(), point.getY());
                int id = point.getId();

                // the projection scalars all assume same origin of line
                // we can use them to find order of projected points
                pcScalars[c][y] = pcs[c].projectionScalar(datapoint);
            }

            double[] dummy = pcScalars[c];
            // sort the index array with comparing the projection scalars
            Arrays.sort(idx[c], new Comparator<Integer>() {
                @Override
                public int compare(final Integer o1, final Integer o2) {
                    return Double.compare(dummy[o1], dummy[o2]);

                }
            });
        }

        double[] dummy = pcScalars[clusters.size()];
        // sort the cluster array with comparing the projection scalars
        Arrays.sort(idc, new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return Double.compare(dummy[o1], dummy[o2]);

            }
        });

        int pointer = 0;
        // sort on projected cluster means
        for (int i = 0; i < idc.length; i++) {
            int c = idc[i];
            ArrayList<DataPoint> cluster = clusters.get(c).getElements();
            // per cluster sort on projected points
            for (int index = 0; index < cluster.size(); index++) {
                result[0][pointer] = cluster.get(idx[c][index]);
                pointer++;
            }
        }
    }

    private ArrayList<Cluster> makeClustering(DataPoint[] unsorted) {
        // arraylist to save remaining clusters in
        ArrayList<Cluster> clusters = new ArrayList();
        // matrix containing distances between clusters
        ArrayList<ArrayList<Double>> distances = new ArrayList();

        // put points in separate clusters and find distances between them
        for (int y = 0; y < unsorted.length; y++) {
            CompLinkCluster singleElement = new CompLinkCluster(unsorted[y], y);
            clusters.add(singleElement);
            distances.add(new ArrayList());

            // store distances in "bottom" triangle of matrix (index 0-y for y-th element of
            // unsorted)
            for (int i = 0; i < y; i++) {
                distances.get(y).add(clusters.get(i).getDistance(singleElement));
            }
            // add diagonal value in adjacency matrix
            distances.get(y).add(0.0);
        }

        // make clustering
        return completeLinkageClusterPartitioning(clusters, distances);
    }

    private ArrayList<Cluster> completeLinkageClusterPartitioning(ArrayList<Cluster> clusters,
            ArrayList<ArrayList<Double>> distances) {
        double prevClosest = Double.MAX_VALUE;

        while (clusters.size() > 1) {
            double closest = Double.MAX_VALUE;
            int first = 0;
            int second = clusters.size() - 1;

            // find two clusters that are closest to each other
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

            // merge the two closest clusters
            Cluster merged;
            if (clusters.get(first) instanceof SNNCluster && clusters.get(second) instanceof SNNCluster) {
                SNNCluster child1 = (SNNCluster) clusters.get(first);
                SNNCluster child2 = (SNNCluster) clusters.get(second);
                merged = new SNNCluster(child1, child2);
            } else { // clusters.get(first) instanceof CompLinkCluster
                merged = new CompLinkCluster(clusters.get(first), clusters.get(second));
            }
            clusters.add(merged);
            // remove merged clusters
            clusters.remove(first);
            clusters.remove(second - 1);

            // add new distances distances
            distances.add(new ArrayList());
            int newList = distances.size() - 1;
            // find new distances by checking existing ones, and remove distances to merged
            // clusters
            for (int i = 0; i < distances.size() - 1; i++) {
                if (i != first && i != second) {
                    // this is a distance between the new cluster and one of the remaining clusters
                    double newDistance;
                    // the new distance is the maximum distance between one of the two merged
                    // clusters and another cluster
                    newDistance = findMaxDistanceAndRemove(distances, i, first, second);
                    distances.get(newList).add(newDistance);
                }
            }
            // add diagonal value in adjacency matrix
            distances.get(newList).add(0.0);
            // remove merged clusters from distances matrix
            distances.remove(first);
            distances.remove(second - 1);
        }

        return clusters;
    }

    private double findMaxDistanceAndRemove(ArrayList<ArrayList<Double>> distances, int old, int firstMerged,
            int secondMerged) {
        double maxDistance;

        if (old < firstMerged) {
            maxDistance = Math.max(distances.get(firstMerged).get(old), distances.get(secondMerged).get(old));
        } else if (old < secondMerged) {
            maxDistance = Math.max(distances.get(old).get(firstMerged), distances.get(secondMerged).get(old));
            // update row for old by removing distance to one merged cluster
            distances.get(old).remove(firstMerged);
        } else {
            maxDistance = Math.max(distances.get(old).get(firstMerged), distances.get(old).get(secondMerged));
            // update row for old by removing distance to both merged clusters
            distances.get(old).remove(firstMerged);
            distances.get(old).remove(secondMerged - 1);
        }

        return maxDistance;
    }

    private boolean tiebreakNewCostCloser(ArrayList<Cluster> clusters, int newFirst, int newSecond, int oldFirst,
            int oldSecond) {
        return false;
    }

    /**
     * Returns a line along the first principal component of the dataset during
     * a single frame
     *
     * @param unsorted dataset at a single frame
     * @return line along the first principal component of the dataset
     */
    private Line[] getPrincipalComponents(ArrayList<Cluster> clusters, int totalSize, int frame) {
        Line[] result = new Line[clusters.size() + 1];

        // prep data for singular value decompositions
        double[][][] data = new double[clusters.size() + 1][][];
        // prep for final pca on overall data set
        data[clusters.size()] = new double[totalSize][2];
        int index = 0;
        // also find mean of each cluster and the overall data set
        double[][] mean = new double[clusters.size() + 1][2];
        // principal components of clusters and data set
        double[][] pc = new double[clusters.size() + 1][2];

        // pca per cluster
        for (int c = 0; c < clusters.size(); c++) {
            ArrayList<DataPoint> cluster = clusters.get(c).getElements();
            data[c] = new double[cluster.size()][2];

            for (int i = 0; i < cluster.size(); i++) {
                DataPoint point = cluster.get(i);
                // for cluster {@code c}
                data[c][i][0] = point.getX();
                data[c][i][1] = point.getY();

                mean[c][0] += point.getX();
                mean[c][1] += point.getY();

                // for overall data set in last row of {@code data}
                data[clusters.size()][index][0] = point.getX();
                data[clusters.size()][index][1] = point.getY();
                index++;
            }

            // maintain overal mean in last row of {@code data}
            mean[clusters.size()][0] += mean[c][0];
            mean[clusters.size()][1] += mean[c][1];

            mean[c][0] = mean[c][0] / cluster.size();
            mean[c][1] = mean[c][1] / cluster.size();

            for (int i = 0; i < cluster.size(); i++) {
                data[c][i][0] -= mean[c][0];
                data[c][i][1] -= mean[c][1];
            }

            // get the singular value decomposition with diagonal matrix Sigma of singular
            // values
            // and matrix V containing a right singular vector (eigenvector) in each column
            Array2DRowRealMatrix datamatrix = new Array2DRowRealMatrix(data[c]);
            SingularValueDecomposition svd = new SingularValueDecomposition(datamatrix);

            // get the first principal component
            pc[c] = svd.getV().getColumn(0);

            Vector point = new Vector(mean[c][0], mean[c][1]);
            Vector direction = new Vector(pc[c][0], pc[c][1]);

            // use maximum index as representative
            int max = Integer.MIN_VALUE;
            for (int i : clusters.get(c).getIndices()) {
                if (i > max) {
                    max = i;
                }
            }
            int repID = max;

            clusterinfo[frame].setDirection(repID, direction);
            clusterinfo[frame].setEigenvalues(repID, svd.getSingularValues());

            result[c] = new Line(point, direction);
        }

        // pca on overall data set
        mean[clusters.size()][0] = mean[clusters.size()][0] / totalSize;
        mean[clusters.size()][1] = mean[clusters.size()][1] / totalSize;

        for (int i = 0; i < totalSize; i++) {
            data[clusters.size()][i][0] -= mean[clusters.size()][0];
            data[clusters.size()][i][1] -= mean[clusters.size()][1];
        }

        // get the singular value decomposition with diagonal matrix Sigma of singular
        // values
        // and matrix V containing a right singular vector (eigenvector) in each column
        Array2DRowRealMatrix datamatrix = new Array2DRowRealMatrix(data[clusters.size()]);
        SingularValueDecomposition svd = new SingularValueDecomposition(datamatrix);

        // get the first principal component
        pc[clusters.size()] = svd.getV().getColumn(0);

        Vector point = new Vector(mean[clusters.size()][0], mean[clusters.size()][1]);
        Vector direction = new Vector(pc[clusters.size()][0], pc[clusters.size()][1]);

        clusterinfo[frame].setDirection(-1, direction);
        clusterinfo[frame].setEigenvalues(-1, svd.getSingularValues());

        result[clusters.size()] = new Line(point, direction);

        return result;
    }

    /**
     * Find index of first frame where first PC is pronounced enough.
     *
     * @param x        index to start searching from
     * @param unsorted dataset to search through
     * @return indices of representatives that should interpolate up to
     *         {@code x - 1}
     */
    private ArrayList<Integer> findFirstPronouncedIndex(int x, DataPoint[][] unsorted,
            LinkedHashMap<Integer, Cluster> keyframeClusters) {
        ArrayList<Integer> result = new ArrayList<>();

        // find clustering using CLC
        ArrayList<Cluster> clusters = makeClustering(unsorted[x]);
        clusterinfo[x] = new ClusteredFrame(clusters);

        // find clusters that should interpolate
        result = findChangedOrPronouncedClusters(x, unsorted, clusters, keyframeClusters);

        int firstPronounced = x;

        while (result.isEmpty() && firstPronounced < unsorted.length - 1) {
            firstPronounced++;

            // find clustering using CLC
            clusters = makeClustering(unsorted[firstPronounced]);
            clusterinfo[firstPronounced] = new ClusteredFrame(clusters);

            result = findChangedOrPronouncedClusters(firstPronounced, unsorted, clusters, keyframeClusters);
        }

        // when reaching the end, everything should interpolate
        if (firstPronounced == unsorted.length - 1) {
            result.addAll(keyframeClusters.keySet());
        }
        // add frameID as last entry in results array
        result.add(firstPronounced);

        return result;
    }

    private ArrayList<Integer> findChangedOrPronouncedClusters(int x, DataPoint[][] unsorted,
            ArrayList<Cluster> newClusters, LinkedHashMap<Integer, Cluster> keyframeClusters) {
        ArrayList<Integer> result = new ArrayList<>();

        // find principal components in clusters, so that eigenvalues (and directions)
        // are calculated
        Line[] pcs = getPrincipalComponents(newClusters, unsorted[x].length, x);

        for (int repID : keyframeClusters.keySet()) {
            Cluster clust = containsID(newClusters, repID);

            if (!sameElements(clust, keyframeClusters.get(repID))) {
                // denote cluster changes with negative repID's
                int change = -1 * repID;
                result.add(change);
            } else { // clust is exactly the same cluster as the one hashed at max
                // check if pronounced, eigenvalues are already calculated
                double[] eigenvalues = clusterinfo[x].getEigenvalues().get(repID);
                if (eigenvalues.length == 1) { // cluster only has a single point in it
                    result.add(repID);
                } else {
                    double eigenratio = eigenvalues[1] / eigenvalues[0];
                    double threshold = (eigenratio - thresholdConstant);
                    if (threshold <= 0) {
                        result.add(repID);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Check which cluster in {@code newClusters} contains a datapoint with id
     * {@code repID}.
     *
     * @param newClusters
     * @param repID
     * @return cluster in {@code newClusters} containing {@code repID}
     */
    private Cluster containsID(ArrayList<Cluster> newClusters, int repID) {
        Cluster result = null;

        for (Cluster clust : newClusters) {
            if (clust.getIndices().contains(repID)) {
                result = clust;
                break;
            }
        }

        return result;
    }

    /**
     * Check whether {@code clust1} and {@code clust2} have the same elements.
     *
     * @param clust1
     * @param clust2
     * @return #elements of {
     * @clust1} == #elements of {@code clust2}
     */
    private boolean sameElements(Cluster clust1, Cluster clust2) {
        boolean result = true;

        if (clust1.getIndices().size() != clust2.getIndices().size()) {
            result = false;
        } else {
            for (int id : clust1.getIndices()) {
                if (!clust2.getIndices().contains(id)) {
                    result = false;
                    break;
                }
            }

        }

        return result;
    }

    /**
     * Returns difference in angle in the range of -<i>pi</i> to <i>pi</i>.
     *
     * @param oldAngle
     * @param newAngle
     * @return oldAngle - newAngle (adjusted to be in the range of -<i>pi</i> to
     *         <i>pi</i>)
     */
    private double angleDiff(double oldAngle, double newAngle) {
        double diff = oldAngle - newAngle;
        // if (diff < -2 * Math.PI) {
        // diff += 2 * Math.PI;
        // } else if (diff > 2 * Math.PI) {
        // diff -= 2 * Math.PI;
        // }

        return diff;
    }

    /**
     * Finds the difference in first PC angle between frame {@code start} and
     * frame {@code nextPronounced - 1} in {@code unsorted}.
     *
     * @param x            first frame index
     * @param nextTimestep up to (but not including) frame index
     * @param unsorted     data set in which we find difference in angle
     * @return difference in first PC angle between frame {@code x} and frame
     *         {@code nextPronounced}
     */
    private double accumulateAngle(int start, int nextTimestep, int repID, DataPoint[][] unsorted) {
        double deltaAngle = 0.0;
        double newAngle, oldAngle, diff;
        int extra = 0;
        // find angle of pc in start frame before accumulating up to nextTimestep
        Vector previous = clusterinfo[start - 1].directions.get(repID);
        oldAngle = Math.atan2(previous.getY(), previous.getX());
        // if the cluster changed (repID negative) we do not want to interpolate all the
        // way to nextTimestep
        if (repID < 0) {
            repID *= -1;
            extra = -1;
        }
        for (int frame = start; frame < nextTimestep + extra; frame++) {
            // find angle of pc in next frame
            Line pc = new Line(new Vector(0, 0), clusterinfo[frame].directions.get(repID));
            pc.adjustForFlip(previous);
            newAngle = Math.atan2(pc.getDirection().getY(), pc.getDirection().getX());
            // ensure we find the difference in angle correctly
            diff = oldAngle - newAngle;
            if (diff > Math.PI * 1.9) { // close to 360 degrees means jump from Pi to -Pi, 1.9 is enough for our data
                                        // sets, make lower if {@code pc} changes quicker
                diff -= 2 * Math.PI;
            } else if (diff < -Math.PI * 1.9) { // close to - 360 degrees means jump from -Pi to Pi, 1.9 is enough for
                                                // our data sets, make lower if {@code pc} changes quicker
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

    private Integer[] findOrdering(int frame, DataPoint[] unsorted, Line[] prevPCs, FileWriter writer)
            throws IOException {
        // take clusters at correct frame frame
        ArrayList<Cluster> clusters = clusterinfo[frame].getClusters();

        // idx is an array of the point indices, idc an array of cluster indices
        Integer[][] idx = new Integer[clusters.size()][];
        Integer[] idc = new Integer[clusters.size()];
        // array to save where points are projected to principal component of cluster,
        // last row stores pc for overall data set
        double pcScalars[][] = new double[clusters.size() + 1][];

        // find line along principal component, with "origin" at mean of point set
        Line[] pcs = getPrincipalComponent(frame, clusters, unsorted.length);

        // ensure overal pc line is not flipped
        if (prevPCs[prevPCs.length - 1] != null) {
            pcs[pcs.length - 1].adjustForFlip(prevPCs[prevPCs.length - 1].getDirection());
        }
        prevPCs[prevPCs.length - 1] = pcs[pcs.length - 1];

        boolean firstframe = false;
        if (prevPCs[0] == null) {
            firstframe = true;
        }

        // stringbuilder for writing projections
        StringBuilder sb = new StringBuilder();

        sb.append("Clusters,");
        sb.append(clusters.size());
        sb.append(",");

        // project mean of clusters to principal component of whole data set and project
        // points to pc per cluster
        pcScalars[clusters.size()] = new double[clusters.size()];
        for (int c = 0; c < clusters.size(); c++) {
            idc[c] = c;
            ArrayList<DataPoint> cluster = clusters.get(c).getElements();
            Vector clusterMean = new Vector(pcs[c].getPoint().getX(), pcs[c].getPoint().getY());

            // ensure pca line does not flip w.r.t. previous frame
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

                    // check maximum and update
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
            }

            double[] dummy = pcScalars[c];
            // sort the index array with comparing the projection scalars
            Arrays.sort(idx[c], new Comparator<Integer>() {
                @Override
                public int compare(final Integer o1, final Integer o2) {
                    return Double.compare(dummy[o1], dummy[o2]);

                }
            });
        }

        // sort the cluster array with comparing the projection scalars
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
            // per cluster sort on projected points
            int clusterpointer = pointer + cluster.size() - 1;
            for (int index = 0; index < cluster.size(); index++) {
                result[pointer] = cluster.get(idx[c][index]);
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
    private Line[] getPrincipalComponent(int frame, ArrayList<Cluster> clusters, int totalSize) {
        Line[] result = new Line[clusters.size() + 1];

        // prep data for singular value decompositions
        double[][][] data = new double[clusters.size() + 1][][];
        // prep for final pca on overall data set
        data[clusters.size()] = new double[totalSize][2];
        int index = 0;
        // also find mean of each cluster and the overall data set
        double[][] mean = new double[clusters.size() + 1][2];

        // pca per cluster
        for (int c = 0; c < clusters.size(); c++) {
            ArrayList<DataPoint> cluster = clusters.get(c).getElements();
            ArrayList<Integer> indices = clusters.get(c).getIndices();

            data[c] = new double[cluster.size()][2];
            for (int i = 0; i < cluster.size(); i++) {
                DataPoint point = cluster.get(i);
                // for cluster {@code c}
                data[c][i][0] = point.getX();
                data[c][i][1] = point.getY();

                mean[c][0] += point.getX();
                mean[c][1] += point.getY();

                // for overall data set in last row of {@code data}
                data[clusters.size()][index][0] = point.getX();
                data[clusters.size()][index][1] = point.getY();
                index++;
            }

            int max = Integer.MIN_VALUE;
            for (int i : indices) {
                if (i > max) {
                    max = i;
                }
            }

            // maintain overal mean in last row of {@code data}
            mean[clusters.size()][0] += mean[c][0];
            mean[clusters.size()][1] += mean[c][1];

            mean[c][0] = mean[c][0] / cluster.size();
            mean[c][1] = mean[c][1] / cluster.size();

            for (int i = 0; i < cluster.size(); i++) {
                data[c][i][0] -= mean[c][0];
                data[c][i][1] -= mean[c][1];
            }

            // use calculated mean and stored direction
            Vector point = new Vector(mean[c][0], mean[c][1]);
            Vector direction = clusterinfo[frame].getDirections().get(max);

            result[c] = new Line(point, direction);
        }

        // pca on overall data set
        mean[clusters.size()][0] = mean[clusters.size()][0] / totalSize;
        mean[clusters.size()][1] = mean[clusters.size()][1] / totalSize;

        for (int i = 0; i < totalSize; i++) {
            data[clusters.size()][i][0] -= mean[clusters.size()][0];
            data[clusters.size()][i][1] -= mean[clusters.size()][1];
        }

        // get the singular value decomposition with diagonal matrix Sigma of singular
        // values
        // and matrix V containing a right singular vector (eigenvector) in each column
        Array2DRowRealMatrix datamatrix = new Array2DRowRealMatrix(data[clusters.size()]);
        SingularValueDecomposition svd = new SingularValueDecomposition(datamatrix);

        // get the first principal component
        double[] pc = svd.getV().getColumn(0);

        Vector point = new Vector(mean[clusters.size()][0], mean[clusters.size()][1]);
        Vector direction = new Vector(pc[0], pc[1]);

        result[clusters.size()] = new Line(point, direction);

        return result;
    }

    private class ClusteredFrame {

        ArrayList<Cluster> clusters;
        LinkedHashMap<Integer, Vector> directions;
        LinkedHashMap<Integer, double[]> eigenvalues;

        public ClusteredFrame(ArrayList<Cluster> clusters) {
            this.clusters = clusters;
            directions = new LinkedHashMap<>();
            eigenvalues = new LinkedHashMap<>();
        }

        public ArrayList<Cluster> getClusters() {
            return this.clusters;
        }

        public LinkedHashMap<Integer, Vector> getDirections() {
            return this.directions;
        }

        public LinkedHashMap<Integer, double[]> getEigenvalues() {
            return this.eigenvalues;
        }

        public void setDirection(int repID, Vector direction) {
            directions.put(repID, direction);
        }

        public void setEigenvalues(int repID, double[] eigenvalues) {
            this.eigenvalues.put(repID, eigenvalues);
        }
    }

}
