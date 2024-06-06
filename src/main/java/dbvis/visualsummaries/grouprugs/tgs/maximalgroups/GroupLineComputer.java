package dbvis.visualsummaries.grouprugs.tgs.maximalgroups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dbvis.visualsummaries.grouprugs.tgs.reebgraph.REdge;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.RVertex;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraph;

// TODO: Finish implementation
public class GroupLineComputer {

    public static void compute(ReebGraph reebGraph, int entities, int timestamps) {

        boolean[] visitedEntities = new boolean[entities];
        List<RVertex> startVertices = reebGraph.getStartVertices();

        for (RVertex v : startVertices) {

            for (REdge e : v.getOutEdges()) {

                List<Integer> entitiesInEdge = e.getComponent();
                int entityTracked = entitiesInEdge.get(0);

                RVertex nextVertex = e.getDest();

                // Handle end vertex
                if (nextVertex.getType() == RVertex.END_VERTEX) {

                    // add maximal group with entitiesInEdge and frame == v.getFrame()
                    MaximalGroup maximalGroup = new MaximalGroup(entitiesInEdge, v.getFrame());
                    maximalGroup.setEndFrame(nextVertex.getFrame());

                    // Set all entities in entitiesInEdge to visited
                    for (int entity : entitiesInEdge) {
                        visitedEntities[entity] = true;
                    }

                }

                while (nextVertex.getFrame() != timestamps - 1) {
                    // If next vertex is a merge vertex
                    if (nextVertex.getType() == RVertex.MERGE_VERTEX) {

                        // Nothing happens?

                    }

                }

            }

        }

    }

    public static Set<MaximalGroup> compute2(ReebGraph reebGraph, int entities, int timestamps) {

        Set<MaximalGroup> groupLines = new HashSet<>();
        boolean[] visitedEntities = new boolean[entities];

        // set all entities to not visited
        for (int entity = 0; entity < entities; entity++) {
            visitedEntities[entity] = false;
        }

        List<RVertex> startVertices = reebGraph.getStartVertices();

        for (int entity = 0; entity < entities; entity++) {

            if (visitedEntities[entity]) {
                continue;
            }

            visitedEntities[entity] = true;

            List<Integer> entityComponent = null;
            REdge entityEdge = null;

            // Find initial edge that contains the entity
            for (RVertex v : startVertices) {
                for (REdge e : v.getOutEdges()) {
                    if (e.getComponent().contains(entity)) {
                        entityComponent = e.getComponent();
                        entityEdge = e;
                    }
                }
            }

            while (entityEdge.getDest().getType() != RVertex.END_VERTEX) {

                RVertex nextVertex = entityEdge.getDest();
                boolean found = false;

                int outEdges = nextVertex.getOutEdges().size();
                int iter = 0;

                while (!found && iter < outEdges) {

                    REdge e = nextVertex.getOutEdges().get(iter);

                    if (e.getComponent().contains(entity)) {

                        List<Integer> commonEntities = new ArrayList<>();
                        for (int commonEntity : e.getComponent()) {
                            if (entityComponent.contains(commonEntity)) {
                                commonEntities.add(commonEntity);
                            }
                        }

                        entityComponent = commonEntities;
                        entityEdge = e;
                        found = true;

                    }

                    iter++;

                }

            }

            for (int entityInComponent : entityComponent) {
                visitedEntities[entityInComponent] = true;
            }

            // Add maximal group with entityComponent and frame == startVertex.getFrame()
            MaximalGroup maximalGroup = new MaximalGroup(entityComponent, startVertices.get(0).getFrame());
            maximalGroup.setEndFrame(entityEdge.getDest().getFrame());

            groupLines.add(maximalGroup);

        }

        return groupLines;

    }

}
