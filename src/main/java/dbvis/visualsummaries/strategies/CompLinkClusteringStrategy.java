/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

import java.util.ArrayList;

import dbvis.visualsummaries.data.DataPoint;

/**
 * Complete-linkage clustering strategy with optimal ordering
 *
 * @author Jules Wulms, TU Eindhoven <j.j.h.m.wulms@tue.nl>
 */
public class CompLinkClusteringStrategy extends ClusterStrategy {

    @Override
    public String getName() {
        return "Complete-linkage clustering";
    }

    /**
     * Returns dataset ordered using optimal ordering of complete-linkage clustering of the point set
     *
     * @param unsorted dataset
     * @return sorted dataset
     */
    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {
        DataPoint[][] result = new DataPoint[unsorted.length][unsorted[0].length];

        //find order per frame
        for (int x = 0; x < unsorted.length; x++) {
            
            //idx is an array of the point indexes
            int[] idx = new int[unsorted[x].length];
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

            //save distances between single elements to make ordering later
            ArrayList<ArrayList<Double>> similarity = deepCopy(distances);

            //make clustering
            Cluster finalCluster = completeLinkageClustering(clusters, distances);

            //calculate how hierarchical clustering should be ordered
            idx = findOrdering(finalCluster, similarity);

            //sort the result set after the cluster order 
            for (int y = 0; y < unsorted[x].length; y++) {
                result[x][y] = unsorted[x][idx[y]];
            }
            System.out.println("Frame " + x + " done!");
        }

        return result;
    }

    public Cluster completeLinkageClustering(ArrayList<Cluster> clusters, ArrayList<ArrayList<Double>> distances) {
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
            
            //merge the two closest clusters
            Cluster merged;
            if(clusters.get(first) instanceof SNNCluster && clusters.get(second) instanceof SNNCluster) {
                SNNCluster child1 = (SNNCluster) clusters.get(first);
                SNNCluster child2 = (SNNCluster) clusters.get(second);
                merged = new SNNCluster(child1, child2);
            } else { // clusters.get(first) instanceof CompLinkCluster
                merged = new CompLinkCluster(clusters.get(first), clusters.get(second));
            }
            clusters.add(merged);
            //remove merged clusters
            clusters.remove(first);
            clusters.remove(second-1);

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
            distances.remove(second-1);
        }

        return clusters.get(0);
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
            distances.get(old).remove(secondMerged-1);
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

    protected boolean tiebreakNewCostCloser(ArrayList<Cluster> clusters, int newFirst, int newSecond, int oldFirst, int oldSecond) {
        return false;
    }
}
