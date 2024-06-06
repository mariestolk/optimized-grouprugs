/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

/**
 * Immutable solution for dynamic programming table in ClusterStrategy
 * @author jwulms
 */
public class BestClusterSolution {
    
    private final double cost;
    private final int leftMiddle;
    private final int rightMiddle;
    
    public BestClusterSolution(double cost, int leftMiddle, int rightMiddle) {
        this.cost = cost;
        this.leftMiddle = leftMiddle;
        this.rightMiddle = rightMiddle;
    }
    
    public double getCost() {
        return cost;
    }
    
    public int getLeftMiddle() {
        return leftMiddle;
    }
    
    public int getRightMiddle() {
        return rightMiddle;
    }
}
