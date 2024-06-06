package dbvis.visualsummaries.grouprugs.visualization;

import java.util.ArrayList;
import java.util.List;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.Component;

public class Postprocessing {

    /**
     * Post-process the entityToPosition map. For each pair of frames check if there
     * are fewer individual edge crossings if we swap the positions of two entities
     * within a group. Updates the entityToPosition map accordingly.
     * 
     * @param components The maximal groups
     * @param etpMap     The entityToPosition map
     */
    public static void postProcess(List<Component> components, Integer[][] etpMap) {

        for (Component component : components) {

            int startFrame = component.getStartFrame();
            int endFrame = component.getEndFrame();

            for (int frame = startFrame; frame < endFrame; frame++) {

                int crossingsBefore = 0;
                int crossingsAfter = 0;

                // First pass to count crossings before and after swapping
                for (int entity1 : component.getEntities()) {
                    for (int entity2 : component.getEntities()) {

                        if (entity1 == entity2) {
                            continue;
                        }

                        int entity1Pos = etpMap[frame][entity1];
                        int entity2Pos = etpMap[frame][entity2];

                        int entity1PosNext = etpMap[frame + 1][entity1];
                        int entity2PosNext = etpMap[frame + 1][entity2];

                        if (entity1Pos < entity2Pos && entity1PosNext > entity2PosNext
                                || entity1Pos > entity2Pos && entity1PosNext < entity2PosNext) {
                            crossingsBefore++;
                        } else {
                            crossingsAfter++;
                        }

                    }
                }

                if (crossingsAfter < crossingsBefore) {

                    // Find current order of entities
                    List<Integer> currentOrder = new ArrayList<>();
                    List<Integer> orderedPositions = new ArrayList<>();
                    for (int entity : component.getEntities()) {
                        currentOrder.add(entity);
                        orderedPositions.add(etpMap[frame][entity]);
                    }

                    // Iterate through the current order map reversed
                    for (int i = currentOrder.size() - 1; i >= 0; i--) {
                        int entity = currentOrder.get(i);
                        // position is orderedPositions.size - 1 - i
                        int position = orderedPositions.get(orderedPositions.size() - 1 - i);

                        etpMap[frame][entity] = position; // Swap positions
                    }

                } // else do nothing
            }
        }
    }

}
