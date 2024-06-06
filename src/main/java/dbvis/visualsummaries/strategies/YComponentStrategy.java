/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

import java.util.Arrays;
import java.util.Comparator;

import dbvis.visualsummaries.data.DataPoint;

/**
 * Y component strategy
 * 
 * @author Jules Wulms, TU Eindhoven <j.j.h.m.wulms@tue.nl>
 */
public class YComponentStrategy implements Strategy {

    @Override
    public String getName() {
        return "Y component";
    }

    /**
     * Returns dataset ordered along the first principal component (at every point in time)
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
            Integer[] idx = new Integer[unsorted[x].length];
            //store x coordinates to sort on them
            double[] ycoordinate = new double[unsorted[x].length];

            //project points to principal component and store projection variable
            for (int y = 0; y < unsorted[x].length; y++) {
                idx[y] = y;
                ycoordinate[y] = unsorted[x][y].getY();
            }

            //sort the index array with comparing the projection scalars 
            Arrays.sort(idx, new Comparator<Integer>() {
                @Override
                public int compare(final Integer o1, final Integer o2) {
                    return Double.compare(ycoordinate[o1], ycoordinate[o2]);
                    
                }
            });

            //sort the result set after the projection order 
            for (int y = 0; y < unsorted[x].length; y++) {
                result[x][y] = unsorted[x][idx[y]];
            }
        }
        return result;
    }
}
