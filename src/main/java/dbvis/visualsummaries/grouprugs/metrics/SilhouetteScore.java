package dbvis.visualsummaries.grouprugs.metrics;

import java.util.ArrayList;
import java.util.List;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.Component;

public class SilhouetteScore {

    /**
     * Computes the silhouette score for a given clustering over all frames.
     * 
     * @param clustering The clustering for which the silhouette score is to be
     *                   computed.
     * @param etp        The entityToPosition map that contains the entities for
     *                   each frame.
     * 
     * @return The silhouette scores for each frame.
     */
    public static double[] computeSilhouette(
            List<Component> clustering,
            Integer[][] etp,
            int HEIGHT) {

        double[] silhouetteScores = new double[etp.length];

        for (int frame = 0; frame < etp.length; frame++) {
            double silhouetteScoreFrame = silhouetteScoreFrame(clustering, etp, frame, HEIGHT);

            silhouetteScores[frame] = silhouetteScoreFrame;

        }

        System.out.println("Silhouette Scores computed.");

        return silhouetteScores;

    }

    /**
     * Computes the silhouette score for a given clustering in a specific frame.
     * 
     * @param clustering The clustering for which the silhouette score is to be
     *                   computed.
     * @param frame      The frame in which the entities are located.
     * @return The silhouette score.
     */
    public static double silhouetteScoreFrame(
            List<Component> clustering,
            Integer[][] etp,
            Integer frame,
            Integer HEIGHT) {

        Integer[] framedata = etp[frame];

        double silhouetteScoreSum = 0;

        for (int entity = 0; entity < etp[frame].length; entity++) {

            List<Integer> ownCluster = getOwnClusterIDs(clustering, framedata, frame, entity);
            List<Integer> otherCluster = getNearestClusterIDs(clustering, framedata, frame, entity);

            silhouetteScoreSum += silhouetteScoreEntity(ownCluster, otherCluster, framedata, frame, entity, HEIGHT);

        }

        double silhouetteScore = silhouetteScoreSum / etp[frame].length;

        return silhouetteScore;

    }

    /**
     * Computes the SilhouetteScore for one entity.
     * 
     * @param clustering The clustering for which the silhouette score is to be
     *                   computed.
     * @param framedata  The frame in the entityToPosition map that contains the
     *                   entities for this frame.
     * @param frame      The frame in which the entities are located.
     * @param entity     The entity for which the silhouette score is to be
     *                   computed.
     * 
     * @return The silhouette score for the entity.
     */
    private static double silhouetteScoreEntity(
            List<Integer> ownCluster,
            List<Integer> otherCluster,
            Integer[] framedata,
            Integer frame,
            Integer entity,
            Integer HEIGHT) {

        double a = computeA(entity, ownCluster, framedata);
        double b = computeB(entity, otherCluster, framedata, HEIGHT);

        // Small offset in case both distances are 0.
        if (a == 0 && b == 0) {
            b = 0.0001;
        }

        return (b - a) / Math.max(a, b);
    }

    /**
     * Computes the intra-cluster distance for a given entity.
     * 
     * @param entity     The entity for which the intra-cluster distance is to be
     *                   computed.
     * @param ownCluster The entity IDs of the own cluster.
     * @param framedata  The frame in which the entities are located.
     * @return The intra-cluster distance.
     */
    private static double computeA(
            Integer entity,
            List<Integer> ownCluster,
            Integer[] framedata) {

        double sum = 0;
        for (Integer e = 0; e < framedata.length; e++) {
            if (ownCluster.contains(e)) {
                sum += Math.abs(framedata[entity] - framedata[e]);
            }
        }

        return sum / ownCluster.size();

    }

    /**
     * Computes the inter-cluster distance for a given entity.
     * 
     * @param entity       The entity for which the inter-cluster distance is to be
     *                     computed.
     * @param otherCluster The entity IDs of the nearest other cluster.
     * @param framedata    The frame in which the entities are located.
     * @return The inter-cluster distance.
     */
    private static double computeB(
            Integer entity,
            List<Integer> otherCluster,
            Integer[] framedata,
            Integer HEIGHT) {

        // Edge Case: If there is only one encompassing group, pick the closest border
        // as 'dummy cluster' as average distance.
        if (otherCluster == null) {

            double distTop = Math.abs(framedata[entity] - 0);
            double distBottom = Math.abs(framedata[entity] - HEIGHT);

            return Math.min(distTop, distBottom);

        }

        // Regular case: Compute average distance to other cluster entities.
        double sum = 0;
        for (Integer e = 0; e < framedata.length; e++) {
            if (otherCluster.contains(e)) {
                sum += Math.abs(framedata[entity] - framedata[e]);
            }
        }

        return sum / otherCluster.size();

    }

    /**
     * Returns list of IDs of cluster to which the entity belongs.
     * 
     * @param entity    The entity for which the cluster is to be determined.
     * @param framedata The frame in the entityToPosition map that contains the
     *                  entities for this frame.
     * @param frame     The frame in which the entities are located.
     * 
     * @return The cluster to which the entity belongs.
     */
    private static List<Integer> getOwnClusterIDs(
            List<Component> maximalGroups,
            Integer[] framedata,
            Integer frame,
            Integer entity) {

        List<Integer> ownCluster = null;

        for (Component mg : maximalGroups) {
            if (mg.getStartFrame() <= frame) {
                if (mg.getEndFrame() >= frame) {
                    if (mg.getEntities().contains(entity)) {
                        ownCluster = mg.getEntities();
                        break;
                    }
                }
            }
        }

        if (ownCluster == null) {
            System.out.println("Entity " + entity + " not found in any cluster. Making own cluster.");

            ownCluster = new ArrayList<Integer>();
            ownCluster.add(entity);
        }

        return ownCluster;

    }

    /**
     * Returns list of IDs of nearest cluster to which the entity does not belong.
     * If all entities are in the same cluster, returns the closest distance to the
     * top or bottom of the frame.
     * 
     * @param maximalGroups The list of maximal groups.
     * @param framedata     The frame in the entityToPosition map that contains the
     *                      entities for this frame.
     * @param frame         The frame in which the entities are located.
     * @param entity        The entity for which the nearest cluster is to be
     *                      determined.
     * 
     * @return The nearest cluster to which the entity does not belong.
     */
    private static List<Integer> getNearestClusterIDs(
            List<Component> maximalGroups,
            Integer[] framedata,
            Integer frame,
            Integer entity) {

        List<Integer> nearestCluster = null;
        double minDistance = Double.MAX_VALUE;

        // Create boolean visited array
        boolean[] visited = new boolean[framedata.length];

        // For each cluster in the frame compute the average distance to the entity
        for (Component mg : maximalGroups) {

            for (Integer e : mg.getEntities()) {
                visited[e] = true;
            }

            if (mg.getStartFrame() <= frame
                    && mg.getEndFrame() >= frame
                    && !mg.getEntities().contains(entity)) {

                double sum = 0;
                for (Integer e : mg.getEntities()) {
                    sum += Math.abs(framedata[entity] - framedata[e]);
                }

                double avgDistance = sum / mg.getEntities().size();

                // If the average distance is smaller than the current minimum distance, update
                // the minimum distance and the nearest cluster
                if (avgDistance < minDistance) {
                    minDistance = avgDistance;
                    nearestCluster = mg.getEntities();
                }

            }
        }

        // Edge case: individual entity in MotionLines that is not part of any cluster
        // is closest.
        for (int i = 0; i < visited.length; i++) {
            if (!visited[i]) {
                double dist = Math.abs(framedata[entity] - framedata[i]);

                if (dist < minDistance) {
                    minDistance = dist;

                    nearestCluster = new ArrayList<Integer>();
                    nearestCluster.add(i);
                }
            }
        }

        return nearestCluster;

    }

}