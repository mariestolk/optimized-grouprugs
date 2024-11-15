package dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.Component;
import dbvis.visualsummaries.grouprugs.visualization.Interpolater;
import dbvis.visualsummaries.strategies.Strategy;
import javafx.scene.shape.CubicCurve;
import javafx.util.Pair;

public class MapperUtils {

    public static Double[][] readProjections(String stratid, String dsname, int frames, int entities) {

        Double[][] projections = new Double[frames][entities];

        String userdir = System.getProperty("user.home");
        File projfolder = new File(userdir + "/motionrugs/projections");
        File orderfolder = new File(userdir + "/motionrugs/ordering");

        if (!projfolder.exists()) {
            System.out.println("ERROR: No projections found.");
        }

        File projfile = null;

        switch (stratid) {
            case "UMAPStrategy":
                projfile = new File(projfolder + "/" + dsname + "_UMAP_1D.csv");
                break;
            case "Stable UMAPStrategy":
                projfile = new File(projfolder + "/" + dsname + "_UMAP_1D.csv");
                break;
            case "Stable sammon mapping":
                projfile = new File(projfolder + "/" + dsname + "_Stable Sammon mapping_1D.csv");
                break;
            case "Stable Sammon Mapping":
                projfile = new File(projfolder + "/" + dsname + "_Stable Sammon mapping_1D.csv");
                break;
            case "t-SNE (simple)":
                projfile = new File(projfolder + "/" + dsname + "_Stable t-SNE (simple)_1D.csv");
                break;
            case "First principal component":
                projfile = new File(projfolder + "/" + dsname + "_First principal component_1D.csv");
                break;
            case "PrincipalComponentStrategy":
                projfile = new File(projfolder + "/" + dsname + "_First principal component_1D.csv");
                break;
            case "Clairvoyant PC chasing":
                projfile = new File(projfolder + "/" + dsname + "_Clairvoyant PC chasing 0.5263157894736842_1D.csv");
                break;
            case "ClairvoyantPCStrategy":
                projfile = new File(projfolder + "/" + dsname + "_Clairvoyant PC chasing 0.5263157894736842_1D.csv");
                break;
            default:
                System.out.println("ERROR: Selected " + stratid + " strategy not found");
                break;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(projfile));
            String line;

            // Skip first line
            br.readLine();
            int frame = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (int i = 0; i < values.length; i++) {
                    projections[frame][i] = Double.parseDouble(values[i]);
                }
                frame++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return projections;

    }

    public static DataPoint[][] readOrderedPoint(SessionData sd, String dsname,
            Strategy selectedStrategy) {

        DataPoint[][] data = sd.getDataset(dsname).getBaseData();
        DataPoint[][] orderedPointsFull = new DataPoint[data.length][data[0].length];

        // Step 1: check if file exists
        String userdir = System.getProperty("user.home");
        File projfolder = new File(userdir + "/motionrugs/ordering");

        if (!projfolder.exists()) {
            System.out.println("ERROR: No projections found.");
        }

        File orderFile = null;

        switch (selectedStrategy.getName()) {
            case "First principal component":
                orderFile = new File(projfolder + "/" + dsname + "_First principal component_ordering.csv");
                break;
            case "UMAPStrategy":
                orderFile = new File(projfolder + "/" + dsname +
                        "_UMAP_ordering.csv");
                break;
            case "Stable sammon mapping":
                orderFile = new File(projfolder + "/" + dsname + "_Stable Sammon mapping_ordering.csv");
                break;
            case "t-SNE (simple)":
                orderFile = new File(projfolder + "/" + dsname + "_t-SNE (simple)_ordering.csv");
                break;
            default:
                System.out.println("File not found, computing orders from scratch.");
                orderFile = null;
                break;
        }

        if (orderFile == null || !orderFile.exists()) {
            DataPoint[][] orderedPoints = selectedStrategy.getOrderedValues(data,
                    dsname);
            // StatWriter.saveOrdering(sd, selectedStrategy.getName());
            return orderedPoints;
        }

        // Read through lines of file and store in orderedPointsFull
        try {
            BufferedReader br = new BufferedReader(new FileReader(orderFile));
            String line;

            // Skip first line
            br.readLine();
            int frame = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (int i = 0; i < values.length; i++) {
                    orderedPointsFull[frame][i] = data[frame][Integer.parseInt(values[i])];
                }
                frame++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return orderedPointsFull;
    }

    /**
     * Draw motion lines for the entities in the entityToPositionMapper. The motion
     * lines are drawn by interpolating between the positions of the entities in the
     * entityToPositionMapper. The interpolation is done using a Bezier spline.
     * 
     * @param entityToPosition
     * @param orderedPointsFull
     * @param data
     * @return
     */
    public static Integer[][] drawMotionLines2(
            Integer[][] entityToPosition,
            DataPoint[][] orderedPointsFull,
            DataPoint[][] data,
            String stratid,
            String dsname,
            int HEIGHT) {

        // Setup projection value
        Double[][] projectedPoints = MapperUtils.readProjections(
                stratid,
                dsname,
                entityToPosition.length,
                entityToPosition[0].length);

        Pair<Double, Double> minMaxProjections = MapperUtils.getMinMaxProjections(projectedPoints);

        // Iterate through entityToPositionMapper and draw motionlines. Anchor Points
        for (int frame = 0; frame < entityToPosition.length; frame++) {
            for (int entity = 0; entity < entityToPosition[frame].length; entity++) {

                if (entityToPosition[frame][entity] == null) {

                    int nextFrame = findClosestFrame(entityToPosition, frame, entity);
                    int transitionWidth = nextFrame - frame;

                    double framesProj = Math.min(transitionWidth / 2, 20);

                    double omega;

                    // Full MotionLines projection
                    if (frame == 0 && nextFrame == entityToPosition.length - 1) {

                        for (int i = frame; i < nextFrame; i++) {

                            double yProjection = (projectedPoints[i][entity] - minMaxProjections.getKey())
                                    / (minMaxProjections.getValue() - minMaxProjections.getKey()) * (HEIGHT - 1);
                            int yInt = (int) Math.floor(yProjection);
                            entityToPosition[i][entity] = yInt;

                        }

                    } else if (nextFrame == entityToPosition.length - 1) {

                        // Get yProjection for nextFrame
                        double yNextProjection = (projectedPoints[nextFrame][entity] - minMaxProjections.getKey())
                                / (minMaxProjections.getValue() - minMaxProjections.getKey()) * (HEIGHT - 1);

                        int yNextInt = (int) Math.floor(yNextProjection);

                        // Set up bezier spline interpolation
                        CubicCurve cubic = interpolateBezierSpline(
                                entityToPosition,
                                transitionWidth,
                                frame - 1,
                                entityToPosition[frame - 1][entity],
                                nextFrame,
                                yNextInt,
                                entity,
                                data);

                        for (int i = frame; i <= nextFrame; i++) {

                            double yBézier = approximateYForX(cubic, i, 0.01);
                            double yProjection = (projectedPoints[i][entity] - minMaxProjections.getKey())
                                    / (minMaxProjections.getValue() - minMaxProjections.getKey()) * (HEIGHT - 1);

                            if (i < frame + framesProj) {

                                omega = (i - frame) / framesProj;

                            } else {

                                omega = 1;

                            }
                            omega = omega * omega * (3 - 2 * omega);

                            int yInt = (int) Math.floor((1 - omega) * yProjection + omega * yBézier);
                            entityToPosition[i][entity] = yInt;

                        }

                    } else if (frame == 0) {

                        // Get yProjection for nextFrame
                        double yPrevProjection = (projectedPoints[frame][entity] - minMaxProjections.getKey())
                                / (minMaxProjections.getValue() - minMaxProjections.getKey()) * (HEIGHT - 1);

                        int yPrevInt = (int) Math.floor(yPrevProjection);

                        // Set up bezier spline interpolation
                        CubicCurve cubic = interpolateBezierSpline(
                                entityToPosition,
                                transitionWidth,
                                frame - 1,
                                yPrevInt,
                                nextFrame,
                                entityToPosition[nextFrame][entity],
                                entity,
                                data);

                        for (int i = frame; i < nextFrame; i++) {

                            double yBézier = approximateYForX(cubic, i, 0.01);
                            double yProjection = (projectedPoints[i][entity] - minMaxProjections.getKey())
                                    / (minMaxProjections.getValue() - minMaxProjections.getKey()) * (HEIGHT - 1);

                            if (i > nextFrame - framesProj) {

                                omega = (nextFrame - i) / framesProj;

                            } else {

                                omega = 1;

                            }
                            omega = omega * omega * (3 - 2 * omega);

                            int yInt = (int) Math.floor(omega * yProjection + (1 - omega) * yBézier);
                            entityToPosition[i][entity] = yInt;
                        }

                    } else {
                        // Set up bezier spline interpolation
                        CubicCurve cubic = interpolateBezierSpline(
                                entityToPosition,
                                transitionWidth,
                                frame - 1,
                                entityToPosition[frame - 1][entity],
                                nextFrame,
                                entityToPosition[nextFrame][entity],
                                entity,
                                data);

                        for (int i = frame; i < nextFrame; i++) {

                            double yBézier = approximateYForX(cubic, i, 0.01);
                            double yProjection = (projectedPoints[i][entity] - minMaxProjections.getKey())
                                    / (minMaxProjections.getValue() - minMaxProjections.getKey()) * (HEIGHT - 1);

                            if (i < frame + framesProj) {

                                omega = (i - frame) / framesProj;

                            } else if (i > nextFrame - framesProj) {

                                omega = (nextFrame - i) / framesProj;

                            } else {

                                omega = 1;

                            }

                            omega = omega * omega * (3 - 2 * omega);
                            omega *= 0.0;

                            int yInt = (int) Math.floor(omega * yProjection + (1 - omega) * yBézier);
                            entityToPosition[i][entity] = yInt;

                        }

                    }

                }

            }

        }

        return entityToPosition;

    }

    /**
     * Interpolates a bezier spline between an entity from x0 to x2.
     * 
     * @param entityToPosition 2D array of entity positions.
     * @param mlWidth          Width of the motion line.
     * @param x0               First timestamp of the entity.
     * @param y0               y-coordinate of the entity at first timestamp.
     * @param x2               Second timestamp of the entity.
     * @param y2               y-coordinate of the entity at second timestamp.
     * @param entityId         ID of the entity.
     * @param data             Data points.
     */
    public static CubicCurve interpolateBezierSpline(
            Integer[][] entityToPosition,
            int mlWidth,
            int x0,
            int y0,
            int x2,
            int y2,
            int entityId,
            DataPoint[][] data) {

        // Edge case: if x0 == x2, take the value at x0 or x2
        if (x0 == x2) {
            if (x0 > 0) {
                entityToPosition[x0][entityId] = y0;
                System.out.println("No motion line drawn because x0 == x2.");
                return null;
            } else {
                System.out.println("No motion line drawn because x0 == x2.");
                entityToPosition[x0][entityId] = y2;
                return null;
            }
        }

        int controlX1 = mlWidth / 3 + x0;
        int controlY1 = y0;

        int controlX2 = mlWidth / 3 * 2 + x0;
        int controlY2 = y2;

        CubicCurve cubic = new CubicCurve();
        cubic.setStartX(x0);
        cubic.setStartY(y0);

        cubic.setControlX1(controlX1);
        cubic.setControlY1(controlY1);

        cubic.setControlX2(controlX2);
        cubic.setControlY2(controlY2);

        cubic.setEndX(x2);
        cubic.setEndY(y2);

        return cubic;

        // for (int frame = x0; frame < x2; frame++) {
        // double y = approximateYForX(cubic, frame, 0.01);
        // int yInt = (int) Math.floor(y);
        // entityToPosition[frame][entityId] = yInt;
        // }

    }

    /**
     * Approximates the y-coordinate for a given x-coordinate on a Bézier cubic
     * curve. Helper function for interpolateBezierSpline and
     * interpolateBezierMotionLine.
     * 
     * @param curve  The Bézier cubic curve.
     * @param xGiven The x-coordinate to approximate the y-coordinate for.
     * @param step   The step size for the approximation.
     * @return The y-coordinate for the given x-coordinate.
     */
    private static double approximateYForX(CubicCurve curve, double xGiven, double step) {
        double closestT = 0;
        double closestX = Double.MAX_VALUE;
        for (double t = 0; t <= 1; t += step) {
            double x = Math.pow(1 - t, 3) * curve.getStartX() +
                    3 * Math.pow(1 - t, 2) * t * curve.getControlX1() +
                    3 * (1 - t) * Math.pow(t, 2) * curve.getControlX2() +
                    Math.pow(t, 3) * curve.getEndX();
            if (Math.abs(x - xGiven) < Math.abs(closestX - xGiven)) {
                closestX = x;
                closestT = t;
            }
        }
        // Use closestT to find the corresponding Y
        double y = Math.pow(1 - closestT, 3) * curve.getStartY() +
                3 * Math.pow(1 - closestT, 2) * closestT * curve.getControlY1() +
                3 * (1 - closestT) * Math.pow(closestT, 2) * curve.getControlY2() +
                Math.pow(closestT, 3) * curve.getEndY();
        return y;
    }

    /**
     * Draw motion lines for the entities in the entityToPositionMapper. The motion
     * lines are drawn by interpolating between the positions of the entities in the
     * entityToPositionMapper. The interpolation is done using a Bezier spline.
     * 
     * @param entityToPosition
     * @param orderedPointsFull
     * @param data
     * @return
     */
    public static Integer[][] drawMotionLines(Integer[][] entityToPosition, DataPoint[][] orderedPointsFull,
            DataPoint[][] data) {

        // Iterate through entityToPositionMapper and draw motionlines. Anchor Points
        for (int frame = 0; frame < entityToPosition.length; frame++) {
            for (int entity = 0; entity < entityToPosition[frame].length; entity++) {

                if (entityToPosition[frame][entity] == null) {

                    int nextFrame = findClosestFrame(entityToPosition, frame, entity);
                    int motionLineWidth = nextFrame - frame;

                    if (frame == 0 && nextFrame == (entityToPosition.length - 1)) {

                        // Determine position by picking first position in ordered.
                        int position = findEntityPosition(orderedPointsFull[frame], frame, entity);

                        Interpolater.interpolateBezierSpline(
                                entityToPosition,
                                motionLineWidth,
                                frame,
                                position,
                                nextFrame,
                                position,
                                entity,
                                data);

                    } else if (frame == 0) {

                        Interpolater.interpolateBezierSpline(
                                entityToPosition,
                                motionLineWidth,
                                frame,
                                entityToPosition[nextFrame][entity],
                                nextFrame,
                                entityToPosition[nextFrame][entity],
                                entity,
                                data);

                    } else if (nextFrame == entityToPosition.length - 1) {

                        Interpolater.interpolateBezierSpline(
                                entityToPosition,
                                motionLineWidth,
                                frame,
                                entityToPosition[frame - 1][entity],
                                nextFrame,
                                entityToPosition[frame - 1][entity],
                                entity,
                                data);

                    } else {

                        Interpolater.interpolateBezierSpline(
                                entityToPosition,
                                motionLineWidth,
                                frame,
                                entityToPosition[frame - 1][entity],
                                nextFrame,
                                entityToPosition[nextFrame][entity],
                                entity,
                                data);

                    }
                }
            }
        }

        return entityToPosition;

    }

    /**
     * Determines whether the current frame should be skipped during MotionRug
     * drawing for drawing motion lines later.
     * 
     * @param component The current maximal group
     * @param frame     The current frame
     * @param WIDTH     The width of the image
     * @return True if the frame should be skipped, false otherwise
     */
    public static boolean shouldSkipFrameForMotionLines(Component component, int frame, int WIDTH,
            int MOTIONLINES_WIDTH) {
        if (MOTIONLINES_WIDTH > 0) {
            if (!(component.getStartFrame() == 0) && (frame - component.getStartFrame()) < MOTIONLINES_WIDTH) {
                return true;
            } else if (!(component.getEndFrame() == WIDTH - 1)
                    && (component.getEndFrame() - frame) < MOTIONLINES_WIDTH) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the next closest frame where the entity is present.
     * 
     * @param frame  The current frame (first frame where
     *               etp[frame][entity] is null)
     * @param entity The entity
     * @return The closest frame where the entity is present. Returns -1 if the
     *         entity is not present in any future frames (and thus never part of a
     *         group again).
     */
    public static Integer findClosestFrame(Integer[][] entityToPosition, int frame, int entity) {
        int closestFrame = entityToPosition.length - 1;

        for (int i = frame; i < entityToPosition.length; i++) {
            if (entityToPosition[i][entity] != null) {
                closestFrame = i;
                break;
            }
        }

        return closestFrame;
    }

    /**
     * Finds entity position in the ordered dataset.
     * 
     * @param orderDataPoints
     * @param frame
     * @param entity
     * @return
     */
    private static Integer findEntityPosition(DataPoint[] orderedDataPoints, Integer frame, Integer entity) {
        int position = -1;

        for (int i = 0; i < orderedDataPoints.length; i++) {

            if (orderedDataPoints[i].getId() == entity) {

                position = i;
                return position;

            }

        }

        return position;
    }

    public static List<Component> computeCurrentGroups(
            int frame,
            List<Component> components) {

        List<Component> currentGroups = new ArrayList<Component>();

        // Determine which maximal groups are present in this column
        for (Component mg : components) {
            if (frame >= mg.getStartFrame() && frame <= mg.getEndFrame()) {
                currentGroups.add(mg);
            }
        }

        return currentGroups;
    }

    /**
     * Get the minimum and maximum projections in the data.
     * 
     * @param projections The projections
     * @return A pair containing the minimum and maximum projections
     */
    public static Pair<Double, Double> getMinMaxProjections(Double[][] projections) {

        double minProjection = Double.MAX_VALUE;
        double maxProjection = Double.MIN_VALUE;
        for (int d1 = 0; d1 < projections.length; d1++) {
            for (int d2 = 0; d2 < projections[0].length; d2++) {
                if (minProjection > projections[d1][d2]) {
                    minProjection = projections[d1][d2];
                }

                if (maxProjection < projections[d1][d2]) {
                    maxProjection = projections[d1][d2];
                }
            }
        }

        Pair<Double, Double> minMaxProjections = new Pair<Double, Double>(minProjection, maxProjection);

        return minMaxProjections;

    }

}
