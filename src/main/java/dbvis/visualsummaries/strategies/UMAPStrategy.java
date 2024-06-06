package dbvis.visualsummaries.strategies;

import javafx.scene.chart.PieChart.Data;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import dbvis.visualsummaries.data.DataPoint;
import tagbio.umap.Umap;

/**
 *
 * @author buchmueller
 */
public class UMAPStrategy implements Strategy {

    private float[][] lastEmbedding = null;

    @Override
    public String getName() {
        return "UMAPStrategy";
    }

    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {

        // float[][] lastEmbedding = null;
        DataPoint[][] ordered = new DataPoint[unsorted.length][unsorted[0].length];

        // code to extract 1D values
        String userdir = System.getProperty("user.home");
        File orderingsfolder = new File(userdir + "/motionrugs/projections");
        if (!orderingsfolder.exists()) {
            orderingsfolder.mkdir();
        }

        File orderingfile = new File(orderingsfolder + "/" + dsName + "_UMAP_1D.csv");

        try {

            double[][] result = null;

            // writer for 1D values
            FileWriter writer = new FileWriter(orderingfile);

            writer.write("projections sorted on id, one frame per line\n");

            // for each frame, we extract the positions of the movers
            for (int i = 0; i < unsorted.length; i++) {
                System.out.println("FRAME " + i + "/" + unsorted.length);
                // holds the positions of each mover in the frame [moverid][x/y]
                double[][] framevalues = new double[unsorted[i].length][2];
                // fill in the position per id
                for (int j = 0; j < unsorted[i].length; j++) {
                    framevalues[j][0] = unsorted[i][j].getX();
                    framevalues[j][1] = unsorted[i][j].getY();
                }
                // do Umap magic on the data
                result = dUMAPmagic(framevalues);

                StringBuilder sb = new StringBuilder();
                // double[] values = new double[result.length];

                sb.append(result[0][0]);
                for (int j = 1; j < result.length; j++) {
                    sb.append(",");
                    sb.append(result[j][0]);
                }

                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n");
                writer.write(sb.toString());

                // Create and fill helper list for sorting the UMAP results, leveraging that the
                // umap results are in the (id) order of the input
                ArrayList<UMAPHelperPoint> resultlist = new ArrayList<>();
                for (int x = 0; x < result.length; x++) {
                    resultlist.add(new UMAPHelperPoint(x, result[x][0]));
                }

                // Sorting the UMAP projection values in ascending order
                resultlist.sort(Comparator.comparingDouble(UMAPHelperPoint::getUmapvalue));

                // Puts together the ordered results by looking up the appropriate data points
                // from the input
                for (int x = 0; x < resultlist.size(); x++) {
                    ordered[i][x] = unsorted[i][resultlist.get(x).getId()];
                }

            }

            // set lastembedding to null to ensure that the next dataset is not influenced
            // by the previous one
            lastEmbedding = null;

            writer.flush();
            writer.close();

        } catch (Exception ex) {
            Logger.getLogger(UMAPStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Make stable
        ordered = ensureStability(ordered);
        writeOrderedToFile(ordered, dsName);

        return ordered;

    }

    // does umap magic. parameters arbitrary so far.
    private double[][] dUMAPmagic(double[][] values) {
        Umap umap = new Umap();
        umap.setNumberComponents(1); // number of dimensions in result
        // Set nearest neighbors to half the number of entities
        // umap.setNumberNearestNeighbours(values.length);
        umap.setNumberNearestNeighbours(4); // Low number of nearest neighbors to capture local structure
        umap.setThreads(10); // use > 1 to enable parallelism

        // Adjusting parameters for better embedding
        umap.setSpread(1.5F); // Controls how clumped the embedded points are
        umap.setMinDist(0.2F); // Controls minimum distance between points in final embedding
        umap.setLearningRate(0.01F); // Smaller learning rate for smooth convergence
        umap.setNumberEpochs(150); // Avoid overfitting.

        double[][] currentEmbedding = umap.fitTransform(values, lastEmbedding);

        // double to float conversion
        float[][] currEmbeddingFloat = new float[currentEmbedding.length][currentEmbedding[0].length];
        for (int i = 0; i < currentEmbedding.length; i++) {
            for (int j = 0; j < currentEmbedding[0].length; j++) {
                currEmbeddingFloat[i][j] = (float) currentEmbedding[i][j];
            }
        }

        lastEmbedding = currEmbeddingFloat;

        return currentEmbedding;
    }

    /**
     * Ensures that the order of the entities in the frames is stable, to prevent
     * the dataset from flipping from frame to frame.
     * 
     * @param ordered The ordered entities in the frames.
     * @return The ordered entities in the frames with stable order.
     */
    private static DataPoint[][] ensureStability(DataPoint[][] ordered) {

        Integer[][] countCrossingsOrdered = new Integer[ordered.length][ordered[0].length];
        for (int frame = 0; frame < ordered.length; frame++) {
            for (int entityorder = 0; entityorder < ordered[frame].length; entityorder++) {

                // get entity id
                int entityid = ordered[frame][entityorder].getId();
                countCrossingsOrdered[frame][entityid] = entityorder;
            }
        }

        for (int frame = 0; frame < countCrossingsOrdered.length - 1; frame++) {

            int crossings = 0;
            int reverseCrossings = 0;

            for (int entity = 0; entity < countCrossingsOrdered[frame].length; entity++) {
                for (int entity2 = entity + 1; entity2 < countCrossingsOrdered[frame].length; entity2++) {

                    // If entity < entity2 in frame 1 and entity > entity2 in frame 2
                    if (countCrossingsOrdered[frame][entity] < countCrossingsOrdered[frame][entity2]
                            && countCrossingsOrdered[frame + 1][entity] > countCrossingsOrdered[frame + 1][entity2]) {
                        crossings++;

                    } // If entity > entity2 in frame 1 and entity < entity2 in frame 2

                    else if (countCrossingsOrdered[frame][entity] > countCrossingsOrdered[frame][entity2]
                            && countCrossingsOrdered[frame + 1][entity] < countCrossingsOrdered[frame + 1][entity2]) {
                        crossings++;
                    }

                    if (countCrossingsOrdered[frame][entity] < countCrossingsOrdered[frame][entity2]
                            && countCrossingsOrdered[frame + 1][entity] < countCrossingsOrdered[frame].length - 1
                                    - countCrossingsOrdered[frame + 1][entity2]) {
                        reverseCrossings++;
                    } else if (countCrossingsOrdered[frame][entity] > countCrossingsOrdered[frame][entity2]
                            && countCrossingsOrdered[frame + 1][entity] > countCrossingsOrdered[frame].length - 1
                                    - countCrossingsOrdered[frame + 1][entity2]) {
                        reverseCrossings++;
                    }

                }
            }

            if (crossings > reverseCrossings) {

                boolean[] swapped = new boolean[ordered[frame].length];
                // fill swapped with false
                for (int i = 0; i < swapped.length; i++) {
                    swapped[i] = false;
                }

                // Reverse the order of the entities in the frame+1
                for (int entity = 0; entity < countCrossingsOrdered[frame].length; entity++) {

                    // Get order before
                    int orderBefore = countCrossingsOrdered[frame + 1][entity];

                    // Set new order
                    int orderafter = countCrossingsOrdered[frame].length - 1 - countCrossingsOrdered[frame + 1][entity];

                    if (swapped[orderBefore] || swapped[orderafter]) {
                        continue;
                    }

                    // Get corresponding entities
                    DataPoint entity1 = ordered[frame + 1][orderBefore];
                    DataPoint entity2 = ordered[frame + 1][orderafter];

                    // Swap entities
                    ordered[frame + 1][orderBefore] = entity2;
                    ordered[frame + 1][orderafter] = entity1;

                    // Update the countCrossingsOrdered
                    countCrossingsOrdered[frame + 1][entity] = countCrossingsOrdered[frame].length - 1
                            - countCrossingsOrdered[frame + 1][entity];

                    // Mark as swapped
                    swapped[orderBefore] = true;
                    swapped[orderafter] = true;

                }
            }

        }

        return ordered;
    }

    private static void writeOrderedToFile(DataPoint[][] ordered, String dsName) {

        // code to extract 1D values
        String userdir = System.getProperty("user.home");
        File orderingsfolder = new File(userdir + "/motionrugs/ordering");
        if (!orderingsfolder.exists()) {
            orderingsfolder.mkdir();

        }

        File orderingfile = new File(orderingsfolder + "/" + dsName + "_UMAP_ordering.csv");

        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(orderingfile));
            writer.write("frame,id\n");
            for (int i = 0; i < ordered.length; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(ordered[i][0].getId());
                for (int j = 1; j < ordered[i].length; j++) {
                    sb.append(",");
                    sb.append(ordered[i][j].getId());
                }
                sb.append("\n");
                writer.write(sb.toString());
            }

            writer.flush();
            writer.close();

        } catch (Exception ex) {
            Logger.getLogger(UMAPStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // public static void main(String[] args) {
    // UMAPStrategy umap = new UMAPStrategy();

    // Create 3 datapoints
    // DataPoint dp1 = new DataPoint(0, 0, 0);
    // DataPoint dp2 = new DataPoint(1, 1, 1);
    // DataPoint dp3 = new DataPoint(2, 2, 2);

    // Create 3 frames
    // DataPoint[][] frames = new DataPoint[3][3];
    // frames[0][0] = dp1;
    // frames[0][1] = dp2;
    // frames[0][2] = dp3;

    // frames[1][0] = dp3;
    // frames[1][1] = dp2;
    // frames[1][2] = dp1;

    // frames[2][0] = dp1;
    // frames[2][1] = dp2;
    // frames[2][2] = dp3;

    // DataPoint[][] ordered = umap.getOrderedValues(frames);

    // for (int i = 0; i < ordered.length; i++) {
    // for (int j = 0; j < ordered[i].length; j++) {
    // System.out.print(ordered[i][j].getId() + " ");
    // }
    // System.out.println();
    // }

    // }

}
