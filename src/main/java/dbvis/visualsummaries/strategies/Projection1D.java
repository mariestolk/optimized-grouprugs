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
public class Projection1D implements Comparable {
    
    private final int identifier;
    public double projection;
    
    public Projection1D(int id, double value) {
        this.identifier = id;
        this.projection = value;
    }
    
    public int getID() {
        return identifier;
    }
    
    public double getProjection() {
        return projection;
    }
    
    public void setProjection(double value) {
        projection = value;
    }
    
    public void addToProjection(double value) {
        projection += value;
    }

    @Override
    public int compareTo(Object o) {
        Projection1D other = (Projection1D) o;
        return Double.compare(projection, other.getProjection());
    }
}
