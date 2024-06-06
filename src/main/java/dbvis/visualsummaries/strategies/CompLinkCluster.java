/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

import dbvis.visualsummaries.data.DataPoint;

/**
 *
 * @author jwulms
 */
public class CompLinkCluster extends Cluster {

    public CompLinkCluster(DataPoint element, int index) {
        super(element, index);
    }
    
    public CompLinkCluster(Cluster child1, Cluster child2) {
        super(child1, child2);
    }
    
    @Override
    public double getDistance(Cluster clust) {
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
