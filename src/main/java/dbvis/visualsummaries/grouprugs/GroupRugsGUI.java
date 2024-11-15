package dbvis.visualsummaries.grouprugs;

// Import classes from util package
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;

// Import classes from swing package
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import dbvis.visualsummaries.data.CSVDataLoader;
import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.grouprugs.linearprogram.MGOrder;
import dbvis.visualsummaries.grouprugs.linearprogram.ReebGraphOrderingPipeline;
import dbvis.visualsummaries.grouprugs.metrics.MetricsUtil;
import dbvis.visualsummaries.grouprugs.metrics.Pearsons;
import dbvis.visualsummaries.grouprugs.tgs.TGS;
import dbvis.visualsummaries.grouprugs.tgs.Utils;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.*;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.PostProcessing;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraph;
import dbvis.visualsummaries.grouprugs.visualization.PNGWriter;
import dbvis.visualsummaries.grouprugs.visualization.Postprocessing;
import dbvis.visualsummaries.grouprugs.visualization.SaveResults;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.FuzzyPositionMapper;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.MapperUtils;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.MotionLinesPositionMapper;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.MotionRugsPositionMapper;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.QPPositionMapper;
import dbvis.visualsummaries.grouprugs.visualization.groupselectionstrategies.ComponentSelectionStrategy;
import dbvis.visualsummaries.strategies.ClairvoyantPCStrategy;
// import dbvis.visualsummaries.strategies.PrincipalComponentStrategy;
import dbvis.visualsummaries.strategies.SammonMappingStrategy;
import dbvis.visualsummaries.strategies.Strategy;
import dbvis.visualsummaries.strategies.TSNESimpleStrategy;
import dbvis.visualsummaries.strategies.UMAPStrategy;

// Import classes from awt package
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GroupRugsGUI extends javax.swing.JFrame {

    private JComboBox<String> strategyComboBox;
    private JComboBox<String> imageStrategyComboBox;
    private JComboBox<String> datasetComboBox;

    // Ordering Strategies have to be instantiated here and added below where marked
    // private PrincipalComponentStrategy principalcomponentstrategy = new
    // PrincipalComponentStrategy();
    private SammonMappingStrategy sammonmappingstrategy = new SammonMappingStrategy();
    private TSNESimpleStrategy tsnesimplestrategy = new TSNESimpleStrategy();
    private UMAPStrategy umapstrategy = new UMAPStrategy();
    private ClairvoyantPCStrategy chasingpcstrategy = new ClairvoyantPCStrategy(1.0 / 1.9, 0.001);

    private List<Component> originalComponents;

    JPanel imagePanel = new JPanel(new BorderLayout());
    JPanel statsPanel = new JPanel(new GridLayout(4, 1));
    JPanel metricsPanel = new JPanel(new BorderLayout());

    public GroupRugsGUI() {

        // Load data
        String[] datapath = new String[1];
        datapath[0] = "data/";
        CSVDataLoader.checkAndLoadCSVDataSets(datapath);

        this.originalComponents = new ArrayList<Component>();

        initComponents();
    }

    public static void main(String[] args) throws Exception {

        // Set up the GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GroupRugsGUI().setVisible(true);
            }

        });

    }

    private void initComponents() {
        setTitle("GroupRugs GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel comboPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Image Strategy
        JLabel imageStrategyLabel = new JLabel("Select Image Strategy:");
        imageStrategyComboBox = new JComboBox<>();
        imageStrategyComboBox.addItem("Ordered Rugs");
        imageStrategyComboBox.addItem("Fuzzy Rugs");
        imageStrategyComboBox.addItem("Motion Lines");
        imageStrategyComboBox.addItem("MotionRugs");

        gbc.gridx = 0;
        gbc.gridy = 0;
        comboPanel.add(imageStrategyLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        comboPanel.add(imageStrategyComboBox, gbc);

        // Strategy
        JLabel strategyLabel = new JLabel("Select Strategy:");
        strategyComboBox = new JComboBox<>();
        // strategyComboBox.addItem("PrincipalComponentStrategy");
        strategyComboBox.addItem("UMAPStrategy");
        strategyComboBox.addItem("Stable sammon mapping");
        strategyComboBox.addItem("t-SNE (simple)");
        strategyComboBox.addItem("ClairvoyantPCStrategy");
        strategyComboBox.addItem("Run All Metrics");

        gbc.gridx = 1;
        gbc.gridy = 0;
        comboPanel.add(strategyLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        comboPanel.add(strategyComboBox, gbc);

        // Dataset
        JLabel datasetLabel = new JLabel("Select Dataset:");
        datasetComboBox = new JComboBox<>();

        // Read datasets from SessionData
        SessionData sessionData = SessionData.getInstance();
        Set<String> datasetNames = sessionData.getDatasetNames();
        for (String datasetName : datasetNames) {
            datasetComboBox.addItem(datasetName);
        }

        gbc.gridx = 2;
        gbc.gridy = 0;
        comboPanel.add(datasetLabel, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        comboPanel.add(datasetComboBox, gbc);

        // Get double entered by user for epsilon
        JLabel motionLinesLabel = new JLabel("Transition Width:");
        JTextField motionLinesPane = new JTextField("15");

        gbc.gridx = 3;
        gbc.gridy = 0;
        comboPanel.add(motionLinesLabel, gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        comboPanel.add(motionLinesPane, gbc);

        // Get double entered by user for epsilon
        JLabel epsilonLabel = new JLabel("Epsilon:");
        JTextField epsilonPane = new JTextField("15");

        gbc.gridx = 4;
        gbc.gridy = 0;
        comboPanel.add(epsilonLabel, gbc);
        gbc.gridx = 4;
        gbc.gridy = 1;
        comboPanel.add(epsilonPane, gbc);

        // Get int entered by user for delta
        JLabel deltaLabel = new JLabel("Delta:");
        JTextField deltaPane = new JTextField("1");

        gbc.gridx = 5;
        gbc.gridy = 0;
        comboPanel.add(deltaLabel, gbc);
        gbc.gridx = 5;
        gbc.gridy = 1;
        comboPanel.add(deltaPane, gbc);

        // Get int entered by user for delta
        JLabel mLabel = new JLabel("M:");
        JTextField mPane = new JTextField("1");

        gbc.gridx = 6;
        gbc.gridy = 0;
        comboPanel.add(mLabel, gbc);
        gbc.gridx = 6;
        gbc.gridy = 1;
        comboPanel.add(mPane, gbc);

        JPanel buttonPanel = new JPanel();
        JButton computeButton = new JButton("Compute");
        buttonPanel.add(computeButton);

        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        comboPanel.add(buttonPanel, gbc);
        gbc.gridx = 7;
        gbc.gridy = 1;

        JCheckBox metricsCheckBox = new JCheckBox("Show Metrics");

        gbc.gridx = 8;
        gbc.gridy = 0;
        comboPanel.add(metricsCheckBox, gbc);

        // Automatically set to checked
        metricsCheckBox.setSelected(false);

        // Create panel for image
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Image"));

        JPanel statsPanel = new JPanel(new GridLayout(4, 1));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));

        // Create Panel for metrics
        JPanel metricsPanel = new JPanel();
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Metrics"));

        // Add comboPanel, imagePanel, and buttonPanel to the main panel using
        // BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Size mainpanel to fit the screen
        int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int screenHeight = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        mainPanel.setPreferredSize(new Dimension(screenWidth, screenHeight - 40));

        mainPanel.add(comboPanel, BorderLayout.NORTH);
        mainPanel.add(imagePanel, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        computeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    // Get selected values from the drop-down menus
                    String selectedStrategy = (String) strategyComboBox.getSelectedItem();

                    if (selectedStrategy.equals("Run All Metrics")) {
                        computeAllMetrics();
                        return;
                    } else {

                        String selectedImageStrategy = (String) imageStrategyComboBox.getSelectedItem();
                        String selectedDataset = (String) datasetComboBox.getSelectedItem();
                        Integer selectedMotionLinesWidth = Integer.parseInt(motionLinesPane.getText());
                        Double epsilon = Double.parseDouble(epsilonPane.getText());
                        Integer delta = Integer.parseInt(deltaPane.getText());
                        Integer m = Integer.parseInt(mPane.getText());

                        Integer[][] etpMap = computeETPMap(selectedStrategy,
                                selectedImageStrategy,
                                selectedDataset,
                                selectedMotionLinesWidth,
                                epsilon,
                                delta,
                                m);

                        boolean MR;
                        if (selectedImageStrategy.equals("MotionRugs")) {
                            MR = true;
                        } else {
                            MR = false;
                        }

                        // Perform necessary computations with the selected values
                        BufferedImage awtImage = computeImage(
                                etpMap,
                                SessionData.getInstance(),
                                selectedDataset,
                                3,
                                epsilon,
                                MR);

                        Double[][] projections = null;
                        if (selectedImageStrategy.equals("Motion Lines")) {
                            projections = MapperUtils.readProjections(selectedStrategy, selectedDataset,
                                    etpMap.length, etpMap[0].length);
                        }

                        // If metrics checkbox is checked, compute metrics
                        if (metricsCheckBox.isSelected()) {

                            HashMap<String, double[]> metrics = MetricsUtil.getMetrics(
                                    selectedDataset,
                                    selectedStrategy,
                                    getSelectedStrategy(selectedStrategy),
                                    selectedImageStrategy,
                                    epsilon,
                                    originalComponents,
                                    etpMap,
                                    projections,
                                    selectedImageStrategy.equals("Motion Lines"),
                                    awtImage);

                            // Display metrics
                            BufferedImage silhouetteScore = Utils
                                    .createHistogramSilhouette(metrics.get("Silhouette Score:"));
                            BufferedImage spatialQualityDist = Utils
                                    .createHistogramSpatialDist(metrics.get("Spatial Quality Dist:"));
                            BufferedImage stabilityDist = Utils
                                    .createHistogramStabilityDist(metrics.get("Stability Dist:"));

                            statsPanel.removeAll();

                            statsPanel.add(new JLabel(new ImageIcon(silhouetteScore)));
                            statsPanel.add(new JLabel(new ImageIcon(stabilityDist)));

                            statsPanel.add(new JLabel(new ImageIcon(spatialQualityDist)));
                        } else {
                            statsPanel.removeAll();
                        }

                        // set imagePanel to right dimensions
                        imagePanel.setPreferredSize(new Dimension(awtImage.getWidth(), awtImage.getHeight()));

                        String directoryname = selectedDataset + "_"
                                + selectedStrategy + "_"
                                + selectedImageStrategy + "_eps"
                                + epsilon;

                        SaveResults.saveResults(awtImage, etpMap, directoryname);

                        // Display awtImage in the imagePanel
                        imagePanel.removeAll();
                        imagePanel.add(new JLabel(new ImageIcon(awtImage)), BorderLayout.CENTER);
                        imagePanel.revalidate();
                        imagePanel.repaint();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(GroupRugsGUI.this, "Error occurred: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        getContentPane().add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private Integer[][] computeETPMap(
            String selectedStrategy,
            String selectedImageStrategy,
            String selectedDataset,
            int selectedMotionLinesWidth,
            Double EPSILON,
            Integer DELTA,
            Integer M) throws Exception {

        /*
         * fishdatamerge: EPSILON == 250d
         */

        /* Obtain user-selected parameters. */
        int MOTIONLINES_WIDTH = selectedMotionLinesWidth;
        Strategy SELECTED_STRATEGY = getSelectedStrategy(selectedStrategy);
        String datasetName = selectedDataset;

        int ENLARGEMENT_FACTOR = 3;

        /* Load required dataset. */
        SessionData sessionData = SessionData.getInstance();
        DataSet current = sessionData.getDataset(datasetName);
        sessionData.setCurrentDataSet(current.getName());
        DataPoint[][] baseData = current.getBaseData(); // Do not remove, used for MotionLines

        // Get number of entities and frames
        int frames = current.getBaseData().length;
        int entities = current.getBaseData()[0].length;

        // Compute tgs
        TGS tgs = new TGS(EPSILON, DELTA, M, datasetName);
        ReebGraph rg = tgs.compute();
        rg = tgs.postprocess(rg);

        List<Component> originalComponents = ComponentSelectionStrategy.selectGroups(rg);
        this.originalComponents = originalComponents;
        List<Component> filteredComponents = filterGroups(originalComponents, DELTA, M);

        MotionLinesPositionMapper mlp = new MotionLinesPositionMapper(MOTIONLINES_WIDTH, frames, entities);
        Integer[][] etpMap = null;

        // Draw image based on selected image strategy
        switch (selectedImageStrategy) {
            case "Fuzzy Rugs":

                FuzzyPositionMapper fpp = new FuzzyPositionMapper(MOTIONLINES_WIDTH, frames, entities);

                etpMap = fpp.fuzzyPositioning(
                        filteredComponents,
                        SELECTED_STRATEGY,
                        sessionData,
                        datasetName,
                        ENLARGEMENT_FACTOR);

                break;

            case "Motion Lines":

                etpMap = mlp.motionlinesPositioning(
                        SELECTED_STRATEGY,
                        sessionData,
                        datasetName,
                        ENLARGEMENT_FACTOR);

                break;
            case "Ordered Rugs":

                // GroupLineComputer.compute2(rg, entities, frames);
                Set<MaximalGroup> maximalgroups = GroupLineComputer.compute2(rg, entities, frames);
                int maxTimestamp = rg.getMaxTimestamp();

                String file = datasetName + "_eps" + EPSILON;

                String OrderPath = file + "_treeMap.txt";
                String GroupMapPath = file + "_groupMap.txt";
                String LayersPath = file + "_layers.txt";

                // Check if corresponding files exist
                java.io.File OrdersFile = new java.io.File("orderedGroups/" + OrderPath);
                java.io.File GroupMapFile = new java.io.File("orderedGroups/" + GroupMapPath);
                java.io.File LayersFile = new java.io.File("orderedGroups/" + LayersPath);

                // Get the ordered maximal groups per layer and the group map
                List<MGOrder> orderedMGroups = null;
                Map<Integer, MaximalGroup> IDToGroupMap = null;
                List<Integer> layers = null;

                ReebGraphOrderingPipeline rgop = new ReebGraphOrderingPipeline();

                // If files exist, read from files
                if (OrdersFile.exists() && GroupMapFile.exists() && LayersFile.exists()) {

                    orderedMGroups = rgop.readTreeMapFromFile(OrdersFile.getName());
                    IDToGroupMap = rgop.readGroupMapFromFile(GroupMapFile.getName(),
                            maxTimestamp);
                    layers = rgop.readLayersFromFile(LayersFile.getName());

                    // Set the ordered maximal groups per layer and the group map
                    rgop.setOrderedMGroups(orderedMGroups);
                    rgop.setGroupMap(IDToGroupMap);
                    rgop.setLayers(layers);

                } else {

                    // Run the ReebGraph ordering pipeline
                    rgop.run(rg, maximalgroups);

                    // Get the ordered maximal duration groups per layer and the group map
                    orderedMGroups = rgop.getOrderedMGroups();
                    IDToGroupMap = rgop.getGroupMap();
                    layers = rgop.getLayers();

                    // Write to file
                    rgop.writeTreeMapToFile(orderedMGroups, file);
                    rgop.writeGroupMapToFile(IDToGroupMap, file);
                    rgop.writeLayersToFile(layers, file);

                }

                QPPositionMapper qp = new QPPositionMapper(
                        MOTIONLINES_WIDTH,
                        baseData,
                        sessionData,
                        SELECTED_STRATEGY,
                        filteredComponents,
                        datasetName,
                        orderedMGroups,
                        IDToGroupMap,
                        ENLARGEMENT_FACTOR);

                etpMap = qp.orderedPositioning();
                break;

            case "MotionRugs":
                MotionRugsPositionMapper mrp = new MotionRugsPositionMapper();
                etpMap = mrp.position(sessionData, baseData, SELECTED_STRATEGY, datasetName);
                break;

            default:
                System.out.println("Selected image strategy not found, default to MotionLines");
                etpMap = mlp.motionlinesPositioning(
                        SELECTED_STRATEGY,
                        sessionData,
                        datasetName,
                        ENLARGEMENT_FACTOR);
                break;
        }

        etpMap = Postprocessing.postProcess(filteredComponents, etpMap);

        return etpMap;

    }

    private void computeAllMetrics() {

        SessionData sessionData = SessionData.getInstance();

        // Set of large datasets: t-SNE exceeds one hour limit for these datasets.
        Set<String> largedatasets = new HashSet<String>() {
            {
                add("200pop1");
                add("mergeFocus");
            }
        };

        // Set of small datasets: t-SNE does not exceed one hour limit for these
        // datasets.
        Set<String> smalldatasets = new HashSet<String>() {
            {
                add("tryagain");
                add("grouping");
            }
        };

        Set<String> largeDatasetStrats = new HashSet<String>();
        largeDatasetStrats.add("ClairvoyantPCStrategy");
        largeDatasetStrats.add("UMAPStrategy");
        largeDatasetStrats.add("Stable sammon mapping");

        // Add strategies to Set
        Set<String> smallDatasetStrats = new HashSet<String>();
        smallDatasetStrats.add("ClairvoyantPCStrategy");
        smallDatasetStrats.add("UMAPStrategy");
        smallDatasetStrats.add("Stable sammon mapping");
        smallDatasetStrats.add("t-SNE (simple)");

        Set<String> imageStrategies = new HashSet<String>();
        imageStrategies.add("Motion Lines");
        imageStrategies.add("Fuzzy Rugs");
        imageStrategies.add("MotionRugs");
        imageStrategies.add("Ordered Rugs");

        // Handle large datasets.
        for (String dataset : largedatasets) {
            for (String strat : largeDatasetStrats) {
                for (String imageStrat : imageStrategies) {
                    try {

                        boolean MR;
                        if (imageStrat.equals("MotionRugs")) {
                            MR = true;
                        } else {
                            MR = false;
                        }

                        Integer[][] etpMap = computeETPMap(strat, imageStrat, dataset, 15, 15.0, 1,
                                1);
                        BufferedImage image = computeImage(etpMap, sessionData, dataset, 3, 15.0, MR);

                        Double[][] projections = null;
                        if (imageStrat.equals("Motion Lines")) {
                            projections = MapperUtils.readProjections(strat, dataset,
                                    etpMap.length, etpMap[0].length);
                        }

                        HashMap<String, double[]> metrics = MetricsUtil.getMetrics(
                                dataset,
                                strat,
                                getSelectedStrategy(strat),
                                imageStrat,
                                15.0,
                                originalComponents,
                                etpMap,
                                projections,
                                imageStrat.equals("Motion Lines"),
                                image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Handle small datasets.
        for (String dataset : smalldatasets) {
            for (String strat : smallDatasetStrats) {
                for (String imageStrat : imageStrategies) {
                    try {

                        boolean MR;
                        if (imageStrat.equals("MotionRugs")) {
                            MR = true;
                        } else {
                            MR = false;
                        }

                        Integer[][] etpMap = computeETPMap(strat, imageStrat, dataset, 15, 15.0, 1, 1);
                        BufferedImage image = computeImage(etpMap, sessionData, dataset, 3, 15.0, MR);

                        Double[][] projections = null;
                        if (imageStrat.equals("Motion Lines")) {
                            projections = MapperUtils.readProjections(strat, dataset,
                                    etpMap.length, etpMap[0].length);
                        }
                        //
                        // saveFinalImage(dataset, strat, imageStrat, 15.0, image);

                        HashMap<String, double[]> metrics = MetricsUtil.getMetrics(
                                dataset,
                                strat,
                                getSelectedStrategy(strat),
                                imageStrat,
                                15.0,
                                originalComponents,
                                etpMap,
                                projections,
                                imageStrat.equals("Motion Lines"),
                                image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Handle fishdatamerge with different parameters.
        for (String strat : largeDatasetStrats) {
            for (String imageStrat : imageStrategies) {
                try {
                    boolean MR;
                    if (imageStrat.equals("MotionRugs")) {
                        MR = true;
                    } else {
                        MR = false;
                    }

                    Integer[][] etpMap = computeETPMap(strat, imageStrat, "fishdatamerge", 75, 250.0, 1, 1);
                    BufferedImage image = computeImage(etpMap, sessionData, "fishdatamerge", 3, 250.0, MR);

                    Double[][] projections = null;
                    if (imageStrat.equals("Motion Lines")) {
                        projections = MapperUtils.readProjections(strat, "fishdatamerge",
                                etpMap.length, etpMap[0].length);
                    }

                    // saveFinalImage("fishdatamerge", strat, imageStrat, 250.0, image);

                    HashMap<String, double[]> metrics = MetricsUtil.getMetrics(
                            "fishdatamerge",
                            strat,
                            getSelectedStrategy(strat),
                            imageStrat,
                            250.0,
                            originalComponents,
                            etpMap,
                            projections,
                            imageStrat.equals("Motion Lines"),
                            image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void saveFinalImage(
            String dataset,
            String strat,
            String imageStrat,
            Double EPSILON,
            BufferedImage image) {

        HashMap<String, String> stratDict = new HashMap<String, String>();
        // stratDict.put("PrincipalComponentStrategy", "PCA");
        stratDict.put("Stable UMAPStrategy", "UMAP");
        stratDict.put("UMAPStrategy", "UMAP");
        stratDict.put("Stable Sammon Mapping", "SAM");
        stratDict.put("Stable sammon mapping", "SAM");
        stratDict.put("t-SNE (simple)", "SNE");

        HashMap<String, String> imageStratDict = new HashMap<String, String>();
        imageStratDict.put("Motion Lines", "ML");
        imageStratDict.put("Fuzzy Rugs", "nGR");
        imageStratDict.put("MotionRugs", "MR");
        imageStratDict.put("Ordered Rugs", "GR");

        // Save image
        String userdir = System.getProperty("user.home");
        File imgfolder = new File(userdir + "/motionrugs/Final_Images");
        if (!imgfolder.exists()) {
            imgfolder.mkdir();
        }

        File outputfile = new File(
                imgfolder + "/" + dataset + "_"
                        + stratDict.get(strat) + "_" + imageStratDict.get(imageStrat) + "_" + "eps" + EPSILON + ".png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(PNGWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Function to compute the image based on the etpMap and selected parameters.
     * 
     * @param etpMap
     * @param sessionData
     * @param datasetName
     * @param ENLARGEMENT_FACTOR
     * @param EPSILON
     * @return
     * @throws Exception
     */
    private BufferedImage computeImage(
            Integer[][] etpMap,
            SessionData sessionData,
            String datasetName,
            int ENLARGEMENT_FACTOR,
            Double EPSILON,
            boolean MR) throws Exception {

        PNGWriter pnGWriter = new PNGWriter();
        BufferedImage image = pnGWriter.drawRugs(etpMap, sessionData, datasetName, ENLARGEMENT_FACTOR, EPSILON, MR);

        return image;
    }

    public Strategy getSelectedStrategy(String stratName) {

        switch (stratName) {
            // case "PrincipalComponentStrategy":
            // return principalcomponentstrategy;
            case "UMAPStrategy":
                return umapstrategy;
            case "ClairvoyantPCStrategy":
                return chasingpcstrategy;
            case "Stable sammon mapping":
                sammonmappingstrategy.setStability(true);
                return sammonmappingstrategy;
            case "t-SNE (simple)":
                tsnesimplestrategy.setStability(true);
                return tsnesimplestrategy;
            default:
                return chasingpcstrategy;
        }
    }

    /**
     * Function filters maximal groups based on DELTA and M.
     * 
     * @param maximalGroups maximal groups
     * @param DELTA         DELTA
     * @param M             M
     * @return filteredGroups filtered maximal groups that have a duration >= DELTA
     *         and a size >= M.
     */
    private List<Component> filterGroups(List<Component> components, Integer DELTA, Integer M) {

        List<Component> filteredGroups = new ArrayList<Component>();

        for (Component mg : components) {
            if (mg.getDuration() >= DELTA && mg.getEntities().size() >= M) {
                filteredGroups.add(mg);
            }
        }

        return filteredGroups;
    }

}
