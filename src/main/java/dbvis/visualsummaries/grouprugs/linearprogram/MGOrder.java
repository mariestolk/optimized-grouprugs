package dbvis.visualsummaries.grouprugs.linearprogram;

import java.util.List;

public class MGOrder {

    private int layer;
    private List<Integer> order;

    public MGOrder(int layer, List<Integer> order) {
        this.layer = layer;
        this.order = order;
    }

    public int getLayer() {
        return layer;
    }

    public List<Integer> getOrder() {
        return order;
    }

}
