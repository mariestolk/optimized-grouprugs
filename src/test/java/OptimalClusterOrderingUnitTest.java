/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.strategies.Cluster;
import dbvis.visualsummaries.strategies.CompLinkCluster;
import dbvis.visualsummaries.strategies.CompLinkClusteringStrategy;

import static org.junit.Assert.*;

/**
 *
 * @author jwulms
 */
public class OptimalClusterOrderingUnitTest {

    public OptimalClusterOrderingUnitTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    CompLinkClusteringStrategy strategy = new CompLinkClusteringStrategy();
    
    DataPoint[] hardPoints = {  new DataPoint(0, 0, 0),
                                new DataPoint(2, 0, 1),
                                new DataPoint(1, 2, 2),
                                new DataPoint(0, 5, 3),
                                new DataPoint(7, 0, 4),
                                new DataPoint(8, 0, 5)  };
    
    DataPoint[] easyPoints = {  new DataPoint(0, 0, 0),
                                new DataPoint(2, 0, 1),
                                new DataPoint(1, 2, 2),
                                new DataPoint(0, 5, 3),
                                new DataPoint(20, 0, 4),
                                new DataPoint(30, 0, 5)  };

    //arraylist to save remaining clusters in
    ArrayList<Cluster> easyClusters = new ArrayList();
    ArrayList<Cluster> hardClusters = new ArrayList();
    //matrix containing hardDistances between clusters
    ArrayList<ArrayList<Double>> hardDistances = new ArrayList();
    ArrayList<ArrayList<Double>> easyDistances = new ArrayList();
    //save hardDistances between single elements to make ordering after clustering
    ArrayList<ArrayList<Double>> hardSimilarity;
    ArrayList<ArrayList<Double>> easySimilarity;

    @Before
    public void setUp() {

        //put points in separate clusters and find hardDistances between them
        for (int y = 0; y < hardPoints.length; y++) {
            CompLinkCluster singleElement = new CompLinkCluster(hardPoints[y], y);
            hardClusters.add(singleElement);
            hardDistances.add(new ArrayList());

            //store hardDistances in "bottom" triangle of matrix (index 0-y for y-th element of unsorted)
            for (int i = 0; i < y; i++) {
                hardDistances.get(y).add(hardClusters.get(i).getDistance(singleElement));
            }
            // add diagonal value in adjacency matrix
            hardDistances.get(y).add(0.0);
        }
        
        //save distances between single elements to make ordering later
        hardSimilarity = deepCopy(hardDistances);
        
        //put points in separate clusters and find hardDistances between them
        for (int y = 0; y < easyPoints.length; y++) {
            CompLinkCluster singleElement = new CompLinkCluster(easyPoints[y], y);
            easyClusters.add(singleElement);
            easyDistances.add(new ArrayList());

            //store hardDistances in "bottom" triangle of matrix (index 0-y for y-th element of unsorted)
            for (int i = 0; i < y; i++) {
                easyDistances.get(y).add(easyClusters.get(i).getDistance(singleElement));
            }
            // add diagonal value in adjacency matrix
            easyDistances.get(y).add(0.0);
        }

        //save distances between single elements to make ordering later
        easySimilarity = deepCopy(easyDistances);
    }
    
    private ArrayList<ArrayList<Double>> deepCopy(ArrayList<ArrayList<Double>> listOfLists) {
        ArrayList<ArrayList<Double>> result = new ArrayList();
        
        int i = 0;
        for (ArrayList<Double> list : listOfLists) {
            result.add(new ArrayList());
            for (double value : list) {
                result.get(i).add(value);
            }
            i++;
        }
        
        return result;
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test.
    @Test
    public void testCLClusteringHard() {

        //make clustering
        Cluster finalCluster = strategy.completeLinkageClustering(hardClusters, hardDistances);
        assertEquals("Not same amount of indices and elements in finalCluster", finalCluster.getIndices().size(), finalCluster.getElements().size());
        assertEquals("Final cluster does not have 6 points, but it has " + finalCluster.getIndices().size(), 6, finalCluster.getIndices().size());
        
        //test all non singleton clusters
        Cluster cluster1 = finalCluster.getChild1();
        Cluster cluster4 = finalCluster.getChild2();
        assertEquals("Not same amount of indices and elements in cluster1", cluster1.getIndices().size(), cluster1.getElements().size());
        assertEquals("cluster1 does not have 2 points, but it has " + cluster1.getIndices().size(), 2, cluster1.getIndices().size());
        
        assertEquals("Not same amount of indices and elements in cluster4", cluster4.getIndices().size(), cluster4.getElements().size());
        assertEquals("cluster4 does not have 4 points, but it has " + cluster4.getIndices().size(), 4, cluster4.getIndices().size());
        
        Cluster cluster3 = cluster4.getChild2();
        assertEquals("Not same amount of indices and elements in cluster3", cluster3.getIndices().size(), cluster3.getElements().size());
        assertEquals("cluster3 does not have 3 points, but it has " + cluster3.getIndices().size(), 3, cluster3.getIndices().size());
        
        Cluster cluster2 = cluster3.getChild2();
        assertEquals("Not same amount of indices and elements in cluster2", cluster2.getIndices().size(), cluster2.getElements().size());
        assertEquals("cluster2 does not have 2 points, but it has " + cluster2.getIndices().size(), 2, cluster2.getIndices().size());
        
        //test all singleton clusters
        Cluster element4 = cluster1.getChild1();
        Cluster element5 = cluster1.getChild2();
        assertEquals("Not same amount of indices and elements in element4", element4.getIndices().size(), element4.getElements().size());
        assertEquals("element4 is not singleton, but number of elements is " + element4.getIndices().size(), 1, element4.getIndices().size());
        assertEquals("element4 has incorrect index " + element4.getIndices().get(0), 4, (int) element4.getIndices().get(0));
        
        assertEquals("Not same amount of indices and elements in element5", element5.getIndices().size(), element5.getElements().size());
        assertEquals("element5 is not singleton, but number of elements is " + element5.getIndices().size(), 1, element5.getIndices().size());
        assertEquals("element5 has incorrect index " + element5.getIndices().get(0), 5, (int) element5.getIndices().get(0));
        
        Cluster element3 = cluster4.getChild1();
        assertEquals("Not same amount of indices and elements in element3", element3.getIndices().size(), element3.getElements().size());
        assertEquals("element3 is not singleton, but number of elements is " + element3.getIndices().size(), 1, element3.getIndices().size());
        assertEquals("element3 has incorrect index " + element3.getIndices().get(0), 3, (int) element3.getIndices().get(0));
        
        Cluster element2 = cluster3.getChild1();
        assertEquals("Not same amount of indices and elements in element2", element2.getIndices().size(), element2.getElements().size());
        assertEquals("element2 is not singleton, but number of elements is " + element2.getIndices().size(), 1, element2.getIndices().size());
        assertEquals("element2 has incorrect index " + element2.getIndices().get(0), 2, (int) element2.getIndices().get(0));
        
        Cluster element0 = cluster2.getChild1();
        Cluster element1 = cluster2.getChild2();
        assertEquals("Not same amount of indices and elements in element0", element0.getIndices().size(), element0.getElements().size());
        assertEquals("element0 is not singleton, but number of elements is " + element0.getIndices().size(), 1, element0.getIndices().size());
        assertEquals("element0 has incorrect index " + element0.getIndices().get(0), 0, (int) element0.getIndices().get(0));
        
        assertEquals("Not same amount of indices and elements in element1", element1.getIndices().size(), element1.getElements().size());
        assertEquals("element1 is not singleton, but number of elements is " + element1.getIndices().size(), 1, element1.getIndices().size());
        assertEquals("element1 has incorrect index " + element1.getIndices().get(0), 1, (int) element1.getIndices().get(0));
    }
    
    @Test
    public void testOptimalOrderingHard() {

        //make clustering
        Cluster finalCluster = strategy.completeLinkageClustering(hardClusters, hardDistances);
        strategy.findOrdering(finalCluster, hardSimilarity);
        
        //test trivial fields
        assertEquals("Not same amount of indices and elements in finalCluster", finalCluster.getIndices().size(), finalCluster.getElements().size());
        assertEquals("Final cluster does not have 6 points, but it has " + finalCluster.getIndices().size(), 6, finalCluster.getIndices().size());
        
        //test whether order is correct
        //top level children never swap
        Cluster cluster1 = finalCluster.getChild1();
        Cluster cluster4 = finalCluster.getChild2();
        assertEquals("Not same amount of indices and elements in cluster1", cluster1.getIndices().size(), cluster1.getElements().size());
        assertEquals("cluster1 does not have 2 points, but it has " + cluster1.getIndices().size(), 2, cluster1.getIndices().size());
        
        assertEquals("Not same amount of indices and elements in cluster4", cluster4.getIndices().size(), cluster4.getElements().size());
        assertEquals("cluster4 does not have 4 points, but it has " + cluster4.getIndices().size(), 4, cluster4.getIndices().size());
        
        //since point (7,0) is in cluster1 and it is closest to the point (2,0) in cluster4
        //we know that cluster3 should be the first child of cluster4 and element4 (7,0) should be the second child in cluster1
        Cluster cluster3 = cluster4.getChild1();
        Cluster element4 = cluster1.getChild2();
        assertEquals("Not same amount of indices and elements in cluster3", cluster3.getIndices().size(), cluster3.getElements().size());
        assertEquals("cluster3 does not have 3 points, but it has " + cluster3.getIndices().size(), 3, cluster3.getIndices().size());
        
        assertEquals("Not same amount of indices and elements in element4", element4.getIndices().size(), element4.getElements().size());
        assertEquals("element4 is not singleton, but number of elements is " + element4.getIndices().size(), 1, element4.getIndices().size());
        assertEquals("element4 has incorrect index " + element4.getIndices().get(0), 4, (int) element4.getIndices().get(0));
        
        //this also means that the first child of cluster 1 is element5 (8,0) and within cluster4 element3 should be the second child
        Cluster element5 = cluster1.getChild1();
        Cluster element3 = cluster4.getChild2();
        assertEquals("Not same amount of indices and elements in element5", element5.getIndices().size(), element5.getElements().size());
        assertEquals("element5 is not singleton, but number of elements is " + element5.getIndices().size(), 1, element5.getIndices().size());
        assertEquals("element5 has incorrect index " + element5.getIndices().get(0), 5, (int) element5.getIndices().get(0));
        
        assertEquals("Not same amount of indices and elements in element3", element3.getIndices().size(), element3.getElements().size());
        assertEquals("element3 is not singleton, but number of elements is " + element3.getIndices().size(), 1, element3.getIndices().size());
        assertEquals("element3 has incorrect index " + element3.getIndices().get(0), 3, (int) element3.getIndices().get(0));
        
        //as element2 in cluster3 is closest to element3, it should be the second child of cluster3, in order to be close to element3
        Cluster element2 = cluster3.getChild2();
        assertEquals("Not same amount of indices and elements in element2", element2.getIndices().size(), element2.getElements().size());
        assertEquals("element2 is not singleton, but number of elements is " + element2.getIndices().size(), 1, element2.getIndices().size());
        assertEquals("element2 has incorrect index " + element2.getIndices().get(0), 2, (int) element2.getIndices().get(0));
        
        //as a result of the above, cluster1 is the first child of cluster3
        Cluster cluster2 = cluster3.getChild1();
        assertEquals("Not same amount of indices and elements in cluster2", cluster2.getIndices().size(), cluster2.getElements().size());
        assertEquals("cluster2 does not have 2 points, but it has " + cluster2.getIndices().size(), 2, cluster2.getIndices().size());
        
        //within cluster1 the elements also swapped places because element1 (2,0) was the element of cluster4, closest to element4 (7,0) in cluster1
        Cluster element0 = cluster2.getChild2();
        Cluster element1 = cluster2.getChild1();
        assertEquals("Not same amount of indices and elements in element0", element0.getIndices().size(), element0.getElements().size());
        assertEquals("element0 is not singleton, but number of elements is " + element0.getIndices().size(), 1, element0.getIndices().size());
        assertEquals("element0 has incorrect index " + element0.getIndices().get(0), 0, (int) element0.getIndices().get(0));
        
        assertEquals("Not same amount of indices and elements in element1", element1.getIndices().size(), element1.getElements().size());
        assertEquals("element1 is not singleton, but number of elements is " + element1.getIndices().size(), 1, element1.getIndices().size());
        assertEquals("element1 has incorrect index " + element1.getIndices().get(0), 1, (int) element1.getIndices().get(0));
    }
    
    @Test
    public void testCLClusteringEasy() {

        //make clustering
        Cluster finalCluster = strategy.completeLinkageClustering(easyClusters, easyDistances);
        assertEquals("Not same amount of indices and elements in finalCluster", finalCluster.getIndices().size(), finalCluster.getElements().size());
        assertEquals("Final cluster does not have 6 points, but it has " + finalCluster.getIndices().size(), 6, finalCluster.getIndices().size());
        
        //test all non singleton clusters
        Cluster cluster1 = finalCluster.getChild2();
        Cluster cluster4 = finalCluster.getChild1();
        assertEquals("Not same amount of indices and elements in cluster1", cluster1.getIndices().size(), cluster1.getElements().size());
        assertEquals("cluster1 does not have 2 points, but it has " + cluster1.getIndices().size(), 2, cluster1.getIndices().size());
        
        assertEquals("Not same amount of indices and elements in cluster4", cluster4.getIndices().size(), cluster4.getElements().size());
        assertEquals("cluster4 does not have 4 points, but it has " + cluster4.getIndices().size(), 4, cluster4.getIndices().size());
        
        Cluster cluster3 = cluster4.getChild2();
        assertEquals("Not same amount of indices and elements in cluster3", cluster3.getIndices().size(), cluster3.getElements().size());
        assertEquals("cluster3 does not have 3 points, but it has " + cluster3.getIndices().size(), 3, cluster3.getIndices().size());
        
        Cluster cluster2 = cluster3.getChild2();
        assertEquals("Not same amount of indices and elements in cluster2", cluster2.getIndices().size(), cluster2.getElements().size());
        assertEquals("cluster2 does not have 2 points, but it has " + cluster2.getIndices().size(), 2, cluster2.getIndices().size());
        
        //test all singleton clusters
        Cluster element4 = cluster1.getChild1();
        Cluster element5 = cluster1.getChild2();
        assertEquals("Not same amount of indices and elements in element4", element4.getIndices().size(), element4.getElements().size());
        assertEquals("element4 is not singleton, but number of elements is " + element4.getIndices().size(), 1, element4.getIndices().size());
        assertEquals("element4 has incorrect index " + element4.getIndices().get(0), 4, (int) element4.getIndices().get(0));
        
        assertEquals("Not same amount of indices and elements in element5", element5.getIndices().size(), element5.getElements().size());
        assertEquals("element5 is not singleton, but number of elements is " + element5.getIndices().size(), 1, element5.getIndices().size());
        assertEquals("element5 has incorrect index " + element5.getIndices().get(0), 5, (int) element5.getIndices().get(0));
        
        Cluster element3 = cluster4.getChild1();
        assertEquals("Not same amount of indices and elements in element3", element3.getIndices().size(), element3.getElements().size());
        assertEquals("element3 is not singleton, but number of elements is " + element3.getIndices().size(), 1, element3.getIndices().size());
        assertEquals("element3 has incorrect index " + element3.getIndices().get(0), 3, (int) element3.getIndices().get(0));
        
        Cluster element2 = cluster3.getChild1();
        assertEquals("Not same amount of indices and elements in element2", element2.getIndices().size(), element2.getElements().size());
        assertEquals("element2 is not singleton, but number of elements is " + element2.getIndices().size(), 1, element2.getIndices().size());
        assertEquals("element2 has incorrect index " + element2.getIndices().get(0), 2, (int) element2.getIndices().get(0));
        
        Cluster element0 = cluster2.getChild1();
        Cluster element1 = cluster2.getChild2();
        assertEquals("Not same amount of indices and elements in element0", element0.getIndices().size(), element0.getElements().size());
        assertEquals("element0 is not singleton, but number of elements is " + element0.getIndices().size(), 1, element0.getIndices().size());
        assertEquals("element0 has incorrect index " + element0.getIndices().get(0), 0, (int) element0.getIndices().get(0));
        
        assertEquals("Not same amount of indices and elements in element1", element1.getIndices().size(), element1.getElements().size());
        assertEquals("element1 is not singleton, but number of elements is " + element1.getIndices().size(), 1, element1.getIndices().size());
        assertEquals("element1 has incorrect index " + element1.getIndices().get(0), 1, (int) element1.getIndices().get(0));
    }
    
    @Test
    public void testOptimalOrderingEasy() {

        //make clustering
        Cluster finalCluster = strategy.completeLinkageClustering(easyClusters, easyDistances);
        strategy.findOrdering(finalCluster, easySimilarity);
        
        assertEquals("Not same amount of indices and elements in finalCluster", finalCluster.getIndices().size(), finalCluster.getElements().size());
        assertEquals("Final cluster does not have 6 points, but it has " + finalCluster.getIndices().size(), 6, finalCluster.getIndices().size());
        
        //test all non singleton clusters
        Cluster cluster1 = finalCluster.getChild2();
        Cluster cluster4 = finalCluster.getChild1();
        assertEquals("Not same amount of indices and elements in cluster1", cluster1.getIndices().size(), cluster1.getElements().size());
        assertEquals("cluster1 does not have 2 points, but it has " + cluster1.getIndices().size(), 2, cluster1.getIndices().size());
        
        assertEquals("Not same amount of indices and elements in cluster4", cluster4.getIndices().size(), cluster4.getElements().size());
        assertEquals("cluster4 does not have 4 points, but it has " + cluster4.getIndices().size(), 4, cluster4.getIndices().size());
        
        Cluster cluster3 = cluster4.getChild2();
        assertEquals("Not same amount of indices and elements in cluster3", cluster3.getIndices().size(), cluster3.getElements().size());
        assertEquals("cluster3 does not have 3 points, but it has " + cluster3.getIndices().size(), 3, cluster3.getIndices().size());
        
        Cluster cluster2 = cluster3.getChild2();
        assertEquals("Not same amount of indices and elements in cluster2", cluster2.getIndices().size(), cluster2.getElements().size());
        assertEquals("cluster2 does not have 2 points, but it has " + cluster2.getIndices().size(), 2, cluster2.getIndices().size());
        
        //test all singleton clusters
        Cluster element4 = cluster1.getChild1();
        Cluster element5 = cluster1.getChild2();
        assertEquals("Not same amount of indices and elements in element4", element4.getIndices().size(), element4.getElements().size());
        assertEquals("element4 is not singleton, but number of elements is " + element4.getIndices().size(), 1, element4.getIndices().size());
        assertEquals("element4 has incorrect index " + element4.getIndices().get(0), 4, (int) element4.getIndices().get(0));
        
        assertEquals("Not same amount of indices and elements in element5", element5.getIndices().size(), element5.getElements().size());
        assertEquals("element5 is not singleton, but number of elements is " + element5.getIndices().size(), 1, element5.getIndices().size());
        assertEquals("element5 has incorrect index " + element5.getIndices().get(0), 5, (int) element5.getIndices().get(0));
        
        Cluster element3 = cluster4.getChild1();
        assertEquals("Not same amount of indices and elements in element3", element3.getIndices().size(), element3.getElements().size());
        assertEquals("element3 is not singleton, but number of elements is " + element3.getIndices().size(), 1, element3.getIndices().size());
        assertEquals("element3 has incorrect index " + element3.getIndices().get(0), 3, (int) element3.getIndices().get(0));
        
        Cluster element2 = cluster3.getChild1();
        assertEquals("Not same amount of indices and elements in element2", element2.getIndices().size(), element2.getElements().size());
        assertEquals("element2 is not singleton, but number of elements is " + element2.getIndices().size(), 1, element2.getIndices().size());
        assertEquals("element2 has incorrect index " + element2.getIndices().get(0), 2, (int) element2.getIndices().get(0));
        
        Cluster element0 = cluster2.getChild1();
        Cluster element1 = cluster2.getChild2();
        assertEquals("Not same amount of indices and elements in element0", element0.getIndices().size(), element0.getElements().size());
        assertEquals("element0 is not singleton, but number of elements is " + element0.getIndices().size(), 1, element0.getIndices().size());
        assertEquals("element0 has incorrect index " + element0.getIndices().get(0), 0, (int) element0.getIndices().get(0));
        
        assertEquals("Not same amount of indices and elements in element1", element1.getIndices().size(), element1.getElements().size());
        assertEquals("element1 is not singleton, but number of elements is " + element1.getIndices().size(), 1, element1.getIndices().size());
        assertEquals("element1 has incorrect index " + element1.getIndices().get(0), 1, (int) element1.getIndices().get(0));
    }
}
