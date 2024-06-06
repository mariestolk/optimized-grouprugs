package dbvis.visualsummaries.grouprugs.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Stability_dist {

    /**
     * Computes the stabilityof the group rug.
     * 
     * @param data the data of the group rug
     * @param etp  the EntityToPosition Map for the group rug
     * 
     * @return the stability of the group rug for each frame
     */
    public static double[] computeKS(Integer[][] etp) {

        double[] stability = new double[etp.length - 1];

        for (int frame = 0; frame < etp.length - 1; frame++) {

            Integer[] frame1 = etp[frame];
            Integer[] frame2 = etp[frame + 1];

            Double frameQuality = stabilityFrame(frame1, frame2, frame);
            stability[frame] = frameQuality;

            // System.out.println("Frame " + frame + " Quality: " + frameQuality);
        }

        System.out.println("Stability Dist computed.");

        return stability;

    }

    /**
     * Computes the stability of the group rug for a single frame.
     * 
     * @param data  the data of the group rug for a single frame
     * @param etp2  the EntityToPosition Map for the group rug for a single frame
     * @param frame the frame number
     * 
     * @return the stability of the group rug for a single frame
     */
    private static Double stabilityFrame(Integer[] etp1, Integer[] etp2, int frame) {

        Double summedNumerator = 0.0;
        Double summedDenominator = 0.0;

        int knn = 10;

        for (int id = 0; id < etp1.length; id++) {

            // Integer currentDataPoint = etp1[i];

            int embLocation = etp2[id];

            // Integer currentRank = entityToRank.get(currentDataPoint.getId());

            Integer[] nearestNeighbors = getKNearestNeighbors(etp1, id, knn);

            for (int k = 0; k < knn; k++) {

                int id_neighbor = nearestNeighbors[k];
                int embLocation_neighbor = etp2[id_neighbor];

                Double distance = getEuclideanDistance(etp1, id, id_neighbor);

                Double wij_denominator = distance;

                // Handling edge case: change to rank difference of 1
                if (wij_denominator == 0) {
                    wij_denominator = 1.0;
                }

                double embDiff = Math.abs(embLocation - embLocation_neighbor);

                // Handling edge case: change to rank difference of 1
                if (embDiff == 0) {
                    embDiff = 1.0;
                }

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
    private static Integer[] getKNearestNeighbors(Integer[] etp, Integer id, int k) {

        List<EntityDistancePair> pq = new ArrayList<EntityDistancePair>();

        for (int i = 0; i < etp.length; i++) {

            if (i != id) {

                pq.add(new EntityDistancePair(i, getEuclideanDistance(etp, id, i)));
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

    private static double getEuclideanDistance(Integer[] etp, int current, int neighbor) {

        // Compute Euclidean distance between two points
        double distance = Math.abs(etp[current] - etp[neighbor]);

        return distance;
    }

}
