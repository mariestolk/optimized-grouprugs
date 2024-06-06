package dbvis.visualsummaries.grouprugs.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.grouprugs.tgs.Utils;

public class SpatialQuality {

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
            double frameQuality = spatialQualityFrame(data[frame], etp[frame], frame, metricType);
            spatialQuality[frame] = frameQuality;

            // System.out.println("Frame " + frame + " Quality: " + frameQuality);
        }

        System.out.println("Spatial Quality Computed.");

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
        double summedDenominator = 0.0;

        int knn = 10;

        HashMap<Integer, Integer> entityToRank = computeEntityToRankMapping(etp);

        for (int i = 0; i < data.length; i++) {

            DataPoint currentDataPoint = data[i];
            Integer currentRank = entityToRank.get(currentDataPoint.getId());

            Integer[] nearestNeighbors = getKNearestNeighbors(data, currentDataPoint, knn);

            for (int k = 0; k < knn; k++) {

                DataPoint neighbor = data[nearestNeighbors[k]];

                Integer rank = entityToRank.get(neighbor.getId());
                double distance = Utils.getEuclideanDistance(currentDataPoint, neighbor);

                double wij_denominator = 0.0;
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

                Integer rankdiff = Math.abs(rank - currentRank);

                double wij = 1 / (wij_denominator); // Distance-weighted Keys Similarity
                double rij = rankdiff;

                summedNumerator += wij * rij;
                summedDenominator += wij;
            }

        }

        double quality = summedNumerator / summedDenominator;

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
     * Computes a mapping from entity to rank based on the etp array. Handles
     * non-unique positions.
     * 
     * @param etp The EntityToPosition array where index represents the entity and
     *            value represents its y-coordinate.
     * @return A map where each key is an entity index and each value is the rank of
     *         that entity.
     */
    private static HashMap<Integer, Integer> computeEntityToRankMapping(Integer[] etp) {

        List<EntityProjectionPair> pq = new ArrayList<EntityProjectionPair>();

        for (int i = 0; i < etp.length; i++) {
            pq.add(new EntityProjectionPair(i, (double) etp[i]));
        }

        // Sort on etp value
        Collections.sort(pq, new Comparator<EntityProjectionPair>() {
            @Override
            public int compare(EntityProjectionPair o1, EntityProjectionPair o2) {
                return o1.etpValue.compareTo(o2.etpValue);
            }
        });

        HashMap<Integer, Integer> entityToRank = new HashMap<Integer, Integer>();

        for (int i = 0; i < pq.size(); i++) {
            entityToRank.put(pq.get(i).entity, i);
        }

        return entityToRank;
    }

    /**
     * A simple class to represent a pair of an entity and its projection value.
     * Used for sorting entities based on their projection value.
     */
    private static class EntityProjectionPair {

        public Integer entity;
        public Double etpValue;

        public EntityProjectionPair(Integer entity, Double etpValue) {
            this.entity = entity;
            this.etpValue = etpValue;
        }

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

    // Simple test instance with an image of 10 by 10, projected along the x-axis.
    public static void main(String[] args) {
        DataPoint[][] data = new DataPoint[10][10];

        Random random = new Random();

        // x is random number between 0 and 10
        int x = random.nextInt(10);
        // y is random number between 0 and 10
        int y = random.nextInt(10);

        for (int frame = 0; frame < 10; frame++) {
            data[frame] = new DataPoint[10];
            for (int j = 0; j < 10; j++) {
                data[frame][j] = new DataPoint(x, y, j);

                // x is random number between 0 and 10
                x = random.nextInt(10);
                // y is random number between 0 and 10
                y = random.nextInt(10);
            }
        }

        // Print matrix
        for (int i = 0; i < 10; i++) {
            System.out.print(" frame " + i + " [");
            for (int j = 0; j < 10; j++) {
                System.out.print("(" + data[i][j].getX() + "," + data[i][j].getY() + "), ");
            }
            System.out.println("]");
        }

        Integer[][] etp = new Integer[10][10];

        // Project along x-axis
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                etp[i][j] = (int) data[i][j].getX();
            }
        }

        // Print matrix
        for (int i = 0; i < 10; i++) {
            System.out.print("[");
            for (int j = 0; j < 10; j++) {
                System.out.print(etp[i][j] + ", ");
            }
            System.out.println("]");
        }

        // double[] spatialQuality = computeKS(data, etp, "distance");

        // for (int i = 0; i < spatialQuality.length; i++) {
        // System.out.println("Frame " + i + " Quality: " + spatialQuality[i]);
        // }
    }

}
