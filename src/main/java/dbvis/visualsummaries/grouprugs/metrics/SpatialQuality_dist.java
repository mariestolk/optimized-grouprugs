package dbvis.visualsummaries.grouprugs.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.grouprugs.tgs.Utils;

public class SpatialQuality_dist {

    /**
     * Computes the spatial quality of the group rug.
     * 
     * @param data the data of the group rug
     * @param etp  the EntityToPosition Map for the group rug
     * 
     * @return the spatial quality of the group rug for each frame
     */
    public static double[] computeKS(DataPoint[][] data, Integer[][] etp, String metricType) {

        double[] spatialQuality = new double[data.length];

        for (int frame = 0; frame < data.length; frame++) {
            Double frameQuality = spatialQualityFrame(data[frame], etp[frame], frame, metricType);
            spatialQuality[frame] = frameQuality;

            // System.out.println("Frame " + frame + " Quality: " + frameQuality);
        }

        System.out.println("Spatial Quality computed.");

        return spatialQuality;

    }

    /**
     * Computes the spatial quality of the group rug for a single frame.
     * 
     * @param data  the data of the group rug for a single frame
     * @param etp   the EntityToPosition Map for the group rug for a single frame
     * @param frame the frame number
     * 
     * @return the spatial quality of the group rug for a single frame
     */
    private static Double spatialQualityFrame(DataPoint[] data, Integer[] etp, int frame, String metricType) {

        Double summedNumerator = 0.0;
        Double summedDenominator = 0.0;

        int knn = 10;

        for (int i = 0; i < data.length; i++) {

            DataPoint currentDataPoint = data[i];
            int id = currentDataPoint.getId();

            int embLocation = etp[id];

            // Integer currentRank = entityToRank.get(currentDataPoint.getId());

            Integer[] nearestNeighbors = getKNearestNeighbors(data, currentDataPoint, knn);

            for (int k = 0; k < knn; k++) {

                DataPoint neighbor = data[nearestNeighbors[k]];
                int id_neighbor = neighbor.getId();

                int embLocation_neighbor = etp[id_neighbor];

                Double distance = Utils.getEuclideanDistance(currentDataPoint, neighbor);

                Double wij_denominator = 0.0;
                switch (metricType) {
                    case "distance":
                        wij_denominator = distance;
                        break;
                    case "rank":
                        wij_denominator = (double) (k + 1);
                        break;
                    default:
                        System.out.println(
                                "Invalid metric type. Choose \"distance\" or \"rank\". Defaulting to distance.");
                        wij_denominator = distance;
                        break;
                }

                // Handling edge case: make distance small number to avoid division by zero
                if (distance == 0) {
                    distance = 0.0000000001;
                }

                Integer embDiff = Math.abs(embLocation - embLocation_neighbor);

                double wij = 1 / (wij_denominator); // Distance-weighted Keys Similarity
                double dij = embDiff;

                summedNumerator += wij * dij;
                summedDenominator += wij;
            }

        }

        Double quality = summedNumerator / summedDenominator;

        return quality;
    }

    /**
     * Returns the k nearest neighbors of a data point.
     * 
     * @param data The data points.
     * @param dp   The data point.
     * @param k    The number of nearest neighbors to return.
     * @return The k nearest neighbors of the data point.
     */
    private static Integer[] getKNearestNeighbors(DataPoint[] data, DataPoint dp, int k) {

        List<EntityDistancePair> pq = new ArrayList<EntityDistancePair>();

        for (int i = 0; i < data.length; i++) {

            if (data[i].getId() != dp.getId()) {

                pq.add(new EntityDistancePair(data[i].getId(), Utils.getEuclideanDistance(dp, data[i])));
            }
        }

        // Sort on distance
        Collections.sort(pq, new Comparator<EntityDistancePair>() {
            @Override
            public int compare(EntityDistancePair o1, EntityDistancePair o2) {
                return o1.distance.compareTo(o2.distance);
            }
        });

        Integer[] nearestNeighbors = new Integer[k];

        for (int i = 0; i < k; i++) {
            nearestNeighbors[i] = pq.get(i).entity;
        }

        return nearestNeighbors;

    }

    /**
     * A simple class to represent a pair of an entity and its distance. Used for
     * sorting entities based on their distance in original data space.
     */
    private static class EntityDistancePair {

        public Integer entity;
        public Double distance;

        public EntityDistancePair(Integer entity, Double distance) {
            this.entity = entity;
            this.distance = distance;
        }

    }

}
