package dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.grouprugs.linearprogram.MGOrder;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.Component;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroup;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.placementheuristic.CompGroupData;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.placementheuristic.ComponentOrderComputer;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.placementheuristic.EquationSolver;
import dbvis.visualsummaries.strategies.Strategy;
import javafx.util.Pair;

public class QPPositionMapper {

    private int MOTIONLINES_WIDTH;
    private int WIDTH;
    private int HEIGHT;

    private DataPoint[][] data;
    private DataPoint[][] orderedDataPoints;

    private Strategy selectedStrategy;

    // Data structures for ordered positioning.
    private List<MGOrder> orderedMGroups;
    private Map<Integer, MaximalGroup> IDToGroupMap;
    private List<Component> components;

    private String dsname;

    private List<Integer> layers;

    public QPPositionMapper(
            int motionlines_width,
            DataPoint[][] data,
            SessionData sd,
            Strategy selectedStrategy,
            List<Component> components,
            String dsname,

            List<MGOrder> orderedMGroups,
            Map<Integer, MaximalGroup> IDToGroupMap,
            int ENLARGEMENT_FACTOR) {

        this.MOTIONLINES_WIDTH = motionlines_width;
        this.HEIGHT = data[0].length * ENLARGEMENT_FACTOR;
        this.WIDTH = data.length;

        this.data = data;
        this.dsname = dsname;
        // this.orderedDataPoints = selectedStrategy.getOrderedValues(data, dsname);
        this.orderedDataPoints = MapperUtils.readOrderedPoint(sd, dsname, selectedStrategy);
        // this.orderedDataPoints =
        // initializeOrderedDataset(selectedStrategy.getOrderedValues(data));
        this.selectedStrategy = selectedStrategy;

        this.orderedMGroups = orderedMGroups;
        this.IDToGroupMap = IDToGroupMap;
        this.components = components;

        // this.projections = initializeProjections();
        this.layers = initializeLayers();

    }

    /**
     * Initializes the layers.
     * 
     * @return The list of layers.
     */
    private List<Integer> initializeLayers() {

        List<Integer> layersList = new ArrayList<Integer>();

        for (MGOrder oc : orderedMGroups) {
            if (!layersList.contains(oc.getLayer())) {
                layersList.add(oc.getLayer());
            }
        }

        // Sort the layers
        layersList.sort(null);

        return layersList;
    }

    /**
     * Maps the data points to the screen coordinates.
     * 
     * @return The entityToPosition array.
     */
    public Integer[][] orderedPositioning() {

        Integer[][] entityToPosition = new Integer[WIDTH][data[0].length];

        HashMap<Component, CompGroupData> compToData = new HashMap<Component, CompGroupData>();

        drawRugs(entityToPosition, compToData);

        MapperUtils.drawMotionLines2(
                entityToPosition,
                orderedDataPoints,
                data,
                selectedStrategy.getName(),
                dsname,
                HEIGHT);

        return entityToPosition;

    }

    /**
     * Fills the entityToPosition array with the screen coordinates of the data
     * points.
     * 
     * @param entityToPosition
     */
    private void drawRugs(Integer[][] etp, HashMap<Component, CompGroupData> compToData) {

        positionComponents(compToData);
        fillEntityToPosition(etp, compToData);

    }

    // -----------------------------------------------------------------------------
    // PositionComponents and helper methods
    // -----------------------------------------------------------------------------
    /**
     * Positions the components on the screen using force-directed layout.
     * 
     * @param etp
     */
    private void positionComponents(HashMap<Component, CompGroupData> compToData) {

        initializeCompToData(compToData);

        HashMap<Integer, List<Component>> componentOrders = ComponentOrderComputer.computeComponentOrder(orderedMGroups,
                components, IDToGroupMap);

        determineAboveBelowComps(compToData, componentOrders);
        initializeStartPositionsTest(compToData);
        solveForceDirectedLayout(compToData);

    }

    /**
     * Initializes the list of CompGroupData objects with components and associated
     * maximal groups.
     * 
     * @param compToData The list of CompGroupData objects.
     */
    private void initializeCompToData(HashMap<Component, CompGroupData> compToData) {

        int id = 0;

        for (Component comp : components) {

            comp.setId(id);

            ArrayList<Integer> maximalGroupIDs = new ArrayList<Integer>();

            for (int i = 0; i < IDToGroupMap.size(); i++) {

                if (comp.getEntities().containsAll(IDToGroupMap.get(i).getEntities())) {
                    maximalGroupIDs.add(i);
                }

            }

            compToData.put(comp, new CompGroupData(comp, maximalGroupIDs));

            id++;
        }

        Double[][] projections = MapperUtils.readProjections(
                selectedStrategy.getName(),
                dsname,
                WIDTH,
                data[0].length);

        Pair<Double, Double> minMax = MapperUtils.getMinMaxProjections(projections);

        // Set the projection position for each component using projection values
        for (CompGroupData cgd : compToData.values()) {
            setProjectionPosition(cgd, projections, minMax);
        }

    }

    /**
     * Determines the components above and below each component at each layer.
     * 
     * @param compToData      HashMap of components to CompGroupData objects.
     * @param componentOrders HashMap of component orders per layer.
     */
    private void determineAboveBelowComps(
            HashMap<Component, CompGroupData> compToData,
            HashMap<Integer, List<Component>> componentOrders) {

        for (int layer : layers) {
            List<Component> componentsInLayer = componentOrders.get(layer);

            List<CompGroupData> prevComps = new ArrayList<CompGroupData>();

            for (Component comp : componentsInLayer) {

                CompGroupData cgd = compToData.get(comp);

                // if (prevComps != null) {

                for (CompGroupData prevComp : prevComps) {

                    // Skip if the component is the same
                    if (prevComp.getComp().getId() == cgd.getComp().getId()) {
                        continue;
                    }

                    cgd.addCompAbove(prevComp);
                    prevComp.addCompBelow(cgd);
                }

                prevComps.add(cgd);

            }

        }

        int problems = 0;

        for (CompGroupData cgd : compToData.values()) {

            System.out.println("Component: " + cgd.getComp().getId());
            //
            // get below components
            List<CompGroupData> belowComps = cgd.getCompBelowList();
            List<CompGroupData> aboveComps = cgd.getCompAboveList();

            for (CompGroupData above : aboveComps) {

                // if above is contained in belowComps
                if (belowComps.contains(above)) {

                    problems += 1;

                    // remove above from belowComps
                    // belowComps.remove(above);

                    // remove cgd from above's belowComps
                    // above.getCompAboveList().remove(cgd);
                }

            }

            // If there is a component that has no forces acting on it, print an error
            if (cgd.getCompAboveList().size() == 0 && cgd.getCompBelowList().size() == 0
                    && cgd.getComp().getEntities().size() != data[0].length) {
                System.out.println("Error: Component " + cgd.getComp().getId() + " has no above or below components.");
            }

        }

        System.out.println("Problems: " + problems);

        // printCompOrder(componentOrders);

    }

    /**
     * Solves the force-directed layout problem for the components.
     * 
     * @param compToData The list of CompGroupData objects.
     */
    private void solveForceDirectedLayout(HashMap<Component, CompGroupData> compToData) {

        int posChanged = Integer.MAX_VALUE;
        int TEMPERATURE = 1000;

        while (posChanged > 0 && TEMPERATURE > 0) {
            posChanged = 0;
            TEMPERATURE--;

            List<CompGroupData> compList = new ArrayList<CompGroupData>(compToData.values());

            // Sort on start position, then on start frame
            compList.sort((c1, c2) -> {
                if (c1.getStartPosition() == c2.getStartPosition()) {
                    return c1.getComp().getStartFrame() - c2.getComp().getStartFrame();
                } else {
                    return c1.getStartPosition() - c2.getStartPosition();
                }
            });

            // iterate through entryset
            for (CompGroupData cgd : compList) {

                Component comp = cgd.getComp();

                List<CompGroupData> aboveComps = cgd.getCompAboveList();
                List<CompGroupData> belowComps = cgd.getCompBelowList();

                List<CompGroupData> currComps = new ArrayList<CompGroupData>();
                for (Component other : components) {
                    if (other.getId() != comp.getId()) {
                        if (other.getStartFrame() <= cgd.getComp().getStartFrame()
                                && other.getEndFrame() >= cgd.getComp().getEndFrame()) {
                            currComps.add(compToData.get(other));
                        }
                    }
                }

                int ub = Math.min(cgd.getStartPosition() + cgd.getComp().getEntities().size(),
                        HEIGHT - cgd.getComp().getEntities().size() - 10);
                int lb = Math.max(cgd.getStartPosition(), 10);

                // For the upperbound, determine group above that is closest to the current
                // group
                for (CompGroupData above : aboveComps) {
                    int abovePos = above.getStartPosition() + above.getComp().getEntities().size();
                    if (abovePos > ub) {
                        ub = abovePos;
                    }

                }

                // For the lowerbound, determine group below that is closest to the current
                // group
                for (CompGroupData below : belowComps) {
                    int belowPos = below.getStartPosition();
                    if (belowPos < lb) {
                        lb = belowPos;
                    }

                }

                double encoded_Pos = cgd.getProjPosition() - cgd.getComp().getEntities().size() / 2;

                double alpha_i = EquationSolver.solveAlpha(

                        aboveComps,
                        belowComps,
                        cgd,
                        encoded_Pos,
                        HEIGHT,
                        cgd.getStartPosition(), // + entities_i,

                        lb,
                        ub,
                        comp.getId());

                if (alpha_i != cgd.getStartPosition()) {
                    cgd.setStartPosition((int) alpha_i);
                    posChanged++;
                }

            }

        }

    }

    /**
     * Sets the projection position of the component.
     * 
     * @param cgd     The CompGroupData object.
     * @param stratid The strategy ID.
     */
    private void setProjectionPosition(CompGroupData cgd, Double[][] projections,
            Pair<Double, Double> minMax) {

        double projValue = findProjectionPosition(cgd, projections);
        double normalizedProjValue = normalizeProjectionPosition(projValue, minMax.getKey(), minMax.getValue());

        cgd.setProjPosition(normalizedProjValue * HEIGHT);

    }

    /**
     * Finds the encoded position of the component.
     * 
     * @param cgd The CompGroupData object.
     */
    private double findProjectionPosition(CompGroupData cgd, Double[][] projections) {

        int projValue = 0;
        int frameCount = 0;

        for (int frame = cgd.getComp().getStartFrame(); frame <= cgd.getComp().getEndFrame(); frame++) {

            double projFrame = 0;
            frameCount++;

            if (MapperUtils.shouldSkipFrameForMotionLines(cgd.getComp(), frame, WIDTH, MOTIONLINES_WIDTH)) {
                continue;
            }

            // Iterate through orderedDataPoints
            for (DataPoint dp : orderedDataPoints[frame]) {
                if (cgd.getComp().getEntities().contains(dp.getId())) {
                    projFrame += projections[frame][dp.getId()];
                }

            }

            projValue += (projFrame / cgd.getComp().getEntities().size());

        }

        projValue = projValue / frameCount;

        return projValue;

    }

    // Function normalizes value between interval [0,1]
    private double normalizeProjectionPosition(double projValue, double min, double max) {

        return (projValue - min) / (max - min);

    }

    // -----------------------------------------------------------------------------
    // FillEntityToPosition and helper methods
    // -----------------------------------------------------------------------------

    /**
     * Fills the entityToPosition array with the screen coordinates of the data
     * based on precomputed component start positions.
     * 
     * @param etp        The entityToPosition array.
     * @param compToData The list of CompGroupData objects.
     */
    private void fillEntityToPosition(Integer[][] etp, HashMap<Component, CompGroupData> compToData) {

        for (HashMap.Entry<Component, CompGroupData> entry : compToData.entrySet()) {
            Component comp = entry.getKey();
            CompGroupData cgd = entry.getValue();

            for (int frame = comp.getStartFrame(); frame <= comp.getEndFrame(); frame++) {

                if (MapperUtils.shouldSkipFrameForMotionLines(comp, frame, WIDTH, MOTIONLINES_WIDTH)) {
                    continue;
                }

                int position = cgd.getStartPosition();

                // Iterate through orderedDataPoints and place entities in the entityToPosition
                for (DataPoint dp : orderedDataPoints[frame]) {
                    if (comp.getEntities().contains(dp.getId())) {
                        etp[frame][dp.getId()] = position;
                        position += 1;
                    }
                }
            }

        }
    }

    /**
     * Sets start positions of the components based on the order of the maximal
     * groups in the last layer they are in.
     * 
     * @param compToData The list of CompGroupData objects.
     */
    private void initializeStartPositionsTest(HashMap<Component, CompGroupData> compToData) {

        int spacing = 5;

        for (MGOrder orderChange : orderedMGroups) {

            int startPosition = 10;

            List<Component> componentsInOrderChange = getComponentsInOrderChange(orderChange);
            boolean[] visited = new boolean[orderChange.getOrder().size()];

            for (Integer maxGroupID : orderChange.getOrder()) {

                if (visited[orderChange.getOrder().indexOf(maxGroupID)]) {
                    continue;
                }

                // get maximal group corresponding to the ID
                MaximalGroup maxGroup = IDToGroupMap.get(maxGroupID);

                // get the component that contains the maximal group
                Component comp = null;
                for (Component c : componentsInOrderChange) {
                    if (c.getEntities().containsAll(maxGroup.getEntities())) {
                        comp = c;

                        // Set components that are visited to true
                        int i = orderChange.getOrder().indexOf(maxGroupID);
                        for (int j = i; j < orderChange.getOrder().size(); j++) {
                            if (c.getEntities()
                                    .containsAll(IDToGroupMap.get(orderChange.getOrder().get(j)).getEntities())) {
                                visited[j] = true;
                            } else {
                                break;
                            }
                        }

                        break;
                    }
                }

                // Continue if component is null
                if (comp == null) {
                    continue;
                }

                CompGroupData compData = compToData.get(comp);

                // set the start position of the component
                if (compData.getStartPosition() == -1) {
                    compData.setStartPosition(startPosition);
                    startPosition += comp.getEntities().size() + spacing;
                }

            }

        }

    }

    /**
     * Sets start positions of the components based on the order of the maximal
     * groups in the last layer they are in.
     * 
     * @param compToData The list of CompGroupData objects.
     */
    private void initializeStartPositions(HashMap<Component, CompGroupData> compToData) {

        for (MGOrder orderChange : orderedMGroups) {

            List<Component> componentsInOrderChange = getComponentsInOrderChange(orderChange);
            boolean[] visited = new boolean[orderChange.getOrder().size()];

            for (Integer maxGroupID : orderChange.getOrder()) {

                if (visited[orderChange.getOrder().indexOf(maxGroupID)]) {
                    continue;
                }

                // get maximal group corresponding to the ID
                MaximalGroup maxGroup = IDToGroupMap.get(maxGroupID);

                // get the component that contains the maximal group
                Component comp = null;
                for (Component c : componentsInOrderChange) {
                    if (c.getEntities().containsAll(maxGroup.getEntities())) {
                        comp = c;

                        // Set components that are visited to true
                        int i = orderChange.getOrder().indexOf(maxGroupID);
                        for (int j = i; j < orderChange.getOrder().size(); j++) {
                            if (c.getEntities()
                                    .containsAll(IDToGroupMap.get(orderChange.getOrder().get(j)).getEntities())) {
                                visited[j] = true;
                            } else {
                                break;
                            }
                        }

                        break;
                    }
                }

                // Continue if component is null
                if (comp == null) {
                    continue;
                }

                CompGroupData compData = compToData.get(comp);

                // set the start position of the component
                if (compData.getStartPosition() == -1) {
                    compData.setStartPosition(0);
                }

            }

        }

    }

    /**
     * Returns the components that exist in the layer corresponding to the order
     * change.
     * 
     * @param orderChange The order change.
     * @return The components that exist in the layer corresponding to the order
     *         change.
     */
    private List<Component> getComponentsInOrderChange(MGOrder orderChange) {

        List<Component> componentsInOrderChange = new ArrayList<>();
        for (Component comp : components) {

            if (comp.getStartFrame() <= orderChange.getLayer()
                    && comp.getEndFrame() >= orderChange.getLayer()) {
                componentsInOrderChange.add(comp);

            }

        }

        return componentsInOrderChange;
    }

    // =========================================================================
    // Debugging Functions
    // =========================================================================
    // Debugging function: Print order of components
    private void printCompOrder(HashMap<Integer, List<Component>> componentOrders) {

        for (int layer : layers) {
            System.out.println("Layer: " + layer);

            HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>();

            for (Component comp : componentOrders.get(layer)) {
                if (countMap.containsKey(comp.getId())) {
                    countMap.put(comp.getId(), countMap.get(comp.getId()) + 1);
                } else {
                    countMap.put(comp.getId(), 1);
                }
                // System.out.print(comp.getId() + " ");
            }

            // Print duplicates
            for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
                if (entry.getValue() > 1) {
                    System.out.println("Duplicate: " + entry.getKey() + " " + entry.getValue() +
                            " in frame " + layer);
                }
            }

            System.out.println();

        }

    }

}
