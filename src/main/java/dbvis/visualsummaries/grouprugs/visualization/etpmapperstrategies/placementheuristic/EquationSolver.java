package dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.placementheuristic;

import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Solves the equation for alpha_i. A negative value for f.value(alpha_i) means
 * that the component must be pushed up (\alpha_i := \alpha_i-1), while a
 * positive value means that the component must be pushed down (\alpha_i :=
 * \alpha_i+1).
 */
public class EquationSolver {

    /**
     * Solve the equation for alpha_i.
     * 
     * @param compAbove   The components above the current component.
     * @param compBelow   The components below the current component.
     * @param cgd         The current component.
     * 
     * @param encoded_Pos The encoded position of the component.
     * @param HEIGHT      The height of the visualization.
     * 
     * @param lowerbound  The lower bound for the solution.
     * @param upperbound  The upper bound for the solution.
     * 
     * @return The position of the component at the midpoint.
     */
    public static double solveAlpha(

            List<CompGroupData> compAboveList,
            List<CompGroupData> compBelowList,
            CompGroupData cgd,

            double encoded_Pos,
            int HEIGHT,

            double init_alpha_i,

            double lowerbound,
            double upperbound,
            int id) {

        UnivariateFunction f = new UnivariateFunction() {
            @Override
            public double value(double alpha_i) {

                double f_e = (1 / ((double) HEIGHT));
                double f_r = 1;

                double offset = 0.000000001;
                double outcome = 0;

                // Add repulsion force above
                for (CompGroupData compAbove : compAboveList) {

                    double border_iMinus1 = compAbove.getStartPosition() +
                            compAbove.getComp().getEntities().size();
                    double border_i = alpha_i;

                    double distance = border_i - border_iMinus1;

                    if (distance < 10) {

                        // Add small offset to avoid division by zero
                        if (distance == 0) {
                            border_i -= offset;
                        }

                        outcome += (1 / (Math.abs(distance)));
                    } else {
                        outcome += f_r * (1 / (Math.abs(distance)));
                    }
                }

                // Add repulsion force below
                for (CompGroupData compBelow : compBelowList) {

                    double border_iPlus1 = compBelow.getStartPosition();
                    double border_i = alpha_i + cgd.getComp().getEntities().size();

                    double distance = border_iPlus1 - border_i;

                    if (distance < 10) {

                        if (distance == 0) {
                            border_i += offset;
                        }

                        outcome -= (1 / (Math.abs(distance)));
                    } else {
                        outcome -= f_r * (1 / (Math.abs(distance)));
                    }

                }

                // Add encoded position force
                outcome += f_e * (encoded_Pos - alpha_i);

                return outcome;

            }
        };

        double alpha_i;

        if (f.value(init_alpha_i) < 0) {
            alpha_i = init_alpha_i - 1;
        } else if (f.value(init_alpha_i) > 0) {
            alpha_i = init_alpha_i + 1;
        } else {
            alpha_i = init_alpha_i;
        }
        if (alpha_i < lowerbound) {
            alpha_i = lowerbound;
        } else if (alpha_i > upperbound) {
            alpha_i = upperbound;
        }

        return alpha_i;

    }

    /**
     * Used to represent the ordering force in previous version.
     * 
     * @param alpha_i        The current position of the component.
     * @param k              The steepness of the sigmoid function.
     * @param desiredSpacing The desired spacing between components.
     * @param f_o            The force of the sigmoid function.
     * @param HEIGHT         The height of the visualization.
     * @param offset         A small offset to avoid division by zero.
     * @param cgd            The current component.
     * @param compAboveList  The components above the current component.
     * @param compBelowList  The components below the current component.
     * @param entities_i     The number of entities in the current component.
     * @param outcome        The outcome of the equation.
     * @return The outcome of the equation.
     */
    private static double calculateOrderingForce(
            double alpha_i,
            double k,
            double desiredSpacing,
            double f_o,
            double HEIGHT,
            double offset,
            CompGroupData cgd,
            List<CompGroupData> compAboveList,
            List<CompGroupData> compBelowList,
            int entities_i,
            double outcome) {

        // Add order force above
        for (CompGroupData compAbove : compAboveList) {

            double y1 = compAbove.getStartPosition() +
                    compAbove.getComp().getEntities().size();
            double y2 = alpha_i; // - entities_i;
            double distance = y1 - y2;

            // double orderingForce = (HEIGHT / (1 + Math.exp(-k * (distance -
            // desiredSpacing))));
            double orderingForce = (HEIGHT / (1 + Math.exp(-k * (distance))));
            // double orderingForce = f_o * (Math.exp(-k * (distance - desiredSpacing)));
            outcome += orderingForce;
        }

        // Add order force below
        for (CompGroupData compBelow : compBelowList) {

            double y1 = alpha_i + cgd.getComp().getEntities().size(); // + entities_i;
            double y2 = compBelow.getStartPosition();
            double distance = y1 - y2;

            // double orderingForce = (HEIGHT / (1 + Math.exp(-k * (distance -
            // desiredSpacing))));
            double orderingForce = (HEIGHT / (1 + Math.exp(-k * (distance))));
            // double orderingForce = f_o * (Math.exp(-k * (distance - desiredSpacing)));
            outcome -= orderingForce;
        }

        return outcome;

    }

}
