package dbvis.visualsummaries.grouprugs.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.*;

public class Stability {

    // Function to compute KS for stability metric
    public static double[] computeKSte(Integer[][] frames, int k) {

        double[] stability = new double[frames.length - 1];

        for (int l = 0; l < frames.length - 1; l++) {

            double totalNumerator = 0;
            double totalDenominator = 0;

            Integer[] currentFrame = frames[l];
            Integer[] nextFrame = frames[l + 1];
            Map<Integer, List<RankDiff>> currentKNN = getKNN(currentFrame, k);
            Map<Integer, List<RankDiff>> nextKNN = getFullKNN(nextFrame);

            // Calculate rank differences and weights for each entity in the next frame
            // based on its neighbors from the current frame
            for (int i = 0; i < currentFrame.length; i++) {
                List<RankDiff> neighbors = currentKNN.get(i);
                if (neighbors == null)
                    continue;

                for (RankDiff neighbor : neighbors) {

                    int currentRankneighbor = neighbor.getRank();

                    // Find the rank of the current neighbor in the next frame
                    int nextRankNeighbor = -1;
                    List<RankDiff> nextNeighbors = nextKNN.get(i);
                    for (RankDiff nextNeighbor : nextNeighbors) {
                        if (nextNeighbor.getId() == neighbor.getId()) {
                            nextRankNeighbor = nextNeighbor.getRank();
                            break;
                        }
                    }

                    double weight = 1.0 / currentRankneighbor;
                    double rij = Math.abs(nextRankNeighbor);

                    totalNumerator += weight * rij;
                    totalDenominator += weight;

                }

            }

            // Compute KS value for the current frame
            double ksValue = totalDenominator > 0 ? totalNumerator / totalDenominator : 0;
            stability[l] = ksValue;

        }

        return stability;
    }

    // Compute k-nearest neighbors for each entity
    private static Map<Integer, List<RankDiff>> getKNN(Integer[] frame, int k) {
        Map<Integer, List<Map.Entry<Integer, Integer>>> distances = new HashMap<>();
        for (int i = 0; i < frame.length; i++) {
            distances.put(i, new ArrayList<>());
            for (int j = 0; j < frame.length; j++) {
                if (i != j) {
                    distances.get(i).add(new AbstractMap.SimpleEntry<>(j, Math.abs(frame[i] - frame[j])));
                }
            }
            distances.get(i).sort(Map.Entry.comparingByValue());
        }

        Map<Integer, List<RankDiff>> knnMap = new HashMap<>();
        for (int i = 0; i < frame.length; i++) {
            // List<Integer> neighbors = new ArrayList<>();
            List<RankDiff> rankDiffs = new ArrayList<>();
            int count = 0;
            int rank = 0;
            int prev_dist = 0;
            for (Map.Entry<Integer, Integer> entry : distances.get(i)) {
                if (count >= k)
                    break;

                int current_dist = entry.getValue();

                if (prev_dist != current_dist) {
                    rank++;
                }

                rankDiffs.add(new RankDiff(entry.getKey(), Math.max(rank, 1)));

                prev_dist = current_dist;
                count++;
            }

            knnMap.put(i, rankDiffs);
        }
        return knnMap;
    }

    // Compute k-nearest neighbors for each entity
    private static Map<Integer, List<RankDiff>> getFullKNN(Integer[] frame) {
        Map<Integer, List<Map.Entry<Integer, Integer>>> distances = new HashMap<>();
        for (int i = 0; i < frame.length; i++) {
            distances.put(i, new ArrayList<>());
            for (int j = 0; j < frame.length; j++) {
                if (i != j) {
                    distances.get(i).add(new AbstractMap.SimpleEntry<>(j, Math.abs(frame[i] - frame[j])));
                }
            }
            distances.get(i).sort(Map.Entry.comparingByValue());
        }

        Map<Integer, List<RankDiff>> knnMap = new HashMap<>();
        for (int i = 0; i < frame.length; i++) {
            // List<Integer> neighbors = new ArrayList<>();
            List<RankDiff> rankDiffs = new ArrayList<>();
            int rank = 0;
            int prev_dist = 0;
            for (Map.Entry<Integer, Integer> entry : distances.get(i)) {

                int current_dist = entry.getValue();

                if (prev_dist != current_dist) {
                    rank++;
                }

                rankDiffs.add(new RankDiff(entry.getKey(), Math.max(rank, 1)));
                // neighbors.add(entry.getKey());

                prev_dist = current_dist;
            }

            knnMap.put(i, rankDiffs);
        }
        return knnMap;
    }

}