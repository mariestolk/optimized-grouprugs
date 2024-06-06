/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

/**
 *
 * @author jwulms
 */
public class DistanceTo {
    
    private final int toID;
    private final double distance;
    
    public DistanceTo(int toID, double distance) {
        this.toID = toID;
        this.distance = distance;
    }
    
    public int getToID() {
        return toID;
    }
    
    public double getDistance() {
        return distance;
    }
}
