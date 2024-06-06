package dbvis.visualsummaries.data;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * The Dataset interface. Intended to provide a common frame for datasets from
 * different sources.
 *
 * @author Juri Buchmüller, University of Konstanz
 * <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public interface DataSet {

    public enum AvailableStatistics{};
    
    /**
     * Returns the unsorted base data, first array being frames, second the
     * points in the frame
     *
     * @return the unsorted base data
     */
    public DataPoint[][] getBaseData();

    /**
     * Returns a single frame of the unsorted base data.
     *
     * @param frameid The ID of the data frame to be returned
     * @return the DataPoint[] containing the unordered entities at the given
     * frame id
     */
    public DataPoint[] getSingleFrame(int frameid);

    /**
     * Stores the sorted data as 2D-array of DataPoints. First dimension are the
     * columns, second dimension the ordered values.
     *
     * @param data The data to be stored.
     * @param strategyID The id of the strategy the data was sorted with.
     */
    public void addOrderedData(DataPoint[][] data, String strategyID);

    /**
     * Retrieves the sorted data as double[][] by strategy identifier.
     *
     * @param strategyID The desired strategy
     * @return The data sorted according to the chosen strategy or null if the
     * data was not sorted according to the specified strategy.
     */
    public DataPoint[][] getData(String strategyID);

    /**
     * Returns the minimum value of the base dataset
     *
     * @param feature The feature for which the max value is sought
     * @return the minimum value of the base dataset
     */
    public double getMin(String feature);

    /**
     * Returns the maximum value of the base dataset
     *
     * @param feature The feature for which the min value is sought
     * @return the maximum value of the base dataset
     */
    public double getMax(String feature);

    /**
     * Returns a list of available features
     *
     * @return a list with features of the dataset
     */
    public List<String> getFeatureList();

    /**
     * Returns the deciles of the chosen feature
     *
     * @param feature The feature for which the deciles are requested
     * @return the deciles of the requested feature
     */
    public Double[] getDeciles(String feature);

    /**
     * Returns the name of the dataset.
     *
     * @return the name of the dataset.
     */
    public String getName();

    public Set<String> getCurStrats();
    
    /**
     * Returns a StrategyStatistics object holding statistics about a given
     * strategy
     *
     * @param strategy The ID of the strategy for which the Statistics are to be
     * returned
     * @return The Statistics about the given strategy ID
     */
    public StrategyStatistics getStatisticsOfStrategy(String strategy);

    /**
     * Sets a StrategyStatistics Object containing the Statistics for a given
     * Strategy
     *
     * @param statistics The Statistics Object
     * @param strategyID The Strategy ID
     */
    public void setStatisticsOfStrategy(StrategyStatistics statistics, String strategyID);
    
    /**
     * Requests the maximum value of a global statistic by its id
     * 
     * @param stat The ID of the max value to be returned
     * @return A double containing the global max value of the requested statistic
     */
    public Double getGlobalStatMax(String stat);
    
    /**
     * Requests the minimum value of a global statistic by its id
     * 
     * @param stat The ID of the min value to be returned
     * @return A double containing the global min value of the requested statistic
     */
    public Double getGlobalStatMin(String stat);

    /**
     * Sets the directions of the first principal component for every frame.
     * 
     * @param directions The direction for every frame
     * @param strategyID The ID of the strategy used to create the motionrug
     */
    public void setDirections(double[] directions, String strategyID);
    
    /**
     * Sets the eigenvalues of the dataset for every frame.
     * 
     * @param eigenvalues First and second eigenvalue for every frame.
     * @param strategyID The ID of the strategy used to create the motionrug
     */
    public void setEigenvalues(double[][] eigenvalues, String strategyID);
    
    /**
     * Requests the direction of the first principal component for a single frame.
     * 
     * @param strategyID The ID of the strategy used to create the motionrug
     * @param frameid The ID of the data frame to be returned
     * @return The direction of the first principal component for each timestep,
     * if the strategy using the first principal component has been used.
     */
    public double getDirection(String strategyID, int frameid);
    
    /**
     * Requests the eigenvalues of the dataset for a single frame.
     * 
     * @param strategyID The ID of the strategy used to create the motionrug
     * @param frameid The ID of the data frame to be returned
     * @return The eigenvalues of the dataset for each timestep,
     * if the strategy using the first principal component has been used.
     */
    public double[] getEigenvalues(String strategyID, int frameid);
    
    /**
     * Sets the KSdist values per point for every frame.
     * 
     * @param pointksdist The KSdist values per point per frame
     * @param strategyID The ID of the strategy used to create the motionrug
     */
    public void setPointKSdist(double[][] pointksdist, String strategyID);
    
    /**
     * Sets the KSrank values per point for every frame.
     * 
     * @param pointksrank The KSrank values per point per frame
     * @param strategyID The ID of the strategy used to create the motionrug
     */
    public void setPointKSrank(double[][] pointksrank, String strategyID);
    
    /**
     * Sets the KSproj values per point for every frame.
     * 
     * @param pointksproj The KSproj values per point per frame
     * @param strategyID The ID of the strategy used to create the motionrug
     */
    public void setPointKSproj(double[][] pointksproj, String strategyID);
    
    /**
     * Requests the KSdist values of all points for a single frame.
     * 
     * @param strategyID The ID of the strategy used to create the motionrug
     * @param frameid The ID of the data frame to be returned
     * @return The KSdist values of all points for frame {@ frameid}.
     */
    public double[] getPointKSdist(String strategyID, int frameid);
    
    /**
     * 
     * @param strategyID
     * @return 
     */
    public double[] getPointKSdistMinMax(String strategyID);
    
    /**
     * Requests the KSrank values of all points for a single frame.
     * 
     * @param strategyID The ID of the strategy used to create the motionrug
     * @param frameid The ID of the data frame to be returned
     * @return The KSrank values of all points for frame {@ frameid}.
     */
    public double[] getPointKSrank(String strategyID, int frameid);
    
    /**
     * 
     * @param strategyID
     * @return 
     */
    public double[] getPointKSrankMinMax(String strategyID);
    
    /**
     * Requests the KSproj values of all points for a single frame.
     * 
     * @param strategyID The ID of the strategy used to create the motionrug
     * @param frameid The ID of the data frame to be returned
     * @return The KSproj values of all points for frame {@ frameid}.
     */
    public double[] getPointKSproj(String strategyID, int frameid);
    
    /**
     * 
     * @param strategyID
     * @return 
     */
    public double[] getPointKSprojMinMax(String strategyID);
}
