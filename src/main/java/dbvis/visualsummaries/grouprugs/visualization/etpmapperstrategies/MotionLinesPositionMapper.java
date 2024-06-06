package dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies;

import dbvis.visualsummaries.color.TwoDColorMapper;
import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.grouprugs.tgs.Utils;
import dbvis.visualsummaries.strategies.Strategy;

public class MotionLinesPositionMapper {

    private int MOTIONLINES_WIDTH;
    private int frames;
    private int entities;

    public MotionLinesPositionMapper(int motionlines_width, int frames, int entities) {
        this.MOTIONLINES_WIDTH = motionlines_width;
        this.frames = frames;
        this.entities = entities;
    }

    /**
     * Method to position the entities in the visualization using the motionlines
     * strategy. The entityToPosition map is used to store the position of the
     * entities in the visualization. The method returns the entityToPosition map.
     * 
     * @param selectedStrategy
     * @param sd
     * @param dsname
     * @param ENLARGEMENT_FACTOR
     * 
     * @return The entityToPosition A map that contains the position of the entities
     */
    public Integer[][] motionlinesPositioning(
            Strategy selectedStrategy,
            SessionData sd,
            String dsname,
            Integer ENLARGEMENT_FACTOR) {
        Integer[][] entityToPosition = new Integer[frames][entities];
        String stratid = selectedStrategy.getName();

        DataSet current = sd.getDataset(dsname);
        DataPoint[][] data = current.getBaseData();

        // Initialize color mappers
        TwoDColorMapper twodcolormapper = new TwoDColorMapper();

        int maxx = (int) Math.ceil(current.getMax("x"));
        int maxy = (int) Math.ceil(current.getMax("y"));
        twodcolormapper.setScale(maxx, maxy);

        DataPoint[][] orderedPointsFull = MapperUtils.readOrderedPoint(sd, dsname, selectedStrategy);

        Double[][] projections = MapperUtils.readProjections(stratid, dsname, data.length, data[0].length);

        // Get maximum and minimum projection values
        Double mini = Double.MAX_VALUE;
        Double maxi = Double.MIN_VALUE;
        for (Double[] frame : projections) {
            for (Double entity_projection : frame) {
                if (mini > entity_projection) {
                    mini = entity_projection;
                }

                if (maxi < entity_projection) {
                    maxi = entity_projection;
                }

            }
        }

        // Get dimensions of image
        int WIDTH = data.length;
        int HEIGHT = ENLARGEMENT_FACTOR * data[0].length;

        int numEntities = data[0].length;

        for (int frame = 0; frame < WIDTH; frame++) {

            for (int entity = 0; entity < numEntities; entity++) {

                double y = projections[frame][entity];

                // Normalize position
                int position = (int) (((y - mini) / (maxi - mini)) * (HEIGHT - 1));

                // Store position in entityToPosition
                entityToPosition[frame][entity] = position;
            }

        }

        return entityToPosition;
    }

}
