package dbvis.visualsummaries.grouprugs.linearprogram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.gurobi.gurobi.GRBException;

import dbvis.visualsummaries.data.CSVDataLoader;
import dbvis.visualsummaries.grouprugs.tgs.TGS;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroup;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroupComputer;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.REdge;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraph;
import dbvis.visualsummaries.grouprugs.visualization.groupselectionstrategies.MaximalDurationStrategy;
import javafx.util.Pair;

/**
 * This class is responsible for converting the ReebGraph and the maximal groups
 * to an MLCM-TC instance, then solving the ILP to obtain the order of the
 * maximal groups in the ReebGraph to avoid crossings.
 */
public class ReebGraphOrderingPipeline {

    Map<Integer, MaximalGroup> IDToGroupMap;
    // TreeMap<Integer, List<Integer>> orderedMGroups;
    List<MGOrder> orderedMGroups;
    List<Integer> layers;

    public ReebGraphOrderingPipeline() {
        this.IDToGroupMap = new HashMap<Integer, MaximalGroup>();
        this.orderedMGroups = new ArrayList<>();
        this.layers = new ArrayList<Integer>();
    }

    /**
     * This method is the main entry point for the ReebGraph ordering pipeline.
     * 
     * @param rg            The ReebGraph to be ordered.
     * @param maximalgroups The maximal groups to be ordered.
     * @return A TreeMap containing the ordered maximal groups per layer.
     * @throws GRBException
     */
    public void run(ReebGraph rg, Set<MaximalGroup> maximalgroups)
            throws GRBException {

        // Handle edge case where there is only 1 maximal duration group
        if (maximalgroups.size() == 1) {

            // create list of integer with the maximal group id = 0
            List<Integer> mGroup = new java.util.ArrayList<Integer>();
            mGroup.add(0);

            // create a TreeMap with the layer 0 and the list of maximal group id = 0
            // orderedMGroups.put(0, mGroup);
            orderedMGroups.add(new MGOrder(0, mGroup));
            IDToGroupMap.put(0, maximalgroups.iterator().next());
            layers.add(0);

            System.out.println("Only 1 maximal group, no need to solve ILP");
            return;
        }

        // Convert Reeb graph to MLCM-TC instance
        MLCMGraph mlcmtc = ReebToMLCM.convert(rg, maximalgroups);
        this.IDToGroupMap = mlcmtc.getIDToGroupMap();
        this.layers = mlcmtc.getLayers();

        // Compute the decision variables
        Map<Integer, Map<Pair<Integer, Integer>, Boolean>> decisionVariables = QP.compute(mlcmtc);

        // Compute the order of the maximal groups
        QPToOrder qto = new QPToOrder();
        qto.computeOrder(decisionVariables);

        // TreeMap of Integer (layer) to List of Integer (ordered maximal groups by ID)
        List<MGOrder> orderChanges = qto.getOrderedVertices();
        // TreeMap<Integer, List<Integer>> orderedMGroups = qto.getOrderedVertices();
        this.orderedMGroups = orderChanges;

    }

    public Map<Integer, MaximalGroup> getGroupMap() {
        return IDToGroupMap;
    }

    public void setGroupMap(Map<Integer, MaximalGroup> IDToGroupMap) {
        this.IDToGroupMap = IDToGroupMap;
    }

    public List<Integer> getLayers() {
        return layers;
    }

    public void setLayers(List<Integer> layers) {
        this.layers = layers;
    }

    public List<MGOrder> getOrderedMGroups() {
        return orderedMGroups;
    }

    public void setOrderedMGroups(List<MGOrder> orderedMGroups) {
        this.orderedMGroups = orderedMGroups;
    }

    public static void main(String[] args) throws GRBException {

        // Load data
        String[] datapath = new String[1];
        datapath[0] = "data/";
        CSVDataLoader.checkAndLoadCSVDataSets(datapath);

        String filename = "fishdatamerge";

        /*
         * fishdata: EPSILON == 400d
         * fishdatamerge: EPSILON == 250d
         * 4clustersextracted: EPSILON == 200d
         * 
         * mergeFocus: EPSILON == 15d
         */
        double EPSILON = 250;
        double DELTA = 1d;
        int M = 1;

        String file = filename + "_eps" + EPSILON;

        TGS tgs = new TGS(EPSILON, DELTA, M, filename);
        ReebGraph rg = tgs.compute();

        // Compute maximal groups
        MaximalGroupComputer.compute(rg);
        Set<MaximalGroup> maximalgroups = MaximalDurationStrategy.selectGroups(rg);

        String TreeMapPath = file + "_treeMap.txt";
        String GroupMapPath = file + "_groupMap.txt";
        String LayersPath = file + "_layers.txt";

        // Check if corresponding files exist
        java.io.File TreeMapFile = new java.io.File("orderedGroups/" + TreeMapPath);
        java.io.File GroupMapFile = new java.io.File("orderedGroups/" + GroupMapPath);
        java.io.File LayersFile = new java.io.File("orderedGroups/" + LayersPath);

        // Find maximum timestamp
        int maxTimestamp = 0;
        for (REdge e : rg.getEdges()) {
            if (e.getDest().getFrame() > maxTimestamp) {
                maxTimestamp = e.getDest().getFrame();
            }
        }

        ReebGraphOrderingPipeline rgop = new ReebGraphOrderingPipeline();

        // If files exist, read from files
        if (TreeMapFile.exists() && GroupMapFile.exists() && LayersFile.exists()) {

            List<MGOrder> orderedMGroups = rgop.readTreeMapFromFile(TreeMapFile.getName());
            // TreeMap<Integer, List<Integer>> orderedMGroups =
            // rgop.readTreeMapFromFile(TreeMapFile.getName());
            Map<Integer, MaximalGroup> IDToGroupMap = rgop.readGroupMapFromFile(GroupMapFile.getName(), maxTimestamp);
            List<Integer> layers = rgop.readLayersFromFile(LayersFile.getName());

            // Set the ordered maximal groups per layer and the group map
            rgop.orderedMGroups = orderedMGroups;
            rgop.IDToGroupMap = IDToGroupMap;
            rgop.layers = layers;

        } else {

            // Run the ReebGraph ordering pipeline
            rgop.run(rg, maximalgroups);

            // Get the ordered maximal groups per layer and the group map
            List<MGOrder> orderedMGroups = rgop.getOrderedMGroups();
            Map<Integer, MaximalGroup> IDToGroupMap = rgop.getGroupMap();
            List<Integer> layers = rgop.getLayers();

            // Write to file
            rgop.writeTreeMapToFile(orderedMGroups, file);
            rgop.writeGroupMapToFile(IDToGroupMap, file);
            rgop.writeLayersToFile(layers, file);

        }

        System.out.println("Done");

    }

    public void writeTreeMapToFile(
            List<MGOrder> orderedMGroups, String filename) {

        // Check if directory exists
        java.io.File directory = new java.io.File("orderedGroups");
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Create txt file in root directory/orderedGroups
        String path = "orderedGroups/" + filename + "_treeMap.txt";

        // Write to file
        try {
            java.io.FileWriter myWriter = new java.io.FileWriter(path);

            for (MGOrder orderChange : orderedMGroups) {

                int layer = orderChange.getLayer();
                List<Integer> orderedVertices = orderChange.getOrder();

                myWriter.write("Layer " + layer + ": ");
                for (int i = 0; i < orderedVertices.size(); i++) {
                    myWriter.write(orderedVertices.get(i) + " ");
                }
                myWriter.write("\n");

            }

            myWriter.close();

        } catch (java.io.IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void writeGroupMapToFile(Map<Integer, MaximalGroup> groupMapReverse, String filename) {

        // Check if directory exists
        java.io.File directory = new java.io.File("orderedGroups");
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Create txt file in root directory/orderedGroups
        String path = "orderedGroups/" + filename + "_groupMap.txt";

        // Write to file
        try {
            java.io.FileWriter myWriter = new java.io.FileWriter(path);

            for (Map.Entry<Integer, MaximalGroup> entry : groupMapReverse.entrySet()) {

                int id = entry.getKey();
                MaximalGroup group = entry.getValue();

                myWriter.write("ID: " + id + " Group: " + group.entitiesToString() + "\n");

            }

            myWriter.close();

        } catch (java.io.IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();

        }

    }

    public void writeLayersToFile(List<Integer> layers, String filename) {

        // Check if directory exists
        java.io.File directory = new java.io.File("orderedGroups");
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Create txt file in root directory/orderedGroups
        String path = "orderedGroups/" + filename + "_layers.txt";

        // Write to file
        try {
            java.io.FileWriter myWriter = new java.io.FileWriter(path);

            for (int i = 0; i < layers.size(); i++) {
                myWriter.write(layers.get(i) + " ");
            }

            myWriter.close();

        } catch (java.io.IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();

        }

    }

    public List<MGOrder> readTreeMapFromFile(String filename) {

        List<MGOrder> orderedMGroups = new ArrayList<>();

        // Read from file
        try {
            java.io.File myObj = new java.io.File("orderedGroups/" + filename);
            java.util.Scanner myReader = new java.util.Scanner(myObj);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] split = data.split(" ");
                int layer = Integer.parseInt(split[1].substring(0, split[1].length() - 1));
                List<Integer> orderedVertices = new ArrayList<Integer>();

                for (int i = 2; i < split.length; i++) {
                    orderedVertices.add(Integer.parseInt(split[i]));
                }

                orderedMGroups.add(new MGOrder(layer, orderedVertices));

            }

            myReader.close();

        } catch (java.io.FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return orderedMGroups;
    }

    public Map<Integer, MaximalGroup> readGroupMapFromFile(String filename, int endFrame) {

        Map<Integer, MaximalGroup> groupMapReverse = new TreeMap<Integer, MaximalGroup>();

        // Read from file
        try {
            java.io.File myObj = new java.io.File("orderedGroups/" + filename);
            java.util.Scanner myReader = new java.util.Scanner(myObj);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] split = data.split(" ");
                int id = Integer.parseInt(split[1]);
                String groupString = split[3];
                MaximalGroup group = new MaximalGroup(groupString, 0, endFrame);

                groupMapReverse.put(id, group);

            }

            myReader.close();

        } catch (java.io.FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();

        }

        return groupMapReverse;
    }

    public List<Integer> readLayersFromFile(String filename) {

        List<Integer> layers = new ArrayList<Integer>();

        // Read from file
        try {
            java.io.File myObj = new java.io.File("orderedGroups/" + filename);
            java.util.Scanner myReader = new java.util.Scanner(myObj);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] split = data.split(" ");

                for (int i = 0; i < split.length; i++) {
                    layers.add(Integer.parseInt(split[i]));
                }

            }

            myReader.close();

        } catch (java.io.FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();

        }

        return layers;
    }

}