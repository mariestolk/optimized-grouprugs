package dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.placementheuristic;

import java.awt.Color;
import java.awt.BasicStroke;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JFrame;

public class ChartPlotter extends ApplicationFrame {

    public ChartPlotter(String title, UnivariateFunction f, double lb, double ub, double step) {
        super(title);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "alpha_i",
                "f(alpha_i)",
                createDataset(f, lb, ub, step),
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        // Display the chart
        ChartFrame frame = new ChartFrame("ETP Mapper", chart);
        frame.pack();
        frame.setVisible(true);

        // label values along y-axis
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);

    }

    public XYDataset createDataset(UnivariateFunction f, double lb, double ub, double step) {

        XYSeries series = new XYSeries("f(alpha_i)");

        for (double i = lb; i <= ub; i += step) {
            series.add(i, f.value(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;

    }

}
