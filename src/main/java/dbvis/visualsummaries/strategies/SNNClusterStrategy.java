/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

import java.util.ArrayList;

import dbvis.visualsummaries.data.DataPoint;

/**
 *
 * @author jwulms
 */
public class SNNClusterStrategy extends CompLinkClusteringStrategy {

    int knn;

    public SNNClusterStrategy(int knn) {
        this.knn = knn;
    }

    @Override
    public String getName() {
        return "SNN clustering";
    }

    /**
     * Returns dataset ordered using optimal ordering of complete-linkage
     * clustering of the point set
     *
     * @param unsorted dataset
     * @return sorted dataset
     */
    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {
        DataPoint[][] result = new DataPoint[unsorted.length][unsorted[0].length];

        // find order per frame
        for (int x = 0; x < unsorted.length; x++) {

            // idx is an array of the point indexes
            int[] idx = new int[unsorted[x].length];
            // arraylist to save remaining clusters in
            ArrayList<Cluster> clusters = new ArrayList<>();
            // matrix containing distances between clusters
            ArrayList<ArrayList<Double>> distances = new ArrayList<>();
            //

            // initialize an array for the knn neighbors of each node
            ArrayList<ArrayList<DistanceTo>> nodes = new ArrayList<>();
            while (nodes.size() < unsorted[x].length) {
                nodes.add(new ArrayList<DistanceTo>());
            }

            // calculate knn nearest neighbors for each node
            for (int node = 0; node < unsorted[x].length; node++) {

                // add dummies to set up later loop
                for (int i = 0; i < knn; i++) {
                    nodes.get(node).add(new DistanceTo(-1, Double.POSITIVE_INFINITY));
                }

                // check distances to all other nodes
                for (int toNode = 0; toNode < unsorted[x].length; toNode++) {
                    if (node == toNode) {
                        continue;
                    }

                    double dist = getDistance(unsorted[x][node], unsorted[x][toNode]);

                    // check if this node is closer than any of the potential knn nearest neighbors
                    for (int i = 0; i < knn; i++) {
                        // check if this point is closer than the (at most) knn current nearest
                        // neighbors
                        if (dist > 0 && dist < nodes.get(node).get(i).getDistance()) {
                            nodes.get(node).add(i, new DistanceTo(toNode, dist));
                            nodes.get(node).remove(nodes.get(node).size() - 1);
                            break;
                        }
                    }
                }
            }

            //

            // put points in separate clusters and find distances between them
            for (int y = 0; y < unsorted[x].length; y++) {
                // collect k nearest neighbors for this element
                ArrayList<Integer> knns = new ArrayList();
                ArrayList<DistanceTo> knnDistances = nodes.get(y);
                // knn are sorted from close to far in knnDistances
                for (int i = 0; i < knnDistances.size(); i++) {
                    knns.add(knnDistances.get(i).getToID());
                }

                SNNCluster singleElement = new SNNCluster(unsorted[x][y], y, knns);
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

            // save distances between single elements to make ordering later
            ArrayList<ArrayList<Double>> similarity = deepCopy(distances);

            // make clustering
            Cluster finalCluster = completeLinkageClustering(clusters, distances);

            // calculate how hierarchical clustering should be ordered
            idx = findOrdering(finalCluster, similarity);

            // sort the result set after the cluster order
            for (int y = 0; y < unsorted[x].length; y++) {
                result[x][y] = unsorted[x][idx[y]];
            }
            System.out.println("Frame " + x + " done!");
        }

        return result;
    }

    @Override
    protected boolean tiebreakNewCostCloser(ArrayList<Cluster> clusters, int newFirst, int newSecond, int oldFirst,
            int oldSecond) {
        boolean result = false;

        SNNCluster firstNewCluster = (SNNCluster) clusters.get(newFirst);
        SNNCluster secondNewCluster = (SNNCluster) clusters.get(newSecond);
        SNNCluster firstOldCluster = (SNNCluster) clusters.get(oldFirst);
        SNNCluster secondOldCluster = (SNNCluster) clusters.get(oldSecond);

        if (firstNewCluster.getEuclDistance(secondNewCluster) < firstOldCluster.getEuclDistance(secondOldCluster)) {
            result = true;
        }

        return result;
    }

    private double getDistance(DataPoint from, DataPoint to) {
        return Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getY() - from.getY(), 2));
    }
}
