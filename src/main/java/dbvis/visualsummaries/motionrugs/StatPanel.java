/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.motionrugs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTable;

import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.data.StrategyStatistics;

/**
 *
 * @author Juri Buchmueller <motionrugs@dbvis.inf.uni-konstanz.de>
 */
public class StatPanel extends JPanel {


    public StatPanel(int height, String stratid) {

        this.setPreferredSize(new Dimension((int) (height * 2), height));
        this.setSize(new Dimension((int) (height * 2), height));
        this.setMaximumSize(new Dimension((int) (height * 2), height));
        this.setBackground(Color.BLACK);

        StrategyStatistics stats = SessionData.getInstance().getCurrentDataSet().getStatisticsOfStrategy(stratid);

        //        System.out.println("Total jumps: " + jumps);
//        System.out.println("Jumps Mean: " + jumpses.getMean());
//        System.out.println("Jumps Median: " + jumpses.getPercentile(50));
//        System.out.println("Jumps StDev: " + jumpses.getStandardDeviation());
//        System.out.print("\n");
//        System.out.println("Total cross: " + crossings);
//        System.out.println("Cross Mean: " + crosses.getMean());
//        System.out.println("Cross Median: " + crosses.getPercentile(50));
//        System.out.println("Cross StDev: " + crosses.getStandardDeviation());
//        System.out.print("\n");
//        System.out.println("Kendalls count: " + kendalls.getValues().length);
//        System.out.println("Kendalls Mean: " + kendalls.getMean());
//        System.out.println("Kendalls Median: " + kendalls.getPercentile(50));
//        System.out.println("Kendalls StDev: " + kendalls.getStandardDeviation());
//        System.out.println("------------------------");
        String[] colnames = {"", "Mean", "Median", "StDev"};

        String[][] values = {{"Jumps", Math.round(stats.getJumpsmean() * 1000.0) / 1000.0 + "", Math.round(stats.getJumpsmedian() * 1000.0) / 1000.0 + "", Math.round(stats.getJumpsstddev() * 1000.0) / 1000.0 + ""},
            {"Cross", Math.round(stats.getCrossmean() * 1000.0) / 1000.0 + "", Math.round(stats.getCrossmedian() * 1000.0) / 1000.0 + "", Math.round(stats.getCrossstddev() * 1000.0) / 1000.0 + ""},
            {"Kendall", Math.round(stats.getKendallsmean() * 1000.0) / 1000.0 + "", Math.round(stats.getKendallsmedian() * 1000.0) / 1000.0 + "", Math.round(stats.getKendallsstddev() * 1000.0) / 1000.0 + ""},
            {"Spatial q. (dist)", Math.round(stats.getKSdistmean() * 1000.0) / 1000.0 + "", Math.round(stats.getKSdistmedian() * 1000.0) / 1000.0 + "", Math.round(stats.getKSdiststddev() * 1000.0) / 1000.0 + ""},
            {"Spatial q. (rank)", Math.round(stats.getKSrankmean() * 1000.0) / 1000.0 + "", Math.round(stats.getKSrankmedian() * 1000.0) / 1000.0 + "", Math.round(stats.getKSrankstddev() * 1000.0) / 1000.0 + ""},
            {"Stability", Math.round(stats.getKSprojmean() * 1000.0) / 1000.0 + "", Math.round(stats.getKSprojmedian() * 1000.0) / 1000.0 + "", Math.round(stats.getKSprojstddev() * 1000.0) / 1000.0 + ""}};

        StatsTableModel sts = new StatsTableModel(colnames, values);
        
        
        JTable jt = new JTable(sts);
        jt.setDefaultRenderer(String.class, new StatsTableCellRenderer(stratid));
        jt.setEnabled(false);
        jt.getColumnModel().getColumn(0).setMinWidth((int) (height * 2 / 3));
        for(int i = 1; i < jt.getColumnCount(); i++) {
            jt.getColumnModel().getColumn(i).setMinWidth((int) (height / 3));
        }
        this.setLayout(new BorderLayout());
        this.add(jt.getTableHeader(), BorderLayout.NORTH);
        this.add(jt, BorderLayout.CENTER);
        //this.revalidate();

    }

}
