package dbvis.visualsummaries.grouprugs.visualization.groupselectionstrategies;

import java.util.List;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.*;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.REdge;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.RVertex;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraph;

import java.util.ArrayList;

/**
 * Given a Reeb graph with the entities forming a group on each edge for the
 * duration of this edge, this class returns each of these groups as
 * Component objects.
 * 
 * Note: a maximal group takes from its start event up to and including its end
 * event - 1.
 */
public class ComponentSelectionStrategy {

    public String getName() {
        return "Component Selection Strategy";
    }

    public String getSimpleName() {
        return "component";
    }

    public static List<Component> selectGroups(ReebGraph rg) {

        List<Component> selectedGroups = new ArrayList<Component>();
        List<RVertex> topoSort = rg.getVertices();

        // Maximal Groups are created from components
        for (RVertex u : topoSort) {

            for (REdge e : u.getOutEdges()) {

                List<Integer> component = e.getComponent();
                Component mg = new Component(component, u.getFrame());

                RVertex v = e.getDest();

                if (v.getType() == "end") {
                    mg.setEndFrame(v.getFrame());
                } else {
                    mg.setEndFrame(v.getFrame() - 1);
                }

                selectedGroups.add(mg);

            }

        }

        return selectedGroups;

    }

}
