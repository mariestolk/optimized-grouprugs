/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.data;

/**
 *
 * @author jwulms
 */
public class QualityStats {
    
    private final int toID;
    private final double distance;
    private final int orderRank;
    
    public QualityStats(int toID, double distance, int orderRank) {
        this.toID = toID;
        this.distance = distance;
        this.orderRank = orderRank;
    }
    
    public int getToID() {
        return toID;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public int getOrderRank() {
        return orderRank;
    }
}
