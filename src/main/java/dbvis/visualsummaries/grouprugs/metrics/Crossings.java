package dbvis.visualsummaries.grouprugs.metrics;

public class Crossings {

    public static double[] count(Integer[][] frames) {
        double[] crossings = new double[frames.length - 1];
        for (int i = 0; i < frames.length - 1; i++) {
            crossings[i] = countCrossings(frames[i], frames[i + 1]);
        }
        return crossings;
    }

    private static double countCrossings(Integer[] frame1, Integer[] frame2) {
        double crossings = 0;
        for (int i = 0; i < frame1.length; i++) {
            for (int j = i + 1; j < frame1.length; j++) {
                if (frame1[i] < frame1[j] && frame2[i] > frame2[j]) {
                    crossings++;
                } else if (frame1[i] > frame1[j] && frame2[i] < frame2[j]) {
                    crossings++;
                }
            }

        }

        return crossings;

    }

}
