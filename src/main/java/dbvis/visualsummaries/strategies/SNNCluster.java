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
public class SNNCluster extends Cluster {
    
    /**
     * Stored k nearest neighbors of every element in cluster such that
     * for element {@code elements.get(i)} the knns are stored in {@code kNearestNeighbors.get(i)}.
     * Similarly, for the index in {@code indices.get(i)} corresponds to the element of which
     * the nearest neighbors are stored in {@code kNearestNeighbors.get(i)}.
     */
    private ArrayList<ArrayList<Integer>> kNearestNeighbors;
    
    public SNNCluster(DataPoint element, int index, ArrayList<Integer> knns) {
        super(element, index);
        
        kNearestNeighbors = new ArrayList();
        kNearestNeighbors.add(knns);
    }
    
    public SNNCluster(SNNCluster child1, SNNCluster child2) {
        super(child1, child2);
        
        kNearestNeighbors = new ArrayList<>(child1.getKNNs());
        kNearestNeighbors.addAll(child2.getKNNs());
    }
    
    public ArrayList<ArrayList<Integer>> getKNNs() {
        return kNearestNeighbors;
    }
    
    protected void updateKNNs() {
        SNNCluster SNNchild1 = (SNNCluster) child1;
        SNNCluster SNNchild2 = (SNNCluster) child2;
        
        kNearestNeighbors = new ArrayList<>(SNNchild1.getKNNs());
        kNearestNeighbors.addAll(SNNchild2.getKNNs());
    }
    
    @Override
    public void updateOrders() {
        updateElements();
        updateIndices();
        updateKNNs();
    }
    
    @Override
    public double getDistance(Cluster clust) {
        double result = Double.MIN_VALUE;
        
        SNNCluster cluster = (SNNCluster) clust;
        
        for(int i = 0; i < this.getIndices().size(); i++) {
            for(int j = 0; j < cluster.getIndices().size(); j++) {
                int snn = 0;
                
                //find number of shared nearest neighbors in the knn nearest neighbors of these points
                for(int myKNN : kNearestNeighbors.get(i)) {
                    if(cluster.getKNNs().get(j).contains(myKNN)) {
                        snn++;
                    }
                }
                
                //check if in each other's knn
                if(cluster.getKNNs().get(j).contains(this.getIndices().get(i)) && kNearestNeighbors.get(i).contains(cluster.getIndices().get(j))) {
                    snn++;
                }
                
                //create distance/dissimilarity measure
                double dist = 1 / (snn + 1);
                
                if(dist > result) {
                    result = dist;
                }
            }
        }
        
        return result;
    }
    
    public double getEuclDistance(Cluster clust) {
        double result = Double.MIN_VALUE;
        
        for(DataPoint element : this.getElements()) {
            for(DataPoint element2 : clust.getElements()) {
                double x2 = Math.pow(element.getX() - element2.getX(), 2);
                double y2 = Math.pow(element.getY() - element2.getY(), 2);
                double dist = Math.sqrt(x2 + y2);
                
                if(dist > result) {
                    result = dist;
                }
            }
        }
        
        return result;
    }
    
}
