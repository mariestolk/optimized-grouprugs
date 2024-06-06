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
public class IdMappingStrategy implements Strategy {

    @Override
    public String getName() {
        return "Order on id";
    }

    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {
        return unsorted;
    }
    
}
