package dbvis.visualsummaries.grouprugs.visualization;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import dbvis.visualsummaries.data.DataPoint;
import javafx.scene.shape.CubicCurve;

/**
 * Class for interpolating between two entities in the GroupRugs. Fills in
 * blanks in the entity to position array.
 */
public class Interpolater {

    /**
     * Function draws a 'motionLine' between two entities using cubic interpolation.
     * 
     * @param entityToPosition 2D array of entity positions.
     * @param mlWidth          Width of the motion line.
     * @param x0               x-coordinate of the first entity.
     * @param y0               y-coordinate of the first entity.
     * @param x2               x-coordinate of the second entity.
     * @param y2               y-coordinate of the second entity.
     * @param entityId         ID of the entity.
     * @param data             Data points.
     */
    public static void interpolateCubic(
            Integer[][] entityToPosition,
            int mlWidth,
            int x0,
            int y0,
            int x2,
            int y2,
            int entityId,
            DataPoint[][] data) throws Exception {

        x0 = x0 + 1;
        int frame = x0;

        double xstep = 1;
        double y = y0;
        double alpha = 0.0;

        for (int i = 0; i < x2; i++) {

            int yInt = (int) Math.floor(y);

            entityToPosition[frame][entityId] = yInt;

            alpha = (double) i / x2;
            alpha = alpha * alpha * (3 - 2 * alpha);

            x0 += xstep;
            y = y0 * (1 - alpha) + y2 * alpha;
            frame++;

        }

    }

    /** 
     * 
     */
    public static void interpolatePrevious() {

    }

    /**
     * Function draws a 'motionLine' between an entity between x0 and x2 using
     * natural spline interpolation.
     * 
     * @param entityToPosition 2D array of entity positions.
     * @param mlWidth          Width of the motion line.
     * @param x0               last x-coordinate of the first location (where it is
     *                         already drawn).
     * @param y0               y-coordinate of the first location (where it is
     *                         already drawn).
     * @param x2               x-coordinate of the second location (where it is
     *                         already drawn).
     * @param y2               y-coordinate of the second location (where it is
     *                         already drawn).
     * @param entityId         ID of the entity.
     * @param data             Data points.
     */
    public static void interpolateNaturalSpline(
            Integer[][] entityToPosition,
            int mlWidth,
            int x0,
            int y0,
            int x2,
            int y2,
            int entityId,
            DataPoint[][] data) throws Exception {

        int xstart = x0;
        int startFrame = x0 + 1;
        int endFrame = x2 + 1;

        double[] x_values = new double[7];
        double[] y_values = new double[7];

        // Add extra points to the spline to make it more smooth.
        x_values[0] = xstart;
        x_values[1] = startFrame;
        x_values[2] = startFrame + 1;
        x_values[3] = (mlWidth) / 2 + startFrame;
        x_values[4] = endFrame - 1;
        x_values[5] = endFrame;
        x_values[6] = endFrame + 1;

        // Add extra points to the spline to make it more smooth.
        y_values[0] = y0;
        y_values[1] = y0;
        y_values[2] = y0;
        y_values[3] = ((y2 - y0) / 2) + y0;
        y_values[4] = y2;
        y_values[5] = y2;
        y_values[6] = y2;

        SplineInterpolator splineInterpolator = new SplineInterpolator();
        PolynomialSplineFunction spline = splineInterpolator.interpolate(x_values, y_values);

        double y = y0;

        for (int frame = xstart; frame < endFrame; frame++) {
            y = spline.value(frame);

            int yInt = (int) Math.floor(y);

            entityToPosition[frame][entityId] = yInt;
        }

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
    public static void interpolateBezierSpline(
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
                return;
            } else {
                entityToPosition[x0][entityId] = y2;
                return;
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

        for (int frame = x0; frame < x2; frame++) {
            double y = approximateYForX(cubic, frame, 0.01);
            int yInt = (int) Math.floor(y);
            entityToPosition[frame][entityId] = yInt;
        }

    }

    /**
     * Interpolates a spline between an entity from x0 to x2. Using a smoothstep
     * function the interpolation is weighted between the bezier spline and the
     * projected data. Preferring the bezier spline at the start and end of the line
     * and the projected data in the middle.
     * 
     * @param entityToPosition   2D array of entity positions.
     * @param mlWidth            Width of the motion line.
     * @param x0                 First timestamp of the entity.
     * @param y0                 y-coordinate of the entity at first timestamp.
     * @param x2                 Second timestamp of the entity.
     * @param y2                 y-coordinate of the entity at second timestamp.
     * @param entityId           ID of the entity.
     * @param data               Data points.
     * @param projections        The projected data.
     * @param minProjection      The minimum value of the projected data.
     * @param maxProjection      The maximum value of the projected data.
     * @param ENLARGEMENT_FACTOR The enlargement factor.
     */
    public static void interpolateBezierMotionLine(
            Integer[][] entityToPosition,
            int mlWidth,
            int x0,
            int y0,
            int x2,
            int y2,
            int entityId,
            DataPoint[][] data,
            Double[][] projections,
            double minProjection,
            double maxProjection,
            int ENLARGEMENT_FACTOR) {

        // Edge case: if x0 == x2, take the value at x0 or x2
        if (x0 == x2) {
            if (x0 > 0) {
                entityToPosition[x0][entityId] = y0;
                return;
            } else {
                entityToPosition[x0][entityId] = y2;
                return;
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

        int HEIGHT = ENLARGEMENT_FACTOR * projections[0].length;

        double alpha = 0.0;
        double midpoint = (x0 + x2) / 2;

        for (int frame = x0; frame < x2; frame++) {

            if (frame < midpoint) {
                alpha = (double) (frame - x0) / (midpoint - x0);
            } else if (frame > midpoint) {
                alpha = (double) (x2 - frame) / (x2 - midpoint);
            } else {
                alpha = 1.0;
            }

            alpha = alpha * alpha * (3 - 2 * alpha);

            // Percentage up to which we want to rely on projected data
            // alpha *= 0.2;

            double y_bezier = approximateYForX(cubic, frame, 0.01);
            double y_proj = projections[frame][entityId];
            double y_scaled_proj = (y_proj - minProjection) / (maxProjection - minProjection) * HEIGHT;

            double y_final = (1 - alpha) * y_bezier + alpha * y_scaled_proj;

            int yInt = (int) Math.floor(y_final);
            entityToPosition[frame][entityId] = yInt;
        }

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

}