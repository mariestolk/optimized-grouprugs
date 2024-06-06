package dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.strategies.Strategy;

public class MotionRugsPositionMapper {

    public Integer[][] position(SessionData sd, DataPoint[][] data, Strategy selectedStrategy, String dsname) {

        Integer[][] etp = new Integer[data.length][data[0].length];

        // =====================================================================
        DataPoint[][] orderedPointsFull = MapperUtils.readOrderedPoint(sd, dsname, selectedStrategy);
        // =====================================================================

        for (int frame = 0; frame < orderedPointsFull.length; frame++) {
            for (int location = 0; location < orderedPointsFull[frame].length; location++) {

                int id = orderedPointsFull[frame][location].getId();
                etp[frame][id] = location;

            }

        }

        return etp;

    }

}
