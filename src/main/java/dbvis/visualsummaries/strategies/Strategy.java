package dbvis.visualsummaries.strategies;

import dbvis.visualsummaries.data.DataPoint;

/**
 * The Strategy interface. Strategies must provide a method to order arrays of
 * DataPoints.
 *
 * @author Juri Buchmüller, University of Konstanz
 *         <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public interface Strategy {

    /**
     * Retrieves the strategies name.
     *
     * @return the name of the strategy.
     */
    public String getName();

    /**
     * Sorts unordered values
     * 
     * @param unsorted The unsorted data points in format [frame][entity]
     * @return
     */
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName);

}
