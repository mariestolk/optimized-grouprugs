package dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies;

import java.util.List;

import dbvis.visualsummaries.color.TwoDColorMapper;
import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.Component;
import dbvis.visualsummaries.strategies.Strategy;
import javafx.util.Pair;

public class FuzzyPositionMapper {

    private int MOTIONLINES_WIDTH;
    private int frames;
    private int entities;

    public FuzzyPositionMapper(int motionlines_width, int frames, int entities) {
        this.MOTIONLINES_WIDTH = motionlines_width;
        this.frames = frames;
        this.entities = entities;
    }

    /**
     * Method to position the entities in the visualization using the fuzzy
     * positioning strategy. This method corresponds to naive GroupRugs.
     * The entityToPosition map is used to store the position of the
     * entities in the visualization. The method returns the entityToPosition map.
     * 
     * @param maximalGroupToDataMap
     * @param selectedStrategy
     * @param sd
     * @param dsname
     * @param ENLARGEMENT_FACTOR
     * 
     * @return
     * 
     * @throws Exception
     */
    public Integer[][] fuzzyPositioning(
            List<Component> components,
            Strategy selectedStrategy,
            SessionData sd,
            String dsname,
            Integer ENLARGEMENT_FACTOR

    ) throws Exception {
        Integer[][] entityToPosition = new Integer[frames][entities];
        String stratid = selectedStrategy.getName();

        DataSet current = sd.getDataset(dsname);
        DataPoint[][] data = current.getBaseData();

        // Initialize color mappers
        TwoDColorMapper twodcolormapper = new TwoDColorMapper();

        int maxx = (int) Math.ceil(current.getMax("x"));
        int maxy = (int) Math.ceil(current.getMax("y"));
        twodcolormapper.setScale(maxx, maxy);

        // DataPoint[][] orderedPointsFull = selectedStrategy.getOrderedValues(data,
        // dsname);
        DataPoint[][] orderedPointsFull = MapperUtils.readOrderedPoint(sd, dsname, selectedStrategy);
        Double[][] projections = MapperUtils.readProjections(stratid, dsname, data.length, data[0].length);
        Pair<Double, Double> minmax = MapperUtils.getMinMaxProjections(projections);

        int WIDTH = data.length;
        int HEIGHT = ENLARGEMENT_FACTOR * data[0].length;

        // Find current groups
        for (int frame = 0; frame < WIDTH; frame++) {

            List<Component> currentGroups = MapperUtils.computeCurrentGroups(frame, components);

            for (Component component : currentGroups) {

                if (MapperUtils.shouldSkipFrameForMotionLines(component, frame, WIDTH, MOTIONLINES_WIDTH)) {
                    continue;
                }

                // Find average position of this group in this frame in projection space
                double cumulativeGroupPosition = 0;

                for (int entity = 0; entity < component.getEntities().size(); entity++) {
                    int entityId = component.getEntities().get(entity);
                    cumulativeGroupPosition = cumulativeGroupPosition + projections[frame][entityId];
                }

                // Get the average position of this group in this frame
                double avgProjectedGroupPosition = (cumulativeGroupPosition / component.getEntities().size());

                // Normalize the average position to a pixel in the range [0, HEIGHT-1]
                int groupCenter = (int) ((avgProjectedGroupPosition - minmax.getKey())
                        / (minmax.getValue() - minmax.getKey())
                        * (HEIGHT - 1));

                int groupStart = groupCenter - (component.getEntities().size() / 2) - 1;
                if (groupStart < 0) {
                    groupStart = 0;
                } else if (groupStart > (HEIGHT - 1) - component.getEntities().size()) {
                    groupStart = (HEIGHT - 1) - component.getEntities().size();
                }

                int groupcount = 0;

                for (int i = 0; i < orderedPointsFull[frame].length; i++) {

                    if (component.getEntities().contains(orderedPointsFull[frame][i].getId())) {

                        double entityLoc = groupStart + (groupcount);

                        int entityLocInt;

                        // round to nearest integer
                        if (entityLoc < groupCenter) {
                            entityLocInt = (int) Math.ceil(entityLoc);
                        } else {
                            entityLocInt = (int) Math.floor(entityLoc);
                        }

                        // Store entityLocInt in entityToPosition
                        entityToPosition[frame][orderedPointsFull[frame][i].getId()] = entityLocInt;

                        groupcount += 1;

                    }

                }
            }
        }

        // Draw MotionLines transition
        MapperUtils.drawMotionLines2(entityToPosition, orderedPointsFull, data, stratid, dsname, HEIGHT);

        return entityToPosition;

    }

    /**
     * Draws FuzzyRugs for the visualization.
     * 
     * @param entityToPosition  The entityToPosition map
     * @param orderedPointsFull The ordered points
     * @param projections       The projections
     * @param maximalGroups     The maximal groups
     * @param minmax            The minmax pair
     * @param HEIGHT            The height of the visualization
     */
    private void drawRugs(
            Integer[][] entityToPosition,
            DataPoint[][] orderedPointsFull,
            Double[][] projections,
            List<Component> maximalGroups,
            Pair<Double, Double> minmax,
            int HEIGHT) {

        int WIDTH = orderedPointsFull.length;

        // Find current groups
        for (int frame = 0; frame < WIDTH; frame++) {

            drawStrip(
                    entityToPosition,
                    orderedPointsFull,
                    projections,
                    maximalGroups,
                    minmax,
                    frame,
                    HEIGHT);

        }

    }

    /**
     * Draws one vertical strip of the rug for a given frame.
     * 
     * @param entityToPosition  The entityToPosition map
     * @param orderedPointsFull The ordered points
     * @param projections       The projections
     * @param maximalGroups     The maximal groups
     * @param minmax            The minmax pair
     * @param frame             The frame
     * @param HEIGHT            The height of the visualization
     */
    private void drawStrip(
            Integer[][] entityToPosition,
            DataPoint[][] orderedPointsFull,
            Double[][] projections,
            List<Component> maximalGroups,
            Pair<Double, Double> minmax,
            int frame,
            int HEIGHT) {

        int WIDTH = orderedPointsFull.length;

        List<Component> currentGroups = MapperUtils.computeCurrentGroups(frame, maximalGroups);

        for (Component maximalGroup : currentGroups) {
            if (MapperUtils.shouldSkipFrameForMotionLines(maximalGroup, frame, WIDTH, MOTIONLINES_WIDTH)) {
                continue;
            } else {
                drawComponentStrip(
                        maximalGroup,
                        entityToPosition,
                        orderedPointsFull,
                        projections,
                        minmax,
                        frame,
                        HEIGHT,
                        WIDTH);
            }

        }

    }

    /**
     * Draws one vertical strip for a specific component in a given frame.
     * 
     * @param component         The maximal group
     * @param entityToPosition  The entityToPosition map
     * @param orderedPointsFull The ordered points
     * @param projections       The projections
     * @param minmax            The minmax pair
     * @param frame             The frame
     * @param HEIGHT            The height of the visualization
     * @param WIDTH             The width of the visualization
     */
    private void drawComponentStrip(
            Component component,
            Integer[][] entityToPosition,
            DataPoint[][] orderedPointsFull,
            Double[][] projections,
            Pair<Double, Double> minmax,

            int frame,
            int HEIGHT,
            int WIDTH) {

        int groupCenter = computeGroupCenter(component, projections, minmax, frame, HEIGHT);
        int groupStart = computeGroupStart(component, groupCenter);
        int groupCount = 0;

        for (int entity = 0; entity < orderedPointsFull[frame].length; entity++) {

            // If entity is part of the component draw it
            if (component.getEntities().contains(orderedPointsFull[frame][entity].getId())) {

                int entityLoc = computeEntityLoc(
                        component,
                        orderedPointsFull,
                        frame,
                        groupCenter,
                        groupStart,
                        groupCount,
                        entity,
                        HEIGHT);

                // Store entityLocInt in entityToPosition
                entityToPosition[frame][orderedPointsFull[frame][entity].getId()] = entityLoc;

                groupCount += 1;

            }
        }
    }

    /**
     * Compute the average position of each maximal group in the projections.
     * 
     * @param maximalGroup The maximal group
     * @param projections  The projections
     * @param minmax       The minmax pair
     * @param frame        The frame
     * @param HEIGHT       The height of the visualization
     * @return The group center
     */
    private Integer computeGroupCenter(
            Component maximalGroup,
            Double[][] projections,
            Pair<Double, Double> minmax,
            int frame,
            int HEIGHT) {
        // Find average position of this group in this frame in projection space
        double cumulativeGroupPosition = 0;

        for (int entity = 0; entity < maximalGroup.getEntities().size(); entity++) {
            int entityId = maximalGroup.getEntities().get(entity);
            cumulativeGroupPosition = cumulativeGroupPosition + projections[frame][entityId];
        }

        // Get the average position of this group in this frame
        double avgProjectedGroupPosition = (cumulativeGroupPosition / maximalGroup.getEntities().size());

        // Normalize the average position to a pixel in the range [0, HEIGHT-1]
        int groupCenter = (int) ((avgProjectedGroupPosition - minmax.getKey())
                / (minmax.getValue() - minmax.getKey())
                * (HEIGHT - 1));

        return groupCenter;

    }

    /**
     * Compute the start of the group, the lowest y-coordinate of the group.
     * 
     * @param component
     * @param groupCenter
     * @return The group start location
     */
    private Integer computeGroupStart(
            Component component,
            int groupCenter) {

        int groupStart = groupCenter + (component.getEntities().size() / 2);

        return groupStart;
    }

    /**
     * Compute the location of an entity in the visualization.
     * 
     * @param component         The component
     * @param orderedPointsFull The ordered points
     * @param frame             The frame
     * @param groupCenter       The group center
     * @param groupStart        The group start
     * @param groupCount        The group count
     * @param entity            The entity
     * @param HEIGHT            The height of the visualization
     * @return The entity location
     */
    private Integer computeEntityLoc(
            Component component,
            DataPoint[][] orderedPointsFull,
            int frame,
            int groupCenter,
            int groupStart,
            int groupCount,
            int entity,
            int HEIGHT) {
        double entityLoc = groupStart - (0.5) * component.getEntities().size() + groupCount;

        int entityLocInt;

        // round to nearest integer
        if (entityLoc < groupCenter) {
            entityLocInt = (int) Math.ceil(entityLoc);
        } else {
            entityLocInt = (int) Math.floor(entityLoc);
        }

        return entityLocInt;
    }

}
