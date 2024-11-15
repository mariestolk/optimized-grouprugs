package dbvis.visualsummaries.grouprugs.linearprogram;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gurobi.gurobi.*;
import javafx.util.Pair;

/**
 * This class implements the following QP model with linear constraints:
 * 
 * minimize: sum_{i,j} sum_{r} (x_{ij}^r + x_{ij}^{r+1} - 2 * x_{ij}^r *
 * x_{ij}^{r+1})
 * 
 * subject to:
 * 0 <= x_{hi}^r + x_{ij}^r - x_{hj}^r <= 1
 * x_{ij} != x_{ji}
 * x_{ij}^r is binary
 * x_{hj}^r = x_{ij}^r if p_{hi} and not p_{ij}
 * x_{hi}^r = x_{hj}^r if p_{ij} and not p_{hj}
 * 
 * The decision variables are stored in a map, where the key is the frame and
 * the value is a map of pairs of groupIDs and the decision variable. Note that
 * only one of x_i_j_r and x_j_i_r is stored in the map.
 */

public class QP {

  public static Map<Integer, Map<Pair<Integer, Integer>, Boolean>> compute(MLCMGraph mlcmtc) throws GRBException {

    try {

      // =========================================================================
      // Set-up the Environment and Model
      // =========================================================================
      Map<Integer, Map<Pair<Integer, Integer>, Boolean>> decisionVariables = new HashMap<>();

      // Create an environment
      GRBEnv env = new GRBEnv("ecm.log");
      GRBModel model = new GRBModel(env);

      Map<String, Double> weightMap = createWeightMap(mlcmtc);
      System.out.println("Weight map created.");

      // =========================================================================
      // Add Variables
      // =========================================================================
      createDecisionVariables(mlcmtc, model);
      System.out.println("Variables added.");

      // =========================================================================
      // Set Objective
      // =========================================================================
      GRBQuadExpr objective = new GRBQuadExpr();
      countEdgeCrossings(mlcmtc, model, objective, weightMap);
      model.setObjective(objective, GRB.MINIMIZE);

      // =========================================================================
      // Add Constraints
      // =========================================================================
      addCompleteOrderingConstraints(mlcmtc, model);
      addTransitivityConstraints(mlcmtc, model);
      addTreeConstraints(mlcmtc, model);

      // =========================================================================
      // Optimize
      // =========================================================================
      model.optimize();

      // =========================================================================
      // Print and store the solution in a map
      // =========================================================================
      for (GRBVar var : model.getVars()) {
        // System.out.println(var.get(GRB.StringAttr.VarName) + " " +
        // var.get(GRB.DoubleAttr.X));

        int groupID1 = Integer.parseInt(var.get(GRB.StringAttr.VarName).split("_")[1]);
        int groupID2 = Integer.parseInt(var.get(GRB.StringAttr.VarName).split("_")[2]);
        int frame = Integer.parseInt(var.get(GRB.StringAttr.VarName).split("_")[3]);
        double value = var.get(GRB.DoubleAttr.X);
        boolean above = value == 1.0;

        Pair<Integer, Integer> pair = new Pair<>(groupID1, groupID2);

        // Check if there's already a map for this frame
        Map<Pair<Integer, Integer>, Boolean> frameMap = decisionVariables.get(frame);
        if (frameMap == null) {
          // If not, create a new map and put it in decisionVariables
          frameMap = new HashMap<>();
          decisionVariables.put(frame, frameMap);
        }

        frameMap.put(pair, above);
        decisionVariables.put(frame, frameMap);

      }

      model.dispose();
      env.dispose();

      return decisionVariables;

    } catch (GRBException e) {
      // Catch the exception
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
      e.printStackTrace();

      return null;

    }

  }

  /**
   * Create the decision variables for the MLCM-TC instance. For each pair of
   * vertices in the same frame, create a decision variable.
   * 
   * @param mlcmtc The MLCM-TC instance
   * @param model  The Gurobi model
   * @throws GRBException If there is an error creating the decision variables
   */
  private static void createDecisionVariables(MLCMGraph mlcmtc, GRBModel model) throws GRBException {

    // get layers
    List<Integer> layers = mlcmtc.getLayers();

    for (int layer : layers) {
      List<MLCMVertex> verticesInLayer = mlcmtc.getVerticesForLayer(layer);

      for (int i = 0; i < verticesInLayer.size(); i++) {
        MLCMVertex v = verticesInLayer.get(i);
        for (int j = 0; j < verticesInLayer.size(); j++) {
          MLCMVertex u = verticesInLayer.get(j);

          if (v == u) {
            continue;
          }

          // If the vertices are in the same frame and are not the same vertex, then add a
          // decision variable
          if (v.getFrame() == u.getFrame()
              && v != u) {

            // Create the decision variable
            String varName = String.format("x_%d_%d_%d", v.getGroupId(), u.getGroupId(), v.getFrame());
            model.addVar(0.0, 1.0, 0.0, GRB.BINARY, varName);

          }
        }
      }
    }

    // Update
    model.update();

  }

  /**
   * Creates a weight map for the MLCM-TC instance. The weight map is used to
   * determine the cost of edge crossings in the model. The weight is lower if the
   * edge is within the same component and higher if the edge is between different
   * components.
   * 
   * @param mlcmtc The MLCM-TC instance
   * @param model  The Gurobi model
   * @return The weight map
   * @throws GRBException
   */
  private static Map<String, Double> createWeightMap(MLCMGraph mlcmtc) {

    // Initialize weights
    double withinCompWeight = 0.001; // Lower cost for crossings within the same component
    double outsideCompWeight = 1; // Higher cost for crossings outside components or between different
                                  // components
    double transitionInitiatedWeight = 0.05; // Lower cost if one of the grouplines is in transition

    Map<String, Double> weightMap = new HashMap<>();

    List<Integer> layers = mlcmtc.getLayers();

    // For each layer except the last layer
    for (int layer : layers.subList(0, layers.size() - 1)) {

      List<MLCMVertex> verticesInLayer = mlcmtc.getVerticesForLayer(layer);

      // For each pair of vertices in the same layer
      for (int i = 0; i < verticesInLayer.size(); i++) {
        MLCMVertex u = verticesInLayer.get(i);
        for (int j = 0; j < verticesInLayer.size(); j++) {
          MLCMVertex v = verticesInLayer.get(j);

          if (u == v) {
            continue;
          }

          // Only consider vertices in layer
          if (u.getFrame() == layer && v.getFrame() == layer) {

            // Find groupIDs for the vertices in current layer
            int groupID1_1 = u.getGroupId();
            int groupID2_1 = v.getGroupId();

            // Find the next layer
            int nextLayer = layers.get(layers.indexOf(layer) + 1);

            String groupID1_2Str = Integer.toString(groupID1_1) + "_" + Integer.toString(nextLayer);
            String groupID2_2Str = Integer.toString(groupID2_1) + "_" + Integer.toString(nextLayer);

            // Find the corresponding vertices in the next layer
            MLCMVertex uNext = mlcmtc.getVertex(groupID1_2Str);
            MLCMVertex vNext = mlcmtc.getVertex(groupID2_2Str);

            // Condition: if u and v share a parent in layer r and in layer r+1, assign low
            // weight
            boolean p_uv = u.getParent() == v.getParent();
            boolean p_uNextvNext = uNext.getParent() == vNext.getParent();

            // Condition: if either groupline u or v (or both) are in transition, assign
            // lower value
            boolean sameChildrenU = u.getParent().getChildGroupIds().equals(uNext.getParent().getChildGroupIds());
            boolean sameChildrenV = v.getParent().getChildGroupIds().equals(vNext.getParent().getChildGroupIds());

            double weight;

            // Check which weight is appropriate
            if (p_uv && p_uNextvNext) { // same component in current and next layer?
              weight = withinCompWeight;
            } else if (!sameChildrenU || !sameChildrenV) { // both remain in MotionRugs in current and next layer?
              weight = transitionInitiatedWeight;
            } else {
              weight = outsideCompWeight;
            }

            // Create weight map
            weightMap.put("w_" + groupID1_1 + "_" + groupID2_1 + "_" + layer, weight);

          }

        }

      }

    }

    return weightMap;

  }

  /**
   * Adds the complete ordering constraints to the MLCM-TC instance. Ensure that
   * for all i, j in V, x_{ij}^r + x_{ji}^r = 1. (Either x_{ij}^r = 1 or x_{ji}^r,
   * but not both.)
   * 
   * @param mlcmtc
   * @param model
   * @throws GRBException
   */
  private static void addCompleteOrderingConstraints(MLCMGraph mlcmtc, GRBModel model) throws GRBException {

    // get layers
    List<Integer> layers = mlcmtc.getLayers();

    for (int layer : layers) {

      List<MLCMVertex> verticesInLayer = mlcmtc.getVerticesForLayer(layer);

      for (int i = 0; i < verticesInLayer.size(); i++) {
        MLCMVertex u = verticesInLayer.get(i);
        for (int j = 0; j < verticesInLayer.size(); j++) {
          MLCMVertex v = verticesInLayer.get(j);

          if (u == v) {
            continue;
          }

          if (u.getFrame() == v.getFrame()) {
            int groupID1 = u.getGroupId();
            int groupID2 = v.getGroupId();
            int frame = u.getFrame();

            GRBVar x_ij = model.getVarByName("x_" + groupID1 + "_" + groupID2 + "_" + frame);
            GRBVar x_ji = model.getVarByName("x_" + groupID2 + "_" + groupID1 + "_" + frame);

            GRBLinExpr expr = new GRBLinExpr();
            expr.addTerm(1.0, x_ij);
            expr.addTerm(1.0, x_ji);

            model.addConstr(expr, GRB.EQUAL, 1.0, "complete_ordering_" + groupID1 + "_" + groupID2 + "_" + frame);
          }

        }

      }
    }

    model.update();
    System.out.println("Complete ordering constraints added.");

  }

  /**
   * Adds the transitivity constraints to the MLCM-TC instance.
   * 
   * @param mlcmtc The MLCM-TC instance
   * @param model  The Gurobi model
   */
  private static void addTransitivityConstraints(MLCMGraph mlcmtc, GRBModel model) throws GRBException {

    List<Integer> layers = mlcmtc.getLayers();

    for (int layer : layers) {

      List<MLCMVertex> verticesInLayer = mlcmtc.getVerticesForLayer(layer);

      // Iterate over all triples of vertices
      for (int h = 0; h < verticesInLayer.size(); h++) {
        MLCMVertex u = verticesInLayer.get(h);
        for (int i = 0; i < verticesInLayer.size(); i++) {
          MLCMVertex v = verticesInLayer.get(i);
          for (int j = 0; j < verticesInLayer.size(); j++) {
            MLCMVertex w = verticesInLayer.get(j);

            if (u == v || u == w || v == w) {
              continue;
            }

            // If the vertices are in the same frame, then add a transitivity constraint
            if (u.getFrame() == v.getFrame() && v.getFrame() == w.getFrame()) {

              // Find groupIDs for the vertices
              int groupID1 = u.getGroupId();
              int groupID2 = v.getGroupId();
              int groupID3 = w.getGroupId();

              // Cast the groupIDs to strings
              String groupID1Str = Integer.toString(groupID1);
              String groupID2Str = Integer.toString(groupID2);
              String groupID3Str = Integer.toString(groupID3);

              // Find frame
              int frame = u.getFrame();

              // cast the frame to a string
              String frameStr = Integer.toString(frame);

              // Create the transitivity constraint
              GRBLinExpr expr = new GRBLinExpr();
              expr.addTerm(1.0, model.getVarByName("x_" + groupID1Str + "_" + groupID2Str + "_" + frameStr));
              expr.addTerm(1.0, model.getVarByName("x_" + groupID2Str + "_" + groupID3Str + "_" + frameStr));
              expr.addTerm(-1.0, model.getVarByName("x_" + groupID1Str + "_" + groupID3Str + "_" + frameStr));

              // Add constraint that expression should be less than or equal to 1.0.
              model.addConstr(expr, GRB.LESS_EQUAL, 1.0,
                  "transitivity1_" + groupID1 + "_" + groupID2 + "_" + groupID3 + "_" + frame);

              // Add constraint that expression should be greater than or equal to 0.0.
              model.addConstr(expr, GRB.GREATER_EQUAL, 0.0,
                  "transitivity0_" + groupID1 + "_" + groupID2 + "_" + groupID3 + "_" + frame);

            }

          }

        }

      }

    }

    // Update
    model.update();
    System.out.println("Transitivity constraints added.");

  }

  /**
   * Adds the tree constraints to the MLCM-TC instance.
   * 
   * @param mlcmtc The MLCM-TC instance
   * @param model  The Gurobi model
   */
  private static void addTreeConstraints(MLCMGraph mlcmtc, GRBModel model) throws GRBException {

    List<Integer> layers = mlcmtc.getLayers();

    for (int layer : layers) {
      List<MLCMVertex> verticesInLayer = mlcmtc.getVerticesForLayer(layer);

      // Iterate over all triples of vertices within the same layer
      for (int h = 0; h < verticesInLayer.size(); h++) {
        MLCMVertex u = verticesInLayer.get(h);
        for (int i = 0; i < verticesInLayer.size(); i++) {
          MLCMVertex v = verticesInLayer.get(i);
          for (int j = 0; j < verticesInLayer.size(); j++) {
            MLCMVertex w = verticesInLayer.get(j);

            if (u == v || u == w || v == w) {
              continue;
            }

            // If the vertices are in the same frame, then add a tree constraint
            if (u.getFrame() == v.getFrame() && v.getFrame() == w.getFrame()) {

              boolean p_hi = u.getParent() == v.getParent(); // u and v have the same parent
              boolean p_hj = u.getParent() == w.getParent(); // u and w have the same parent
              boolean p_ij = v.getParent() == w.getParent(); // v and w have the same parent

              // Find groupIDs for the vertices
              int groupID_H = u.getGroupId();
              int groupID_I = v.getGroupId();
              int groupID_J = w.getGroupId();

              // Find frame
              int frame = u.getFrame();

              if (p_hi && !p_hj) {

                // Add constraint that x_hj^r = x_ij^r
                GRBLinExpr expr = new GRBLinExpr();
                expr.addTerm(1.0, model.getVarByName("x_" + groupID_H + "_" + groupID_J + "_" + frame));
                expr.addTerm(-1.0, model.getVarByName("x_" + groupID_I + "_" + groupID_J + "_" + frame));

                model.addConstr(expr, GRB.EQUAL, 0.0,
                    "tree1_" + groupID_H + "_" + groupID_I + "_" + groupID_J + "_" + frame);

              } else if (p_ij && !p_hj) {

                // Add constraint that x_hi^r = x_hj^r
                GRBLinExpr expr = new GRBLinExpr();
                expr.addTerm(1.0, model.getVarByName("x_" + groupID_H + "_" + groupID_I + "_" + frame));
                expr.addTerm(-1.0, model.getVarByName("x_" + groupID_H + "_" + groupID_J + "_" + frame));

                model.addConstr(expr, GRB.EQUAL, 0.0,
                    "tree2_" + groupID_H + "_" + groupID_I + "_" + groupID_J + "_" + frame);

              }

            }

          }

        }

      }

    }

    // Update
    model.update();
    System.out.println("Tree constraints added.");

  }

  /**
   * Counts edge crossings in the model.
   */
  private static void countEdgeCrossings(MLCMGraph mlcmtc, GRBModel model, GRBQuadExpr objective,
      Map<String, Double> weightMap) throws GRBException {

    List<Integer> layers = mlcmtc.getLayers();

    // Iterate over all pairs of layers except the last layer
    for (int layer : layers.subList(0, layers.size() - 1)) {

      List<MLCMVertex> verticesInLayer = mlcmtc.getVerticesForLayer(layer);

      // Iterate over all pairs of vertices in the same layer
      for (int i = 0; i < verticesInLayer.size(); i++) {
        MLCMVertex v = verticesInLayer.get(i);
        for (int j = 0; j < verticesInLayer.size(); j++) {
          MLCMVertex u = verticesInLayer.get(j);

          if (v == u) {
            continue;
          }

          // Only consider vertices in layer
          if (v.getFrame() == layer && u.getFrame() == layer) {

            // Find groupIDs for the vertices
            int groupID1 = v.getGroupId();
            int groupID2 = u.getGroupId();

            // Check if the edge is within the same component
            String weightKey = "w_" + groupID1 + "_" + groupID2 + "_" + layer;

            double weight = weightMap.get(weightKey);

            // Find the next layer
            int nextLayer = layers.get(layers.indexOf(layer) + 1);

            // Get the decision variable for layer and layer+1
            GRBVar x_r_ij = model.getVarByName("x_" + groupID1 + "_" + groupID2 + "_" + layer);
            GRBVar x_rPlus1_ij = model.getVarByName("x_" + groupID1 + "_" + groupID2 + "_" + nextLayer);

            // Term: x^r_{i,j} + x^{r+1}_{i+1, j+1} - 2 * (x^r_{i,j} * x^{r+1}_{i+1, j+1})
            objective.addTerm(1.0, x_r_ij);
            objective.addTerm(1.0, x_rPlus1_ij);
            objective.addTerm(-2.0 * weight, x_r_ij, x_rPlus1_ij);

          }

        }

      }

    }

    System.out.println("Objective set.");

  }

}