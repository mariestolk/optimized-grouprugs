package dbvis.visualsummaries.strategies;

import java.util.ArrayList;
import java.util.Comparator;

import dbvis.visualsummaries.data.DataPoint;
import tagbio.umap.UnstableUmap;

/**
 *
 * @author buchmueller
 */
public class UnstableUMAPStrategy implements Strategy {

    @Override
    public String getName() {
        return "UMAPStrategy";
    }

    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsname) {

        DataPoint[][] ordered = new DataPoint[unsorted.length][unsorted[0].length];

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
            double[][] result = dUMAPmagic(framevalues);

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
        return ordered;
    }

    // does umap magic. parameters arbitrary so far.
    private double[][] dUMAPmagic(double[][] values) {
        UnstableUmap umap = new UnstableUmap();
        umap.setNumberComponents(1); // number of dimensions in result
        umap.setNumberNearestNeighbours(15);
        umap.setThreads(10); // use > 1 to enable parallelism
        return umap.fitTransform(values);
    }

}
