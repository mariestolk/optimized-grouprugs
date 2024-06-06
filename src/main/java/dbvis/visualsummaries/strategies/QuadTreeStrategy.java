package dbvis.visualsummaries.strategies;

import java.awt.Point;
import java.util.ArrayList;

import dbvis.visualsummaries.data.DataPoint;

/**
 * QuadTree parent strategy. Can be used for different implementation (e.g.
 * default QuadTRee, Point Quadtree)
 *
 * @author Eren Cakmak, University of Konstanz
 * <cakmak@dbvis.inf.uni-konstanz.de>
 * @author Juri Buchmüller, University of Konstanz
 * <buchmueller@dbvis.inf.uni-konstanz.de>
 */
public class QuadTreeStrategy implements Strategy {

    @Override
    public String getName() {
        return "Point QuadTree";
    }

    @Override
    public DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName) {
        DataPoint[][] result = new DataPoint[unsorted.length][unsorted[0].length];

        for (int x = 0; x < unsorted.length; x++) {
            // create the quadtree and insert the elements
            PointQuadTree quadTree = new PointQuadTree();

            for (int y = 0; y < unsorted[x].length; y++) {
                quadTree.insert(new Point((int) unsorted[x][y].getX(), (int) unsorted[x][y].getY()), unsorted[x][y]);
            }
            //return the inorder traversal
            ArrayList<DataPoint> list = quadTree.inorderTraversal();

            //System.out.println(list.size());
            for (int y = 0; y < list.size(); y++) {
                if (list.get(y) != null) {
                    result[x][y] = list.get(y);
                }
            }
        }
        return result;
    }

}
