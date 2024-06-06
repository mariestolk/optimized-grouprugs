package dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.placementheuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dbvis.visualsummaries.grouprugs.linearprogram.MGOrder;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.Component;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.MaximalGroup;
import dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.MapperUtils;

public class ComponentOrderComputer {

    /**
     * Computes the order of components for each layer based on the given MGOrder
     * per layer.
     * 
     * @param mgOrder      The MGOrder per layer
     * @param components   The components
     * @param IDToGroupMap The mapping of entity IDs to group IDs
     * @return The order of components per layer
     */
    public static HashMap<Integer, List<Component>> computeComponentOrder(List<MGOrder> mgOrder,
            List<Component> components, Map<Integer, MaximalGroup> IDToGroupMap) {

        HashMap<Integer, List<Component>> compOrder = new HashMap<Integer, List<Component>>();

        for (MGOrder mg_order : mgOrder) {

            boolean[] visited = new boolean[mg_order.getOrder().size()];

            int layer = mg_order.getLayer();
            List<Component> order = new ArrayList<Component>();

            // Get components that exist during this layer
            List<Component> currComponents = MapperUtils.computeCurrentGroups(layer, components);
            boolean[] visitedComps = new boolean[currComponents.size()];

            for (int i : mg_order.getOrder()) {

                if (visited[mg_order.getOrder().indexOf(i)]) {
                    continue;
                }

                for (Component comp : currComponents) {

                    if (comp.getEntities().containsAll(IDToGroupMap.get(i).getEntities())) {
                        order.add(comp);
                        visitedComps[currComponents.indexOf(comp)] = true;
                    }

                    for (int j = mg_order.getOrder().indexOf(i); j < mg_order.getOrder().size(); j++) {
                        if (comp.getEntities()
                                .containsAll(IDToGroupMap.get(mg_order.getOrder().get(j)).getEntities())) {
                            visited[j] = true;
                        } else {
                            break;
                        }

                    }

                }

            }

            // Check if all components are added. If not, print missing components.
            for (int i = 0; i < currComponents.size(); i++) {
                if (!visitedComps[i]) {
                    System.out.println("PROBLEM, missed Component: " + currComponents.get(i));
                }
            }

            compOrder.put(layer, order);

        }

        return compOrder;

    }

}
