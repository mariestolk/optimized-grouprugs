package dbvis.visualsummaries.motionrugs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import dbvis.visualsummaries.data.CSVDataLoader;
import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.QualityStats;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.data.StrategyStatistics;
import dbvis.visualsummaries.strategies.ClairvoyantClusterPCStrategy;
import dbvis.visualsummaries.strategies.ClairvoyantPCStrategy;
import dbvis.visualsummaries.strategies.ClusterPCStrategy;
import dbvis.visualsummaries.strategies.CompLinkClusteringStrategy;
import dbvis.visualsummaries.strategies.ExternalOrderingStrategy;
import dbvis.visualsummaries.strategies.HilbertCurveStrategy;
import dbvis.visualsummaries.strategies.IdMappingStrategy;
import dbvis.visualsummaries.strategies.PrincipalComponentStrategy;
import dbvis.visualsummaries.strategies.QuadTreeStrategy;
import dbvis.visualsummaries.strategies.RTreeStrategy;
import dbvis.visualsummaries.strategies.SNNClusterStrategy;
import dbvis.visualsummaries.strategies.SammonMappingStrategy;
import dbvis.visualsummaries.strategies.Strategy;
import dbvis.visualsummaries.strategies.TSNESimpleStrategy;
import dbvis.visualsummaries.strategies.UMAPStrategy;
import dbvis.visualsummaries.strategies.XComponentStrategy;
import dbvis.visualsummaries.strategies.YComponentStrategy;
import dbvis.visualsummaries.strategies.ZOrderCurveStrategy;

/**
 * MotionRugs main gui. Initializes processing of the rugs.
 *
 * @author Juri Buchmüller, University of Konstanz
 *         <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public class MotionRugsGUI extends javax.swing.JFrame implements RugMouseMotionListener {

    private DataSet curDataSet;
    private JPanel addPanel = new JPanel();

    private int curX = 0;
    private boolean orderVis = false;
    private int knn = 10;
    private boolean calculateKNNstats = true;
    private boolean alsoStability = false;
    private List<String> measures = Arrays.asList("input measure (dist)", "input measure (rank)", "projection measure",
            "stability (dist)", "stability (rank)",
            "stability + spatial q. (dist)", "stability + spatial q. (rank)");

    // Ordering Strategies have to be instantiated here and added below where marked
    private Strategy pqrstrategy = new QuadTreeStrategy();
    private Strategy rtreestrategy = new RTreeStrategy();
    private Strategy zorderstrategy = new ZOrderCurveStrategy();
    private HilbertCurveStrategy hilbertcurvestrategy = new HilbertCurveStrategy();
    private PrincipalComponentStrategy principalcomponentstrategy = new PrincipalComponentStrategy();
    private ClairvoyantPCStrategy interpolatepcstrategy = new ClairvoyantPCStrategy(1.0 / 1.9);
    private ClairvoyantPCStrategy chasingpcstrategy = new ClairvoyantPCStrategy(1.0 / 1.9, 0.001);
    private XComponentStrategy xcomponentstrategy = new XComponentStrategy();
    private YComponentStrategy ycomponentstrategy = new YComponentStrategy();
    private CompLinkClusteringStrategy complinkcluststrategy = new CompLinkClusteringStrategy();
    private SNNClusterStrategy snncluststrategy = new SNNClusterStrategy(knn);
    private SammonMappingStrategy sammonmappingstrategy = new SammonMappingStrategy();
    // private TSNEStrategy tsnestrategy = new TSNEStrategy();
    private TSNESimpleStrategy tsnesimplestrategy = new TSNESimpleStrategy();
    private ClusterPCStrategy clusterpcstrategy = new ClusterPCStrategy();
    private ClairvoyantClusterPCStrategy interpolateclusterpcstrategy = new ClairvoyantClusterPCStrategy(1.0 / 1.9);
    private ExternalOrderingStrategy extorderstrategy = new ExternalOrderingStrategy();
    private IdMappingStrategy idmappingstrategy = new IdMappingStrategy();
    private UMAPStrategy umapstrategy = new UMAPStrategy();
    private MovePanel mvp;
    private ArrayList<VisPanel> vispanels = new ArrayList<>();
    private ArrayList<RugPanel> rugpanels = new ArrayList<>();

    /**
     * Constructor initializing the datasets and strategies
     */
    public MotionRugsGUI(String[] datadir) {
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.PAGE_AXIS));

        // If set, the first item of the datadir array is taken as data directory
        // location. If not set, defaults to /data/*
        CSVDataLoader.checkAndLoadCSVDataSets(datadir);

        SessionData data = SessionData.getInstance();
        initComponents();

        this.setExtendedState(MAXIMIZED_BOTH);
        Dimension windowsize = new Dimension(this.getWidth(), this.getHeight());
        Dimension movepanel = new Dimension(windowsize.width, ((Double) (windowsize.height * .3)).intValue());

        jPanel3.setBackground(java.awt.Color.orange);

        jPanel3.setSize(movepanel);
        jPanel3.setMaximumSize(movepanel);
        jPanel3.setPreferredSize(movepanel);

        jPanel3.repaint();
        jPanel3.revalidate();

        jPanel3.setLayout(new BorderLayout());
        mvp = new MovePanel();
        // mvp.resizePanel(jPanel3.getSize());
        jPanel3.add(mvp, BorderLayout.CENTER);

        System.out.println("REALSIZE " + mvp.getSize());
        this.pack();

        if (data.getDatasetNames().isEmpty()) {
            Logger.getLogger(MotionRugsGUI.class.getName()).log(Level.SEVERE, null, "NO DATASETS FOUND.");
            System.exit(-1);
        }

        // lists datasets
        jComboBox4.removeAllItems();
        for (String s : data.getDatasetNames()) {
            jComboBox4.addItem(s);
        }

        curDataSet = data.getDataset(jComboBox4.getItemAt(jComboBox4.getSelectedIndex()));
        data.setCurrentDataSet(curDataSet.getName());

        jComboBox5.removeAllItems();

        // In the feature list, frame, id and position are excluded as features
        for (String s : curDataSet.getFeatureList()) {
            if (s.equals("frame") || s.equals("id") || s.equals("x") || s.equals("y")) {
                continue;
            }
            jComboBox5.addItem(s);
        }
        for (String s : measures) {
            jComboBox5.addItem(s);
        }

        jComboBox6.removeAllItems();

        // Adding Strategy to selection menu. Has to be the same string as provided in
        // Strategy Class when calling getName()
        jComboBox6.addItem("Hilbert curve");
        jComboBox6.addItem("Point Quadtree");
        jComboBox6.addItem("R-Tree");
        jComboBox6.addItem("Z-Order");
        jComboBox6.addItem("First principal component");
        jComboBox6.addItem("Clairvoyant (interpolate)");
        jComboBox6.addItem("Clairvoyant (slow chase)");
        jComboBox6.addItem("UMAPStrategy");
        jComboBox6.addItem("X-component");
        jComboBox6.addItem("Y-component");
        jComboBox6.addItem("Complete-linkage clustering");
        jComboBox6.addItem("SNN clustering");
        jComboBox6.addItem("Stable sammon mapping");
        jComboBox6.addItem("Sammon mapping");
        jComboBox6.addItem("Stable t-SNE");
        jComboBox6.addItem("t-SNE");
        jComboBox6.addItem("Stable t-SNE (simple)");
        jComboBox6.addItem("t-SNE (simple)");
        jComboBox6.addItem("CLC + PCA");
        jComboBox6.addItem("CLC + SPC");
        jComboBox6.addItem("External ordering");
        jComboBox6.addItem("Order on id");
        jComboBox6.addItem("Parameter experiment");

        jComboBox7.removeAllItems();

        // Adding Color Mappers to selection menu.
        jComboBox7.addItem("Binned percentile Blue to Red");
        jComboBox7.addItem("Linear Blue to Red");
        jComboBox7.addItem("Linear Grayscale");
        jComboBox7.addItem("Distance of position to previous frame");
        jComboBox7.addItem("Spatial Coloring");
        jComboBox7.addItem("KSdist Coloring");
        jComboBox7.addItem("KSrank Coloring");
        jComboBox7.addItem("KSproj Coloring");

        jToggleButton1.setEnabled(false);
        jLabel3.setVisible(false);
        jSpinner1.setVisible(false);

        // Sets the features according to the ones available in a chosen dataset (except
        // standard features)
        jComboBox4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                curDataSet = data.getDataset(jComboBox4.getItemAt(jComboBox4.getSelectedIndex()));
                SessionData.getInstance().setCurrentDataSet(curDataSet.getName());
                System.out.println("Selected Dataset: " + curDataSet.getName());
                jComboBox5.removeAllItems();
                for (String s : curDataSet.getFeatureList()) {
                    if (s.equals("frame") || s.equals("id") || s.equals("x") || s.equals("y")) {
                        continue;
                    }
                    jComboBox5.addItem(s);
                }
                for (String s : measures) {
                    jComboBox5.addItem(s);
                }
                // if dataset has been switched, delete all existing rugpanels (otherwise
                // statistics get inconsistent, and different datasets are not labeled yet
                // anyway)
                addPanel.removeAll();
                rugpanels.clear();
                orderVis = false;
                jToggleButton1.setSelected(orderVis);
                jToggleButton1.setEnabled(false);
                mvp.resetMovePanel(orderVis);

                revalidate();
                repaint();

            }
        });

        // Sets the available colors depending on the chosen strategy
        jComboBox6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedStrategy = jComboBox6.getItemAt(jComboBox6.getSelectedIndex());
                if (selectedStrategy.equals("First principal component")
                        || selectedStrategy.equals("Clairvoyant (interpolate)")
                        || selectedStrategy.equals("Clairvoyant (slow chase)")
                        || selectedStrategy.equals("CLC + SPC")) {
                    jComboBox7.removeItem("Direction");
                    jComboBox7.addItem("Direction");

                    if (selectedStrategy.equals("Clairvoyant (interpolate)")
                            || selectedStrategy.equals("Clairvoyant (slow chase)")
                            || selectedStrategy.equals("CLC + SPC")) {
                        jLabel3.setVisible(true);
                        jSpinner1.setVisible(true);
                        jSpinner1.setValue(0.526);
                    } else {
                        jLabel3.setVisible(false);
                        jSpinner1.setVisible(false);
                    }

                } else {
                    jComboBox7.removeItem("Direction");
                    jLabel3.setVisible(false);
                    jSpinner1.setVisible(false);
                }

            }
        });

        jButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NumberFormat formatter = new DecimalFormat("#0.00");
                String selectedDataSet = jComboBox4.getItemAt(jComboBox4.getSelectedIndex());
                String selectedFeature = jComboBox5.getItemAt(jComboBox5.getSelectedIndex());
                String selectedStrategy = jComboBox6.getItemAt(jComboBox6.getSelectedIndex());
                String selectedColorMapper = jComboBox7.getItemAt(jComboBox7.getSelectedIndex());
                double threshold = (double) jSpinner1.getValue();
                DataSet current = SessionData.getInstance().getDataset(selectedDataSet);
                BufferedImage bf = null;
                DataPoint[][] orderedpoints = null;

                long startTime = System.currentTimeMillis();
                // ADD NEW STRATEGIES HERE
                // According to the selected strategy the data of the chosen dataset is ordered
                switch (selectedStrategy) {
                    case "Point Quadtree":
                        orderedpoints = pqrstrategy.getOrderedValues(current.getBaseData(), current.getName());

                        break;
                    case "R-Tree":
                        orderedpoints = rtreestrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "Hilbert curve":
                        hilbertcurvestrategy.setHilbertOrder(100);
                        orderedpoints = hilbertcurvestrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "Z-Order":
                        orderedpoints = zorderstrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "First principal component":
                        orderedpoints = principalcomponentstrategy.getOrderedValues(current.getBaseData(),
                                current.getName());
                        SessionData.getInstance().getCurrentDataSet()
                                .setDirections(principalcomponentstrategy.getDirections(), selectedStrategy);
                        SessionData.getInstance().getCurrentDataSet()
                                .setEigenvalues(principalcomponentstrategy.getEigenvalues(), selectedStrategy);
                        break;
                    case "Clairvoyant (interpolate)":
                        interpolatepcstrategy.setThresHoldConstant(threshold);
                        orderedpoints = interpolatepcstrategy.getOrderedValues(current.getBaseData(),
                                current.getName());
                        selectedStrategy = selectedStrategy + " " + threshold;
                        SessionData.getInstance().getCurrentDataSet()
                                .setDirections(interpolatepcstrategy.getDirections(), selectedStrategy);
                        SessionData.getInstance().getCurrentDataSet()
                                .setEigenvalues(interpolatepcstrategy.getEigenvalues(), selectedStrategy);
                        break;
                    case "Clairvoyant (slow chase)":
                        chasingpcstrategy.setThresHoldConstant(threshold);
                        orderedpoints = chasingpcstrategy.getOrderedValues(current.getBaseData(), current.getName());
                        selectedStrategy = selectedStrategy + " " + threshold;
                        SessionData.getInstance().getCurrentDataSet().setDirections(chasingpcstrategy.getDirections(),
                                selectedStrategy);
                        SessionData.getInstance().getCurrentDataSet().setEigenvalues(chasingpcstrategy.getEigenvalues(),
                                selectedStrategy);
                        break;
                    case "X-component":
                        orderedpoints = xcomponentstrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "UMAPStrategy":
                        orderedpoints = umapstrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "Y-component":
                        orderedpoints = ycomponentstrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "Complete-linkage clustering":
                        orderedpoints = complinkcluststrategy.getOrderedValues(current.getBaseData(),
                                current.getName());
                        break;
                    case "SNN clustering":
                        orderedpoints = snncluststrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "Stable sammon mapping":
                        sammonmappingstrategy.setStability(true);
                        orderedpoints = sammonmappingstrategy.getOrderedValues(current.getBaseData(),
                                current.getName());
                        break;
                    case "Sammon mapping":
                        sammonmappingstrategy.setStability(false);
                        orderedpoints = sammonmappingstrategy.getOrderedValues(current.getBaseData(),
                                current.getName());
                        break;
                    // case "Stable t-SNE":
                    // tsnestrategy.setStability(true);
                    // orderedpoints = tsnestrategy.getOrderedValues(current.getBaseData());
                    // break;
                    // case "t-SNE":
                    // tsnestrategy.setStability(false);
                    // orderedpoints = tsnestrategy.getOrderedValues(current.getBaseData());
                    // break;
                    case "CLC + PCA":
                        orderedpoints = clusterpcstrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "CLC + SPC":
                        interpolateclusterpcstrategy.setThresHoldConstant(threshold);
                        orderedpoints = interpolateclusterpcstrategy.getOrderedValues(current.getBaseData(),
                                current.getName());
                        selectedStrategy = selectedStrategy + " " + threshold;
                        break;
                    case "Stable t-SNE (simple)":
                        tsnesimplestrategy.setStability(true);
                        orderedpoints = tsnesimplestrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "t-SNE (simple)":
                        tsnesimplestrategy.setStability(false);
                        orderedpoints = tsnesimplestrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "External ordering":
                        orderedpoints = extorderstrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "Order on id":
                        orderedpoints = idmappingstrategy.getOrderedValues(current.getBaseData(), current.getName());
                        break;
                    case "Parameter experiment":

                        for (int i = 0; i < 100; i++) {
                            startTime = System.currentTimeMillis();

                            threshold = 0.01 * i;
                            interpolatepcstrategy.setThresHoldConstant(threshold);
                            orderedpoints = interpolatepcstrategy.getOrderedValues(current.getBaseData(),
                                    current.getName());

                            selectedStrategy = "Clairvoyant (interpolate) " + formatter.format(threshold);
                            SessionData.getInstance().getCurrentDataSet()
                                    .setDirections(interpolatepcstrategy.getDirections(), selectedStrategy);
                            SessionData.getInstance().getCurrentDataSet()
                                    .setEigenvalues(interpolatepcstrategy.getEigenvalues(), selectedStrategy);
                            SessionData.getInstance().addOrderedData(selectedDataSet, selectedStrategy, orderedpoints);

                            long endTime = System.currentTimeMillis();

                            calculateStats(orderedpoints, current, selectedStrategy);
                            StatWriter.saveStats(SessionData.getInstance(), selectedStrategy, endTime - startTime);
                            StatWriter.saveOrdering(SessionData.getInstance(), selectedStrategy);
                            bf = PNGWriter.drawAndSaveRugs(SessionData.getInstance(), selectedFeature,
                                    current.getName(), selectedStrategy, selectedColorMapper, current.getFeatureList());
                            repaintPanel(bf, selectedStrategy);

                            if (alsoStability && calculateKNNstats) {
                                bf = PNGWriter.drawAndSaveRugs(SessionData.getInstance(), measures.get(5),
                                        current.getName(), selectedStrategy, selectedColorMapper,
                                        current.getFeatureList());
                                repaintPanel(bf, selectedStrategy);
                            }
                        }

                        startTime = System.currentTimeMillis();

                        threshold = 1.0;
                        interpolatepcstrategy.setThresHoldConstant(threshold);
                        orderedpoints = interpolatepcstrategy.getOrderedValues(current.getBaseData(),
                                current.getName());
                        selectedStrategy = "Clairvoyant (interpolate) " + formatter.format(threshold);
                        SessionData.getInstance().getCurrentDataSet()
                                .setDirections(interpolatepcstrategy.getDirections(), selectedStrategy);
                        SessionData.getInstance().getCurrentDataSet()
                                .setEigenvalues(interpolatepcstrategy.getEigenvalues(), selectedStrategy);
                        break;
                }

                long endTime = System.currentTimeMillis();

                // Store data for rug in sessiondata
                SessionData.getInstance().addOrderedData(selectedDataSet, selectedStrategy, orderedpoints);

                // Calculate statistics
                calculateStats(orderedpoints, current, selectedStrategy);

                StatWriter.saveStats(SessionData.getInstance(), selectedStrategy, endTime - startTime);
                StatWriter.saveOrdering(SessionData.getInstance(), selectedStrategy);

                // Creates an image from the reordered data points.
                bf = PNGWriter.drawAndSaveRugs(SessionData.getInstance(), selectedFeature, current.getName(),
                        selectedStrategy, selectedColorMapper, current.getFeatureList());

                System.out.println("DONE REORDERING");
                repaintPanel(bf, selectedStrategy);

                if (alsoStability && calculateKNNstats) {
                    bf = PNGWriter.drawAndSaveRugs(SessionData.getInstance(), measures.get(5), current.getName(),
                            selectedStrategy, selectedColorMapper, current.getFeatureList());

                    System.out.println("STABILITY RUG DONE");
                    repaintPanel(bf, selectedStrategy);
                }
                jToggleButton1.setEnabled(true);
                // renderImages();
            }

        });
    }

    private void renderImages() {
        SessionData sd = SessionData.getInstance();
        int frames = sd.getCurrentDataSet().getBaseData().length;
        String stratid = jComboBox6.getItemAt(jComboBox6.getSelectedIndex());
        for (int i = 0; i < frames; i++) {

            DataPoint[] frame = sd.getCurrentDataSet().getSingleFrame(i);
            System.out.println(sd.getCurrentDataSet().getCurStrats());
            if (stratid.startsWith("Clairvoyant")) {
                stratid = "Clairvoyant (interpolate) 0.526";
            }
            if (!stratid.equals("")) {
                frame = sd.getCurrentDataSet().getData(stratid)[i];
            }

            BufferedImage image = new BufferedImage(1743, 1024, BufferedImage.TYPE_INT_BGR);
            Graphics2D g2 = image.createGraphics();
            File output = new File("D:\\renders2\\output" + stratid + i + ".png");

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int height = 1024;
            int width = 1743;
            g2.setColor(Color.GRAY);
            g2.fillRect(0, 0, width, height);

            int data_minx = ((Double) Math.floor(sd.getDataset(sd.getCurrentDataSetID()).getMin("x"))).intValue();
            int data_miny = ((Double) Math.floor(sd.getDataset(sd.getCurrentDataSetID()).getMin("y"))).intValue();

            int data_maxx = ((Double) Math.ceil(sd.getDataset(sd.getCurrentDataSetID()).getMax("x"))).intValue();
            int data_maxy = ((Double) Math.ceil(sd.getDataset(sd.getCurrentDataSetID()).getMax("y"))).intValue();

            double data_width = data_maxx - data_minx;
            double data_height = data_maxy - data_miny;
            // System.out.println("WH: " + data_width + " " + data_height);
            double heightratio = data_height / height;
            double viewHeight = height;
            double viewWidth = ((Double) (data_width / heightratio)).intValue();

            // System.out.println("DATA RATIO: " + data_width + "/" + data_height + " " +
            // (data_width*1.0/data_height));
            // System.out.println("HEIGHTRATIO: " + heightratio + " (" + data_height + "/" +
            // height + ") new width = " + viewWidth + " height = " + height);
            int viewX = ((Double) ((width * .5) - (.5 * viewWidth))).intValue();
            int viewY = 0;

            g2.setColor(Color.WHITE);
            g2.fillRect(viewX, viewY, ((Double) viewWidth).intValue(), ((Double) viewHeight).intValue());

            int w = ((Double) (viewWidth * 0.1)).intValue();
            int h = ((Double) (viewHeight * 0.1)).intValue();
            g2.setColor(Color.lightGray);
            g2.drawRect(viewX + w, viewY + h, 8 * w, 8 * h);

            g2.translate(viewX + w, viewY + h);

            double scalex = viewWidth * .8 / data_maxx;
            double scaley = viewHeight * .8 / data_maxy;

            g2.scale(scalex, scaley);

            // find mean of each coordinate
            double[] mean = new double[2];

            for (int j = 0; j < frame.length; j++) {
                mean[0] += frame[j].getX();
                mean[1] += frame[j].getY();
            }
            mean[0] = mean[0] / frame.length;
            mean[1] = mean[1] / frame.length;

            // draw the ellipse for PCA
            g2.setColor(Color.LIGHT_GRAY);
            g2.rotate(sd.getCurrentDataSet().getDirection(stratid, curX), mean[0], mean[1]);

            double[] eigenvalues = sd.getCurrentDataSet().getEigenvalues(stratid, curX);
            Ellipse2D.Double ellipse = new Ellipse2D.Double(mean[0] - eigenvalues[0] / 4, mean[1] - eigenvalues[1] / 4,
                    eigenvalues[0] / 2, eigenvalues[1] / 2);
            g2.fill(ellipse);
            g2.draw(ellipse);

            g2.setColor(Color.BLUE);
            Line2D.Double shape2 = new Line2D.Double(mean[0] - eigenvalues[0] / 4, mean[1],
                    mean[0] + eigenvalues[0] / 4, mean[1]);
            g2.draw(shape2);
            g2.rotate(-sd.getCurrentDataSet().getDirection(stratid, curX), mean[0], mean[1]);

            int prevx = Integer.MIN_VALUE, prevy = Integer.MIN_VALUE;
            Color orderColor;
            float brightness = 0.3f;
            for (DataPoint dp : frame) {
                int x = ((Double) dp.getX()).intValue();
                int y = ((Double) dp.getY()).intValue();

                if (prevx != Integer.MIN_VALUE && prevy != Integer.MIN_VALUE) {
                    orderColor = Color.getHSBColor(0.58f, 0.75f, brightness);
                    brightness += 0.7f / frame.length;
                    g2.setColor(orderColor);
                    g2.drawLine(prevx, prevy, x, y);
                    // System.out.println(prevx + " " + prevy + " " + x + " " + y);
                } else {
                    g2.setColor(Color.BLACK);
                }

                g2.fillOval(x - (int) (3 / scalex), y - (int) (3 / scalex), (int) (6 / scalex), (int) (6 / scalex));
                prevx = x;
                prevy = y;
            }
            try {
                ImageIO.write(image, "png", output);
                System.out.println("IMAGE " + i);
            } catch (IOException ex) {
                Logger.getLogger(MotionRugsGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Calculates rug statistics
     *
     * @param orderedpoints
     */
    private void calculateStats(DataPoint[][] orderedpoints, DataSet dataset, String strategyID) {
        int frame = 0;

        int[][] orders = new int[orderedpoints.length][orderedpoints[0].length];

        for (int f = 0; f < orderedpoints.length; f++) {
            for (int e = 0; e < orderedpoints[f].length; e++) {
                if (orderedpoints[f][e] == null) {
                    System.out.println("ERROR: f=" + f + ", e=" + e);
                }
                orders[f][e] = orderedpoints[f][e].getId();
            }
        }

        int jumps = 0;
        int crossings = 0;
        double KSdistNum = 0.0;
        double KSdistDen = 0.0;
        double KSrankNum = 0.0;
        double KSrankDen = 0.0;
        double KSdistInputNum = 0.0;
        double KSdistInputDen = 0.0;
        double KSrankInputNum = 0.0;
        double KSrankInputDen = 0.0;
        double KSprojNum = 0.0;
        double KSprojDen = 0.0;

        double pointKSdistNum = 0.0;
        double pointKSdistDen = 0.0;
        double pointKSrankNum = 0.0;
        double pointKSrankDen = 0.0;
        double pointKSprojNum = 0.0;
        double pointKSprojDen = 0.0;

        DescriptiveStatistics jumpses = new DescriptiveStatistics();
        DescriptiveStatistics crosses = new DescriptiveStatistics();
        DescriptiveStatistics kendalls = new DescriptiveStatistics();
        DescriptiveStatistics KSdist = new DescriptiveStatistics();
        DescriptiveStatistics KSrank = new DescriptiveStatistics();
        DescriptiveStatistics KSdistInput = new DescriptiveStatistics();
        DescriptiveStatistics KSrankInput = new DescriptiveStatistics();
        DescriptiveStatistics KSproj = new DescriptiveStatistics();

        ArrayList<ArrayList<QualityStats>> prevInputRank = new ArrayList();
        ArrayList<ArrayList<QualityStats>> prevProjRank = new ArrayList();

        double[][] pointksdist = new double[orders.length][orders[0].length];
        double[][] pointksrank = new double[orders.length][orders[0].length];
        double[][] pointksproj = new double[orders.length][orders[0].length];

        for (; frame < orders.length; frame++) {

            // knn-nearest-inputNeighbor graph
            if (calculateKNNstats) {
                // initialize an array for the knn neighbors of each node
                ArrayList<ArrayList<QualityStats>> nodes = new ArrayList();
                ArrayList<ArrayList<QualityStats>> projections = new ArrayList();
                while (nodes.size() < orders[frame].length) {
                    nodes.add(new ArrayList<QualityStats>());
                    projections.add(new ArrayList<QualityStats>());
                }
                // initialize an array to store knn rank per node
                int[][] inputRank = new int[orders[0].length][orders[0].length];
                int[][] projRank = new int[orders[0].length][orders[0].length];

                // summing up over all points
                KSdistNum = 0.0;
                KSdistDen = 0.0;
                KSrankNum = 0.0;
                KSrankDen = 0.0;
                KSdistInputNum = 0.0;
                KSdistInputDen = 0.0;
                KSrankInputNum = 0.0;
                KSrankInputDen = 0.0;
                KSprojNum = 0.0;
                KSprojDen = 0.0;

                // calculate knn nearest neighbors for each node
                for (int node = 0; node < orders[frame].length; node++) {
                    int nodeID = orders[frame][node];
                    // add a dummy to set up later loop
                    nodes.get(nodeID).add(new QualityStats(-1, Double.POSITIVE_INFINITY, Integer.MAX_VALUE));
                    projections.get(nodeID).add(new QualityStats(-1, Double.POSITIVE_INFINITY, Integer.MAX_VALUE));

                    // summing up per point
                    pointKSdistNum = 0.0;
                    pointKSdistDen = 0.0;
                    pointKSrankNum = 0.0;
                    pointKSrankDen = 0.0;
                    pointKSprojNum = 0.0;
                    pointKSprojDen = 0.0;

                    // check distances to all other nodes
                    for (int toNode = 0; toNode < orders[frame].length; toNode++) {
                        if (node == toNode) {
                            continue;
                        }
                        int toID = orders[frame][toNode];
                        double dist = getDistance(orderedpoints[frame][node], orderedpoints[frame][toNode]);
                        int orderRank;
                        int far, near;
                        // check order of nodes
                        if (node <= toNode) {
                            far = toNode;
                            near = node;
                        } else {
                            far = node;
                            near = toNode;
                        }
                        // find nearest inputNeighbor rank
                        if (near == node) {
                            orderRank = (2 * (far - near - 1)) + 1 - Math.max(0, (far - near) - near);
                            if ((far - near) - near > 0) {
                                orderRank++;
                            }
                        } else { // (far == node)
                            orderRank = (2 * (far - near - 1)) + 1
                                    - Math.max(0, (far - near) - (orders[frame].length - far));
                            if ((far - near) - (orders[frame].length - far) > 0) {
                                orderRank++;
                            }
                        }
                        // check if this node is closer than any of the potential knn nearest neighbors
                        boolean nodesDone = false;
                        boolean projDone = false;
                        int looplength = nodes.size();
                        for (int i = 0; i < looplength; i++) {
                            // first check in actual space
                            if (!nodesDone && dist > 0 && dist < nodes.get(nodeID).get(i).getDistance()) {
                                nodes.get(nodeID).add(i, new QualityStats(toID, dist, orderRank));
                                nodesDone = true;
                            }
                            // then check in projected space
                            if (!projDone && orderRank > 0
                                    && orderRank < projections.get(nodeID).get(i).getOrderRank()) {
                                projections.get(nodeID).add(i, new QualityStats(toID, dist, orderRank));
                                projDone = true;
                            }
                            if (nodesDone && projDone) {
                                break;
                            }
                        }
                    }
                    // {@code nodes.get(nodeID)} contains all nearest neighbors per node {@code
                    // nodeID}, from close to far
                    // find the nearest inputNeighbor rank per pair in input and projected space for
                    // this frame
                    for (int i = 0; i < nodes.size() - 1; i++) {
                        QualityStats neighbor = nodes.get(nodeID).get(i);
                        inputRank[nodeID][neighbor.getToID()] = i + 1;
                        projRank[nodeID][neighbor.getToID()] = neighbor.getOrderRank();
                    }
                    // first {@code knn} elements of {@code nodes.get(nodeID)} are the knn nearest
                    // neighbors of {@code nodeID}
                    for (int i = 0; i < knn; i++) {
                        QualityStats neighbor = nodes.get(nodeID).get(i);
                        // numerator and denominator for KSdist
                        KSdistNum += (double) neighbor.getOrderRank() / (double) neighbor.getDistance();
                        KSdistDen += 1.0 / (double) neighbor.getDistance();
                        // per point KSdist
                        pointKSdistNum += (double) neighbor.getOrderRank() / (double) neighbor.getDistance();
                        pointKSdistDen += 1.0 / (double) neighbor.getDistance();
                        // numerator and denominator for KSrank
                        double rank = (double) (i + 1);
                        KSrankNum += (double) neighbor.getOrderRank() / rank;
                        KSrankDen += 1.0 / (double) rank;
                        // per point KSrank
                        pointKSrankNum += (double) neighbor.getOrderRank() / rank;
                        pointKSrankDen += 1.0 / (double) rank;
                    }
                    // calculate the KS measures on the input and projected space using the previous
                    // frame as original and current frame as projection
                    if (frame > 0) {
                        for (int i = 0; i < knn; i++) {
                            // first input measures
                            QualityStats inputNeighbor = prevInputRank.get(nodeID).get(i);
                            // numerator and denominator for KSdistInput
                            KSdistInputNum += (double) inputRank[nodeID][inputNeighbor.getToID()]
                                    / (double) inputNeighbor.getDistance();
                            KSdistInputDen += 1.0 / (double) inputNeighbor.getDistance();
                            // numerator and denominator for KSrankInput
                            double rank = (double) (i + 1);
                            KSrankInputNum += (double) inputRank[nodeID][inputNeighbor.getToID()] / rank;
                            KSrankInputDen += 1.0 / rank;
                            // next projected measures
                            QualityStats projNeighbor = prevProjRank.get(nodeID).get(i);
                            // numerator and denominator for KSproj
                            KSprojNum += (double) projRank[nodeID][projNeighbor.getToID()]
                                    / (double) projNeighbor.getOrderRank();
                            KSprojDen += 1.0 / (double) projNeighbor.getOrderRank();
                            // per point KSproj
                            pointKSprojNum += (double) projRank[nodeID][projNeighbor.getToID()]
                                    / (double) projNeighbor.getOrderRank();
                            pointKSprojDen += 1.0 / (double) projNeighbor.getOrderRank();
                        }
                    }

                    // calculate measures per point
                    pointksdist[frame][nodeID] = pointKSdistNum / pointKSdistDen;
                    pointksrank[frame][nodeID] = pointKSrankNum / pointKSrankDen;
                    if (frame > 0) {
                        pointksproj[frame][nodeID] = pointKSprojNum / pointKSprojDen;
                    }
                }

                // store knn graph for next frame
                prevInputRank = nodes;
                prevProjRank = projections;

                // calculate actual measures
                KSdist.addValue(KSdistNum / KSdistDen);
                KSrank.addValue(KSrankNum / KSrankDen);
                if (frame > 0) {
                    KSdistInput.addValue(KSdistInputNum / KSdistInputDen);
                    KSrankInput.addValue(KSrankInputNum / KSrankInputDen);
                    KSproj.addValue(KSprojNum / KSprojDen);
                } else {
                    // KSdistInput.addValue(0.0);
                    // KSrankInput.addValue(0.0);
                    // KSproj.addValue(0.0);
                }

            }

            if (frame < orders.length - 1) {
                // Kendalls Tau
                double[] da = new double[orders[frame].length];
                double[] db = new double[orders[frame].length];
                for (int entity = 0; entity < orders[frame].length; entity++) {
                    da[entity] = entity + 0.0;
                    db[entity] = indexOfIntArray(orders[frame + 1], orders[frame][entity]) + 0.0;
                }
                kendalls.addValue(new KendallsCorrelation().correlation(da, db));

                // System.out.println("Frame " + frame + " " + indexOfIntArray(orders[frame],
                // 103) + "-"+ indexOfIntArray(orders[frame+1],103));
                int framejumps = 0;
                int framecross = 0;

                for (int ent = 0; ent < orders[frame].length; ent++) {
                    int id = orders[frame][ent];
                    int pos1 = ent;
                    int pos2 = indexOfIntArray(orders[frame + 1], id);
                    int idcross = 0;
                    // We cross everything which was larger befor and now is smaller
                    for (int cross = ent + 1; cross < orders[frame].length; cross++) {
                        int id2 = orders[frame][cross];
                        int bpos1 = cross;
                        int bpos2 = indexOfIntArray(orders[frame + 1], id2);

                        if (bpos1 > pos1 && bpos2 < pos2) {
                            idcross++;
                        }
                    }
                    framejumps += Math.abs(pos2 - pos1);
                    framecross += idcross;

                }
                // System.out.println("Jumps in frame " + frame + ": " + framejumps);
                // System.out.println("Crossings in frame " + frame + ": " + framecross);

                jumpses.addValue(framejumps);
                crosses.addValue(framecross);

                jumps += framejumps;
                crossings += framecross;
            }
        }
        // System.out.println("Total jumps: " + jumps);
        // System.out.println("Jumps Mean: " + jumpses.getMean());
        // System.out.println("Jumps Median: " + jumpses.getPercentile(50));
        // System.out.println("Jumps StDev: " + jumpses.getStandardDeviation());
        // System.out.print("\n");
        // System.out.println("Total cross: " + crossings);
        // System.out.println("Cross Mean: " + crosses.getMean());
        // System.out.println("Cross Median: " + crosses.getPercentile(50));
        // System.out.println("Cross StDev: " + crosses.getStandardDeviation());
        // System.out.print("\n");
        // System.out.println("Kendalls count: " + kendalls.getValues().length);
        // System.out.println("Kendalls Mean: " + kendalls.getMean());
        // System.out.println("Kendalls Median: " + kendalls.getPercentile(50));
        // System.out.println("Kendalls StDev: " + kendalls.getStandardDeviation());
        // System.out.println("------------------------");

        StrategyStatistics statistics = new StrategyStatistics(strategyID, jumps, jumpses, crossings, crosses, kendalls,
                KSdist, KSrank, KSdistInput, KSrankInput, KSproj);

        DataSet currentDS = SessionData.getInstance().getCurrentDataSet();
        currentDS.setStatisticsOfStrategy(statistics, strategyID);
        currentDS.setPointKSdist(pointksdist, strategyID);
        currentDS.setPointKSrank(pointksrank, strategyID);
        currentDS.setPointKSproj(pointksproj, strategyID);
    }

    public static int indexOfIntArray(int[] array, int key) {
        int returnvalue = -1;
        for (int i = 0; i < array.length; ++i) {
            if (key == array[i]) {
                returnvalue = i;
                break;
            }
        }
        return returnvalue;
    }

    private double getDistance(DataPoint from, DataPoint to) {
        return Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getY() - from.getY(), 2));
    }

    /**
     * Repaints the Panel showing the visualizations.
     *
     * @param toAdd the Image to be added to the VisPanel
     */
    private void repaintPanel(BufferedImage toAdd, String stratid) {
        RugPanel rp = new RugPanel(toAdd, stratid);
        rp.addListener(this);
        addPanel.add(rp);
        rugpanels.add(rp);

        jScrollPane1.revalidate();
        jScrollPane1.repaint();
        System.out.println("Added.");
    }

    @Override
    public void rugXHasChanged(int x) {
        curX = x;
        mvp.updateMovePanel(curX);
        // for (VisPanel vp : vispanels) {
        // vp.setCurX(x);
        // vp.repaint();
        // }
        for (RugPanel rp : rugpanels) {
            rp.setCurX(x);
            rp.repaint();
        }
        // inform movepanel
        // inform vispanels
    }

    @Override
    public void rugStratVisualizable(boolean visualizable, String stratid) {
        mvp.updateMovePanel(visualizable, stratid, orderVis);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox<String>();
        jLabel2 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox<String>();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox<String>();
        jToggleButton2 = new javax.swing.JToggleButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox7 = new javax.swing.JComboBox<String>();
        jLabel3 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane(addPanel);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1024, 768));
        setSize(new java.awt.Dimension(1024, 768));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jLabel4.setText("Dataset");

        jComboBox4.setModel(
                new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setText("Visualization settings");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel2)
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                                .addComponent(jLabel4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 140,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(28, Short.MAX_VALUE)));
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29,
                                        Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(16, 16, 16)));

        jPanel1.add(jPanel6);

        jLabel5.setText("Feature");

        jComboBox5.setModel(
                new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jToggleButton1.setText("Visualize order");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel7Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 140,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(28, Short.MAX_VALUE)));
        jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel5)
                                        .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29,
                                        Short.MAX_VALUE)
                                .addComponent(jToggleButton1)
                                .addGap(16, 16, 16)));

        jPanel1.add(jPanel7);

        jLabel6.setText("Strategy");

        jComboBox6.setModel(
                new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jToggleButton2.setText("Additional stability rug");
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel8Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jToggleButton2, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel8Layout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 140,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(28, Short.MAX_VALUE)));
        jPanel8Layout.setVerticalGroup(
                jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29,
                                        Short.MAX_VALUE)
                                .addComponent(jToggleButton2)
                                .addGap(16, 16, 16)));

        jPanel1.add(jPanel8);

        jPanel9.setPreferredSize(new java.awt.Dimension(207, 107));

        jLabel1.setText("Colors");

        jComboBox7.setModel(
                new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText("Threshold Constant");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(0.526315789d, 0.0d, 1.0d, 0.001d));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel9Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel9Layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, 140,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel9Layout.createSequentialGroup()
                                                .addComponent(jLabel3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jSpinner1)))
                                .addContainerGap(14, Short.MAX_VALUE)));
        jPanel9Layout.setVerticalGroup(
                jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32,
                                        Short.MAX_VALUE)
                                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3))
                                .addGap(16, 16, 16)));

        jPanel1.add(jPanel9);

        jButton2.setText("Add Rug");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jButton2)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(jButton2)
                                .addContainerGap(67, Short.MAX_VALUE)));

        jPanel1.add(jPanel2);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 187, Short.MAX_VALUE));

        jSplitPane1.setTopComponent(jPanel3);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jSplitPane1.setBottomComponent(jScrollPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSplitPane1));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 112,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 598,
                                        Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jToggleButton1ActionPerformed
        orderVis = !orderVis;
        mvp.updateMovePanel(orderVis);
    }// GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jToggleButton2ActionPerformed
        alsoStability = !alsoStability;
    }// GEN-LAST:event_jToggleButton2ActionPerformed

    /**
     * GUI Starter
     *
     * @param args The first String determines the data directory containing the
     *             datasets to be processed. If not set, defaults to /data/*
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
                    .getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info
                        .getName())) {
                    javax.swing.UIManager
                            .setLookAndFeel(info
                                    .getClassName());

                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger
                    .getLogger(MotionRugsGUI.class
                            .getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger
                    .getLogger(MotionRugsGUI.class
                            .getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger
                    .getLogger(MotionRugsGUI.class
                            .getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger
                    .getLogger(MotionRugsGUI.class
                            .getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>
        // </editor-fold>

        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MotionRugsGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue
                .invokeLater(new Runnable() {
                    public void run() {
                        new MotionRugsGUI(args).setVisible(true);

                    }
                });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JComboBox<String> jComboBox7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    // End of variables declaration//GEN-END:variables

}
