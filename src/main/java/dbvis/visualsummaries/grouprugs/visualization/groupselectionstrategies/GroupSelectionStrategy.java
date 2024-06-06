

package dbvis.visualsummaries.grouprugs.visualization.groupselectionstrategies;

import java.util.List;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroup;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraph;

public interface GroupSelectionStrategy {

    public static List<MaximalGroup> selectGroups(ReebGraph rg) {
        return null;
    }

    public String getName();

    public String getSimpleName();

}