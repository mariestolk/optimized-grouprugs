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
     * @param f_r         The repulsion force.
     * @param f_o         The ordering force.
     * @param f_e         The encoded position force.
     * 
     * @param compAbove   The components above the current component.
     * @param compBelow   The components below the current component.
     * @param encoded_Pos The encoded position of the component.
     * 
     * @param entities_i  Half the number of entities in the component.
     * 
     * @param lowerbound  The lower bound for the solution.
     * @param upperbound  The upper bound for the solution.
     * 
     * @return The position of the component at the midpoint.
     */
    public static double solveAlpha(
            double f_r,
            double f_o,
            double f_e,

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

                // double f_e = (1 / (HEIGHT / 2));
                double f_e = 0.05; // equal to 1/20

                double k = 0.1;
                double desiredSpacing = 10;

                double offset = 0.000000001;
                double outcome = 0;

                // add midpoint force
                // outcome += f_m * (m_i - alpha_i);

                // Add encoded position force
                outcome += f_e * (encoded_Pos - alpha_i);

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

                // Add repulsion force above
                for (CompGroupData compAbove : compAboveList) {

                    // double border_iMinus1 = compAbove.getStartPosition() +
                    // compAbove.getComp().getEntities().size();
                    // double border_i = alpha_i - entities_i;

                    double border_iMinus1 = compAbove.getStartPosition() +
                            compAbove.getComp().getEntities().size();
                    double border_i = alpha_i;

                    // Add small offset to avoid division by zero
                    if (border_i - border_iMinus1 == 0) {
                        border_i -= offset;
                    }

                    outcome += (1 / (Math.abs(border_i - border_iMinus1)));
                }

                // Add repulsion force below
                for (CompGroupData compBelow : compBelowList) {

                    double border_iPlus1 = compBelow.getStartPosition();
                    double border_i = alpha_i + cgd.getComp().getEntities().size();

                    if (border_iPlus1 - border_i == 0) {
                        border_i += offset;
                    }

                    outcome -= (1 / (Math.abs(border_iPlus1 - border_i)));

                }

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

}
