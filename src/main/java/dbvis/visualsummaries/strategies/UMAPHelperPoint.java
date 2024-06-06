/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

/**
 *
 * @author buchmueller
 */
public class UMAPHelperPoint {
    
    private int id;
    private double umapvalue;

    public UMAPHelperPoint(int id, double umapvalue) {
        this.id = id;
        this.umapvalue = umapvalue;
    }

    public int getId() {
        return id;
    }

    public double getUmapvalue() {
        return umapvalue;
    }
    
}
