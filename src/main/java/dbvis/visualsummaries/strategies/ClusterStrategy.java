/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.strategies;

import java.util.ArrayList;

import dbvis.visualsummaries.data.DataPoint;

/**
 *
 * @author jwulms
 */
abstract class ClusterStrategy implements Strategy {

    @Override
    public abstract String getName();

    @Override
    public abstract DataPoint[][] getOrderedValues(DataPoint[][] unsorted, String dsName);

    public int[] findOrdering(Cluster topLevelCluster, ArrayList<ArrayList<Double>> similarity) {
        int[] ordering = new int[topLevelCluster.getIndices().size()];

        if (topLevelCluster.getIndices().size() > 1) { // both children are not null
            BestClusterSolution[][] dynProgTable = new BestClusterSolution[topLevelCluster.getIndices().size()][topLevelCluster.getIndices().size()];

            //find optimal ordering for both children
            findOrdering(topLevelCluster.getChild1(), similarity, dynProgTable);
            findOrdering(topLevelCluster.getChild2(), similarity, dynProgTable);

            //find the best order, given the orderings of the children
            Cluster leftChild = topLevelCluster.getChild1();
            Cluster rightChild = topLevelCluster.getChild2();

            //variables to store best solution at top level
            int bestLeft = leftChild.getIndices().get(0);
            int bestRight = rightChild.getIndices().get(rightChild.getIndices().size() - 1);
            double overallBestCost = Double.MAX_VALUE;

            //dynamic programming on the hierarchical clustering structure
            if (leftChild.getIndices().size() > 1 && rightChild.getIndices().size() > 1) {
                // both subtrees are not a single leaf
                for (int leftIndex : leftChild.getIndices()) {
                    for (int rightIndex : rightChild.getIndices()) {
                        double lowestCost = Double.MAX_VALUE;

                        for (int leftMiddle : leftChild.getOtherChild(leftIndex).getIndices()) {
                            for (int rightMiddle : rightChild.getOtherChild(rightIndex).getIndices()) {
                                double cost = findCost(dynProgTable, leftIndex, rightIndex, leftMiddle, rightMiddle, similarity);

                                if (cost < lowestCost) {
                                    dynProgTable[leftIndex][rightIndex] = new BestClusterSolution(cost, leftMiddle, rightMiddle);
                                    dynProgTable[rightIndex][leftIndex] = new BestClusterSolution(cost, rightMiddle, leftMiddle);
                                    lowestCost = cost;
                                }

                                //also memorize best solution at top level
                                if (cost < overallBestCost) {
                                    overallBestCost = cost;
                                    bestLeft = leftIndex;
                                    bestRight = rightIndex;
                                }

                            }
                        }
                        
                    }
                }
            } else { // a subtree consists of a single leaf
                handleLeaves(topLevelCluster, similarity, dynProgTable);
            }

            //backtrack to find the right solution and set it in field of topLevelCluster
//            System.out.print("[ ");
            backtrackOrdering(dynProgTable, topLevelCluster, bestLeft, bestRight);
//            System.out.println("]");
        }

        //retrieve stored ordering
        ArrayList<Integer> orderedIndices = topLevelCluster.getIndices();
        for (int i = 0; i < orderedIndices.size(); i++) {
            ordering[i] = orderedIndices.get(i);
        }

        return ordering;
    }

    private void findOrdering(Cluster cluster, ArrayList<ArrayList<Double>> similarity, BestClusterSolution[][] dynProgTable) {

        if (cluster.getIndices().size() > 1) { // both children are not null
            //find optimal ordering for both children
            findOrdering(cluster.getChild1(), similarity, dynProgTable);
            findOrdering(cluster.getChild2(), similarity, dynProgTable);

            //find the best order, given the orderings of the children
            Cluster leftChild = cluster.getChild1();
            Cluster rightChild = cluster.getChild2();

            if (leftChild.getIndices().size() > 1 && rightChild.getIndices().size() > 1) { // both subtrees are not a single leaf
                for (int leftIndex : leftChild.getIndices()) {
                    for (int rightIndex : rightChild.getIndices()) {
                        double lowestCost = Double.MAX_VALUE;

                        for (int leftMiddle : leftChild.getOtherChild(leftIndex).getIndices()) {
                            for (int rightMiddle : rightChild.getOtherChild(rightIndex).getIndices()) {
                                double cost = findCost(dynProgTable, leftIndex, rightIndex, leftMiddle, rightMiddle, similarity);

                                if (cost < lowestCost) {
                                    dynProgTable[leftIndex][rightIndex] = new BestClusterSolution(cost, leftMiddle, rightMiddle);
                                    dynProgTable[rightIndex][leftIndex] = new BestClusterSolution(cost, rightMiddle, leftMiddle);
                                    lowestCost = cost;
                                }

                            }
                        }
                        
                    }
                }
            } else { // a subtree consists of a single leaf
                handleLeaves(cluster, similarity, dynProgTable);
            }

        }
    }

    private void handleLeaves(Cluster cluster, ArrayList<ArrayList<Double>> similarity, BestClusterSolution[][] dynProgTable) {
        Cluster leftSubtree = cluster.getChild1();
        Cluster rightSubtree = cluster.getChild2();
        //first check if both subtree have a single leaf
        if (leftSubtree.getIndices().size() == 1 && rightSubtree.getIndices().size() == 1) { // both subtrees are a leaf
            int leftIndex = leftSubtree.getIndices().get(0);
            int rightIndex = rightSubtree.getIndices().get(0);

            //first fill in base case of recursion
            dynProgTable[leftIndex][leftIndex] = new BestClusterSolution(0.0, leftIndex, leftIndex);
            dynProgTable[rightIndex][rightIndex] = new BestClusterSolution(0.0, rightIndex, rightIndex);

            //calculate the cost between the pair of vertices and fill in table
            double cost = findCost(dynProgTable, leftIndex, rightIndex, leftIndex, rightIndex, similarity);

            dynProgTable[leftIndex][rightIndex] = new BestClusterSolution(cost, leftIndex, rightIndex);
            dynProgTable[rightIndex][leftIndex] = new BestClusterSolution(cost, rightIndex, leftIndex);

        } else if (leftSubtree.getIndices().size() == 1) { // only right subtree has two children not null
            int leftIndex = leftSubtree.getIndices().get(0);

            //first fill in base case of recursion
            dynProgTable[leftIndex][leftIndex] = new BestClusterSolution(0.0, leftIndex, leftIndex);
            //find optimal order for right child
            findOrdering(rightSubtree, similarity, dynProgTable);

            for (int rightIndex : rightSubtree.getIndices()) {
                double lowestCost = Double.MAX_VALUE;
                for (int rightMiddle : rightSubtree.getOtherChild(rightIndex).getIndices()) {
                    double cost = findCost(dynProgTable, leftIndex, rightIndex, leftIndex, rightMiddle, similarity);

                    if (cost < lowestCost) {
                        dynProgTable[leftIndex][rightIndex] = new BestClusterSolution(cost, leftIndex, rightMiddle);
                        dynProgTable[rightIndex][leftIndex] = new BestClusterSolution(cost, rightMiddle, leftIndex);
                        lowestCost = cost;
                    }
                }
                
            }

        } else if (rightSubtree.getIndices().size() == 1) { // only left subtree has two children not null
            int rightIndex = rightSubtree.getIndices().get(0);

            //first fill in base case of recursion
            dynProgTable[rightIndex][rightIndex] = new BestClusterSolution(0.0, rightIndex, rightIndex);
            //find optimal order for right child
            findOrdering(leftSubtree, similarity, dynProgTable);

            for (int leftIndex : leftSubtree.getIndices()) {
                double lowestCost = Double.MAX_VALUE;
                for (int leftMiddle : leftSubtree.getOtherChild(leftIndex).getIndices()) {
                    double cost = findCost(dynProgTable, leftIndex, rightIndex, leftMiddle, rightIndex, similarity);

                    if (cost < lowestCost) {
                        dynProgTable[leftIndex][rightIndex] = new BestClusterSolution(cost, leftMiddle, rightIndex);
                        dynProgTable[rightIndex][leftIndex] = new BestClusterSolution(cost, rightIndex, leftMiddle);
                        lowestCost = cost;
                    }
                }
                
            }

        }
    }

    private double findCost(BestClusterSolution[][] dynProgTable, int leftIndex, int rightIndex, int leftMiddle, int rightMiddle, ArrayList<ArrayList<Double>> similarity) {
        double cost = dynProgTable[leftIndex][leftMiddle].getCost() + dynProgTable[rightMiddle][rightIndex].getCost();

        if (leftMiddle < rightMiddle) {
            cost += similarity.get(rightMiddle).get(leftMiddle);
        } else {
            cost += similarity.get(leftMiddle).get(rightMiddle);
        }

        return cost;
    }

    private void backtrackOrdering(BestClusterSolution[][] dynProgTable, Cluster cluster, int leftIndex, int rightIndex) {

        if (cluster.getIndices().size() == 1) {
//            System.out.print("element(" + cluster.getIndices().get(0) + ") ");
            return;
        } else {
            Cluster child1 = cluster.getChild1();
            Cluster child2 = cluster.getChild2();

            BestClusterSolution bestIntermediates = dynProgTable[leftIndex][rightIndex];

            //check if ordering should change
            if (child2.getIndices().contains(leftIndex) && child1.getIndices().contains(rightIndex)) {
                cluster.swapChildren();
                child1 = cluster.getChild1();
                child2 = cluster.getChild2();
            }

            //now child1 contains leftIndex and child2 contains rightIndex
            //we recursively do the same for the children
            backtrackOrdering(dynProgTable, child1, leftIndex, bestIntermediates.getLeftMiddle());
            double cost = (bestIntermediates.getCost()-dynProgTable[leftIndex][bestIntermediates.getLeftMiddle()].getCost()-dynProgTable[bestIntermediates.getRightMiddle()][rightIndex].getCost());
//            System.out.print("cost(" + cost + ") ");
            backtrackOrdering(dynProgTable, child2, bestIntermediates.getRightMiddle(), rightIndex);

            //the children have the correct ordering now
            //we update the orders at this level before going up a level in the recursion
            cluster.updateOrders();
        }

    }

}
