package dbvis.visualsummaries.data;

import java.util.HashMap;
import java.util.Set;

/**
 * Stores the data during execution
 *
 * @author Juri Buchmüller, University of Konstanz
 * <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public class SessionData {
    
    private static SessionData instance;
    private HashMap<String, DataSet> datasets;
    private String currentDataSet;
    
    private SessionData(){
        datasets = new HashMap<>();
    }
    
    public static SessionData getInstance(){
        if(instance==null){
            instance = new SessionData();
        }
        return instance;
    }  
    
    public Set<String> getDatasetNames(){
        return datasets.keySet();
    }

    public DataSet getDataset(String name) {
        if(datasets.get(name)==null)System.out.println("NUHULL");
        return datasets.get(name);
    }

    public void addDataset(DataSet dataset) {
        datasets.put(dataset.getName(), dataset);
    }
    
    
    public void addOrderedData(String datasetname, String strategyname, DataPoint[][] ordered){
        System.out.println("Adding ordered data:" + datasetname + " " + strategyname + " " + ordered.length);
        DataSet tochange = datasets.get(datasetname);
        System.out.println("dataset in session is not null: " + (tochange != null));
        tochange.addOrderedData(ordered, strategyname);
        
        
        datasets.put(datasetname, tochange);
    }

    public String getCurrentDataSetID() {
        return currentDataSet;
    }
    
    public DataSet getCurrentDataSet() {
        return datasets.get(currentDataSet);
    }

    public void setCurrentDataSet(String currentDataSet) {
        this.currentDataSet = currentDataSet;
        
    }
 
}
