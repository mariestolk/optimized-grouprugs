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
public abstract class Cluster {
    
    private ArrayList<DataPoint> elements;
    private ArrayList<Integer> indices;
    protected Cluster child1, child2;
    
    public Cluster(DataPoint element, int index) {
        elements = new ArrayList();
        elements.add(element);
        indices = new ArrayList();
        indices.add(index);
        child1 = null;
        child2 = null;
    }
    
    public Cluster(Cluster child1, Cluster child2) {
        elements = new ArrayList<>(child1.getElements());
        elements.addAll(child2.getElements());
        indices = new ArrayList<>(child1.getIndices());
        indices.addAll(child2.getIndices());
//        System.out.print("Indices in cluster: [ ");
//        for(int i : indices) {
//            System.out.print(i + " ");
//        }
//        System.out.println("]");
        this.child1 = child1;
        this.child2 = child2;
    }
    
    public ArrayList<DataPoint> getElements() {
        return elements;
    }
    
    public ArrayList<Integer> getIndices() {
        return indices;
    }
    
    public Cluster getChild1() {
        return child1;
    }
    
    public Cluster getChild2() {
        return child2;
    }
    
    protected void updateElements() {
        elements = new ArrayList<>(child1.getElements());
        elements.addAll(child2.getElements());
    }
    
    protected void updateIndices() {
        indices = new ArrayList<>(child1.getIndices());
        indices.addAll(child2.getIndices());
    }
    
    public void updateOrders() {
        updateElements();
        updateIndices();
    }
    
    public abstract double getDistance(Cluster clust);

    public Cluster getOtherChild(int index) {
        Cluster result;
        
        if(child1.getIndices().contains(index)) {
            result = child2;
        } else { // child2.getIndices().contains(index)
            result = child1;
        }
        
        return result;
    }

    public void swapChildren() {
        Cluster helper = child1;
        child1 = child2;
        child2 = helper;
    }
}
