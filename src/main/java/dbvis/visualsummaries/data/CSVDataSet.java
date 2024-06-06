package dbvis.visualsummaries.data;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Allows the loading of csv datasets. CSV datasets have to have the following
 * fields:
 * <ul>
 * <li>frame: needs to be an integer beginning at 0 sequentially without gaps.
 * The movement has to be muniformly sampled, in other words, for each frame, a
 * position and feature values have to be known for all movers</li>
 * <li>id: mover id. Integer starting at 0, needs to be gapless sequential</li>
 * <li>x: The x coordinate as decimal, in a cartesian coordinate system.</li>
 * <li>y: The y coordinate as decimal, in a cartesian coordinate system.</li>
 * <li>All other features need to be decimal with a . as decimal separator</li>
 * </ul>
 *
 * The csv should come with a comma as separator and <b>a header line</b> is
 * required! At the moment, all frames need to be filled with the same amount of
 * movers. No gaps are covered.
 *
 * @author Juri Buchmüller, University of Konstanz
 * <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public class CSVDataSet implements DataSet {

    private List<String> features;
    private DataPoint[][] baseData;
    private HashMap<String, Double[]> deciles;
    private HashMap<String, DataPoint[][]> orderedDataSets;
    private HashMap<String, Double> featureMins;
    private HashMap<String, Double> featureMaxs;
    private HashMap<String, StrategyStatistics> stats;
    private HashMap<String, Double> minstats;
    private HashMap<String, Double> maxstats;
    private HashMap<String, double[]> directions;
    private HashMap<String, double[][]> eigenvalues;
    private HashMap<String, double[][]> pointksdist;
    private HashMap<String, double[][]> pointksrank;
    private HashMap<String, double[][]> pointksproj;
    private String name;

    public enum AvailableStatistics {
        JUMPSMEAN, JUMPSMEDIAN, JUMPSSTDEV, CROSSMEAN, CROSSMEDIAN, CROSSSTDEV, KENDALLSMEAN, KENDALLSMEDIAN, KENDALLSSTDEV, 
    }

    /**
     *
     * @param features the list of features contained in the dataset
     * @param baseData the unordered base data of the movment
     * @param deciles a map containing deciles of the feature value ranges for
     * each feature
     * @param name the name of the dataset
     * @param featureMins the min value per feature
     * @param featureMaxs the max value per feature
     */
    public CSVDataSet(List<String> features, DataPoint[][] baseData, HashMap<String, Double[]> deciles, String name, HashMap<String, Double> featureMins, HashMap<String, Double> featureMaxs) {
        this.features = features;
        this.baseData = baseData;
        this.deciles = deciles;
        this.name = name;
        this.name = this.name.replace(".csv", "");
        orderedDataSets = new HashMap<>();
        this.featureMins = featureMins;
        this.featureMaxs = featureMaxs;
        this.stats = new HashMap<>();
        this.minstats = new HashMap<>();
        this.maxstats = new HashMap<>();
        this.directions = new HashMap<>();
        this.eigenvalues = new HashMap<>();
        this.pointksdist = new HashMap<>();
        this.pointksrank = new HashMap<>();
        this.pointksproj = new HashMap<>();
    }

    /**
     * @return the base data
     */
    @Override
    public DataPoint[][] getBaseData() {
        return baseData;
    }

    @Override
    public DataPoint[] getSingleFrame(int frameid) {
        return baseData[frameid];
    }

    /**
     * Stores results of applied ordering strategies separately
     *
     * @param data the ordered data to store
     * @param strategyID the id of the strategy the data was ordered with
     */
    @Override
    public void addOrderedData(DataPoint[][] data, String strategyID) {
        if (data == null) {
            System.out.println("DATA IS NULL");
        }
        if (strategyID == null) {
            System.out.println("STRATID IS NULL");
        }
        orderedDataSets.put(strategyID, data);
    }

    /**
     * Returns the ordered data
     *
     * @param strategyID the strategy for which ordered data is returned
     * @return the ordered data
     */
    @Override
    public DataPoint[][] getData(String strategyID) {
        //System.out.println("TRYING TO GET DATA. INPUT ID: " + strategyID);
        //System.out.println("EXISTING KEYS: " + orderedDataSets.keySet());
        return orderedDataSets.get(strategyID);
    }
    
    @Override
    public Set<String> getCurStrats(){
        return orderedDataSets.keySet();
    }

    /**
     * Returns the min value of the requested feature
     *
     * @param featureid the feature for which the min value is requested
     * @return the min value for the requested feature
     */
    @Override
    public double getMin(String featureid) {
        return featureMins.get(featureid);
    }

    /**
     * Returns the max value of the requested feature
     *
     * @param featureid the feature for which the max value is requested
     * @return the min value for the requested feature
     */
    @Override
    public double getMax(String featureid) {
        return featureMaxs.get(featureid);
    }

    /**
     * Returns the list of available features in the Dataset.
     *
     * @return the ist of available features
     */
    @Override
    public List<String> getFeatureList() {
        return features;
    }

    /**
     * Returns the deciles of a requested feature
     *
     * @param feature the feature for which the deciles are requested
     * @return the deciles for the requested feature
     */
    @Override
    public Double[] getDeciles(String feature) {
        return deciles.get(feature);
    }

    /**
     * Returns the name of the dataset
     *
     * @return the name of the dataset
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public StrategyStatistics getStatisticsOfStrategy(String strategy) {
        return stats.get(strategy);
    }

    /**
     * Adds the statistics for a strategy. Updates the dataset's
     * min/max-Statistics
     *
     * @param statistics the StrategyStatistics object to be set
     * @param strategyID the od of the strategy for which the StrategyStatistics
     * apply
     */
    @Override
    public void setStatisticsOfStrategy(StrategyStatistics statistics, String strategyID) {
        if (!minstats.containsKey("jumpsmean") || minstats.get("jumpsmean") > statistics.getJumpsmean()) {
            minstats.put("jumpsmean", statistics.getJumpsmean());
        }
        if (!minstats.containsKey("jumpsmedian") || minstats.get("jumpsmedian") > statistics.getJumpsmedian()) {
            minstats.put("jumpsmedian", statistics.getJumpsmedian());
        }
        if (!minstats.containsKey("jumpsstdev") || minstats.get("jumpsstdev") > statistics.getJumpsstddev()) {
            minstats.put("jumpsstdev", statistics.getJumpsstddev());
        }

        if (!minstats.containsKey("crossmean") || minstats.get("crossmean") > statistics.getCrossmean()) {
            minstats.put("crossmean", statistics.getCrossmean());
        }
        if (!minstats.containsKey("crossmedian") || minstats.get("crossmedian") > statistics.getCrossmedian()) {
            minstats.put("crossmedian", statistics.getCrossmedian());
        }
        if (!minstats.containsKey("crossstdev") || minstats.get("crossstdev") > statistics.getCrossstddev()) {
            minstats.put("crossstdev", statistics.getCrossstddev());
        }

        if (!minstats.containsKey("kendallsmean") || minstats.get("kendallsmean") > statistics.getKendallsmean()) {
            minstats.put("kendallsmean", statistics.getKendallsmean());
        }
        if (!minstats.containsKey("kendallsmedian") || minstats.get("kendallsmedian") > statistics.getKendallsmedian()) {
            minstats.put("kendallsmedian", statistics.getKendallsmedian());
        }
        if (!minstats.containsKey("kendallsstdev") || minstats.get("kendallsstdev") > statistics.getKendallsstddev()) {
            minstats.put("kendallsstdev", statistics.getKendallsstddev());
        }
        
        if (!minstats.containsKey("KSdistmean") || minstats.get("KSdistmean") > statistics.getKSdistmean()) {
            minstats.put("KSdistmean", statistics.getKSdistmean());
        }
        if (!minstats.containsKey("KSdistmedian") || minstats.get("KSdistmedian") > statistics.getKSdistmedian()) {
            minstats.put("KSdistmedian", statistics.getKSdistmedian());
        }
        if (!minstats.containsKey("KSdiststdev") || minstats.get("KSdiststdev") > statistics.getKSdiststddev()) {
            minstats.put("KSdiststdev", statistics.getKSdiststddev());
        }
        
        if (!minstats.containsKey("KSrankmean") || minstats.get("KSrankmean") > statistics.getKSrankmean()) {
            minstats.put("KSrankmean", statistics.getKSrankmean());
        }
        if (!minstats.containsKey("KSrankmedian") || minstats.get("KSrankmedian") > statistics.getKSrankmedian()) {
            minstats.put("KSrankmedian", statistics.getKSrankmedian());
        }
        if (!minstats.containsKey("KSrankstdev") || minstats.get("KSrankstdev") > statistics.getKSrankstddev()) {
            minstats.put("KSrankstdev", statistics.getKSrankstddev());
        }
        
        if (!minstats.containsKey("KSprojmean") || minstats.get("KSprojmean") > statistics.getKSprojmean()) {
            minstats.put("KSprojmean", statistics.getKSprojmean());
        }
        if (!minstats.containsKey("KSprojmedian") || minstats.get("KSprojmedian") > statistics.getKSprojmedian()) {
            minstats.put("KSprojmedian", statistics.getKSprojmedian());
        }
        if (!minstats.containsKey("KSprojstdev") || minstats.get("KSprojstdev") > statistics.getKSprojstddev()) {
            minstats.put("KSprojstdev", statistics.getKSprojstddev());
        }
        
        if (!minstats.containsKey("KSdistInputmean") || minstats.get("KSdistInputmean") > statistics.getKSdistInputmean()) {
            minstats.put("KSdistInputmean", statistics.getKSdistInputmean());
        }
        if (!minstats.containsKey("KSdistInputmedian") || minstats.get("KSdistInputmedian") > statistics.getKSdistInputmedian()) {
            minstats.put("KSdistInputmedian", statistics.getKSdistInputmedian());
        }
        if (!minstats.containsKey("KSdistInputstdev") || minstats.get("KSdistInputstdev") > statistics.getKSdistInputstddev()) {
            minstats.put("KSdistInputstdev", statistics.getKSdistInputstddev());
        }
        
        if (!minstats.containsKey("KSrankInputmean") || minstats.get("KSrankInputmean") > statistics.getKSrankInputmean()) {
            minstats.put("KSrankInputmean", statistics.getKSrankInputmean());
        }
        if (!minstats.containsKey("KSrankInputmedian") || minstats.get("KSrankInputmedian") > statistics.getKSrankInputmedian()) {
            minstats.put("KSrankInputmedian", statistics.getKSrankInputmedian());
        }
        if (!minstats.containsKey("KSrankInputstdev") || minstats.get("KSrankInputstdev") > statistics.getKSrankInputstddev()) {
            minstats.put("KSrankInputstdev", statistics.getKSrankInputstddev());
        }

        if (!maxstats.containsKey("jumpsmean") || maxstats.get("jumpsmean") < statistics.getJumpsmean()) {
            maxstats.put("jumpsmean", statistics.getJumpsmean());
        }
        if (!maxstats.containsKey("jumpsmedian") || maxstats.get("jumpsmedian") < statistics.getJumpsmedian()) {
            maxstats.put("jumpsmedian", statistics.getJumpsmedian());
        }
        if (!maxstats.containsKey("jumpsstdev") || maxstats.get("jumpsstdev") < statistics.getJumpsstddev()) {
            maxstats.put("jumpsstdev", statistics.getJumpsstddev());
        }

        if (!maxstats.containsKey("crossmean") || maxstats.get("crossmean") < statistics.getCrossmean()) {
            maxstats.put("crossmean", statistics.getCrossmean());
        }
        if (!maxstats.containsKey("crossmedian") || maxstats.get("crossmedian") < statistics.getCrossmedian()) {
            maxstats.put("crossmedian", statistics.getCrossmedian());
        }
        if (!maxstats.containsKey("crossstdev") || maxstats.get("crossstdev") < statistics.getCrossstddev()) {
            maxstats.put("crossstdev", statistics.getCrossstddev());
        }
        
        if (!maxstats.containsKey("kendallsmean") || maxstats.get("kendallsmean") < statistics.getKendallsmean()) {
            maxstats.put("kendallsmean", statistics.getKendallsmean());
        }
        if (!maxstats.containsKey("kendallsmedian") || maxstats.get("kendallsmedian") < statistics.getKendallsmedian()) {
            maxstats.put("kendallsmedian", statistics.getKendallsmedian());
        }
        if (!maxstats.containsKey("kendallsstdev") || maxstats.get("kendallsstdev") < statistics.getKendallsstddev()) {
            maxstats.put("kendallsstdev", statistics.getKendallsstddev());
        }
        
        if (!maxstats.containsKey("KSdistmean") || maxstats.get("KSdistmean") < statistics.getKSdistmean()) {
            maxstats.put("KSdistmean", statistics.getKSdistmean());
        }
        if (!maxstats.containsKey("KSdistmedian") || maxstats.get("KSdistmedian") < statistics.getKSdistmedian()) {
            maxstats.put("KSdistmedian", statistics.getKSdistmedian());
        }
        if (!maxstats.containsKey("KSdiststdev") || maxstats.get("KSdiststdev") < statistics.getKSdiststddev()) {
            maxstats.put("KSdiststdev", statistics.getKSdiststddev());
        }
        
        if (!maxstats.containsKey("KSrankmean") || maxstats.get("KSrankmean") < statistics.getKSrankmean()) {
            maxstats.put("KSrankmean", statistics.getKSrankmean());
        }
        if (!maxstats.containsKey("KSrankmedian") || maxstats.get("KSrankmedian") < statistics.getKSrankmedian()) {
            maxstats.put("KSrankmedian", statistics.getKSrankmedian());
        }
        if (!maxstats.containsKey("KSrankstdev") || maxstats.get("KSrankstdev") < statistics.getKSrankstddev()) {
            maxstats.put("KSrankstdev", statistics.getKSrankstddev());
        }
        
        if (!maxstats.containsKey("KSprojmean") || maxstats.get("KSprojmean") < statistics.getKSprojmean()) {
            maxstats.put("KSprojmean", statistics.getKSprojmean());
        }
        if (!maxstats.containsKey("KSprojmedian") || maxstats.get("KSprojmedian") < statistics.getKSprojmedian()) {
            maxstats.put("KSprojmedian", statistics.getKSprojmedian());
        }
        if (!maxstats.containsKey("KSprojstdev") || maxstats.get("KSprojstdev") < statistics.getKSprojstddev()) {
            maxstats.put("KSprojstdev", statistics.getKSprojstddev());
        }
        
        if (!maxstats.containsKey("KSdistInputmean") || maxstats.get("KSdistInputmean") < statistics.getKSdistInputmean()) {
            maxstats.put("KSdistInputmean", statistics.getKSdistInputmean());
        }
        if (!maxstats.containsKey("KSdistInputmedian") || maxstats.get("KSdistInputmedian") < statistics.getKSdistInputmedian()) {
            maxstats.put("KSdistInputmedian", statistics.getKSdistInputmedian());
        }
        if (!maxstats.containsKey("KSdistInputstdev") || maxstats.get("KSdistInputstdev") < statistics.getKSdistInputstddev()) {
            maxstats.put("KSdistInputstdev", statistics.getKSdistInputstddev());
        }
        
        if (!maxstats.containsKey("KSrankInputmean") || maxstats.get("KSrankInputmean") < statistics.getKSrankInputmean()) {
            maxstats.put("KSrankInputmean", statistics.getKSrankInputmean());
        }
        if (!maxstats.containsKey("KSrankInputmedian") || maxstats.get("KSrankInputmedian") < statistics.getKSrankInputmedian()) {
            maxstats.put("KSrankInputmedian", statistics.getKSrankInputmedian());
        }
        if (!maxstats.containsKey("KSrankInputstdev") || maxstats.get("KSrankInputstdev") < statistics.getKSrankInputstddev()) {
            maxstats.put("KSrankInputstdev", statistics.getKSrankInputstddev());
        }
        this.stats.put(strategyID, statistics);
    }

    @Override
    public Double getGlobalStatMax(String stat) {
        return maxstats.get(stat);
    }

    @Override
    public Double getGlobalStatMin(String stat) {
        return minstats.get(stat);
    }
    
    public void setDirections(double[] directions, String strategyID) {
        this.directions.put(strategyID, directions);
    }
    
    public void setEigenvalues(double[][] eigenvalues, String strategyID) {
        this.eigenvalues.put(strategyID, eigenvalues);
    }

    @Override
    public double getDirection(String strategyID, int frameid) {
        return directions.get(strategyID)[frameid];
    }

    @Override
    public double[] getEigenvalues(String strategyID, int frameid) {
        return eigenvalues.get(strategyID)[frameid];
    }
    
    @Override
    public void setPointKSdist(double[][] pointksdist, String strategyID) {
        this.pointksdist.put(strategyID, pointksdist);
    }

    @Override
    public void setPointKSrank(double[][] pointksrank, String strategyID) {
        this.pointksrank.put(strategyID, pointksrank);
    }

    @Override
    public void setPointKSproj(double[][] pointksproj, String strategyID) {
        this.pointksproj.put(strategyID, pointksproj);
    }

    @Override
    public double[] getPointKSdist(String strategyID, int frameid) {
        return pointksdist.get(strategyID)[frameid];
    }
    
    @Override
    public double[] getPointKSdistMinMax(String strategyID) {
        double[] result = new double[2];
        double[][] values = pointksdist.get(strategyID);
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (int frame = 0; frame < values.length; frame++) {
            for (int id = 0; id < values[frame].length; id++) {
                if (values[frame][id] < min) {
                    min = values[frame][id];
                }
                if (values[frame][id] > max) {
                    max = values[frame][id];
                }
            }
        }
        
        result[0] = min;
        result[1] = max;
        
        return result;
    }

    @Override
    public double[] getPointKSrank(String strategyID, int frameid) {
        return pointksrank.get(strategyID)[frameid];
    }
    
    @Override
    public double[] getPointKSrankMinMax(String strategyID) {
        double[] result = new double[2];
        double[][] values = pointksrank.get(strategyID);
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (int frame = 0; frame < values.length; frame++) {
            for (int id = 0; id < values[frame].length; id++) {
                if (values[frame][id] < min) {
                    min = values[frame][id];
                }
                if (values[frame][id] > max) {
                    max = values[frame][id];
                }
            }
        }
        
        result[0] = min;
        result[1] = max;
        
        return result;
    }

    @Override
    public double[] getPointKSproj(String strategyID, int frameid) {
        return pointksproj.get(strategyID)[frameid];
    }
    
    @Override
    public double[] getPointKSprojMinMax(String strategyID) {
        double[] result = new double[2];
        double[][] values = pointksproj.get(strategyID);
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (int frame = 1; frame < values.length; frame++) {
            for (int id = 0; id < values[frame].length; id++) {
                if (values[frame][id] < min) {
                    min = values[frame][id];
                }
                if (values[frame][id] > max) {
                    max = values[frame][id];
                }
            }
        }
        
        result[0] = min;
        result[1] = max;
        
        return result;
    }

}
