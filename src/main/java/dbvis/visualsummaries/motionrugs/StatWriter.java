/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.motionrugs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import dbvis.visualsummaries.data.DataPoint;
import dbvis.visualsummaries.data.DataSet;
import dbvis.visualsummaries.data.SessionData;
import dbvis.visualsummaries.data.StrategyStatistics;

/**
 *
 * @author jwulms
 */
public class StatWriter {

    public static void saveOrdering(SessionData sd, String stratid) {
        DataSet current = sd.getCurrentDataSet();
        DataPoint[][] da = current.getData(stratid);
        String dsname = current.getName();

        String userdir = System.getProperty("user.home");
        File orderingsfolder = new File(userdir + "/motionrugs/ordering");
        if (!orderingsfolder.exists()) {
            orderingsfolder.mkdir();
        }

        File orderingfile = new File(userdir + "/motionrugs/ordering/" + dsname + "_" + stratid + "_ordering.csv");
        try {
            // write
            FileWriter writer = new FileWriter(orderingfile);

            System.out.println("Number of frames: " + da.length);
            writer.write("frame,id\n");
            for (int x = 0; x < da.length; x++) {
                StringBuilder sb = new StringBuilder();
                int[] ranking = new int[da[x].length];
                // create array that stores ranking per id, instead of ids per rank
                for (int y = 0; y < da[x].length; y++) {
                    ranking[da[x][y].getId()] = y;
                }

                // output ranking in order of ids
                sb.append(ranking[0]);
                for (int y = 1; y < ranking.length; y++) {
                    sb.append(",");
                    sb.append(ranking[y]);
                }

                sb.append("\n");
                writer.write(sb.toString());
            }

            writer.flush();
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(StatWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void saveStats(SessionData sd, String stratid, double timingInMs) {
        DataSet current = sd.getCurrentDataSet();
        StrategyStatistics stratStats = current.getStatisticsOfStrategy(stratid);
        int nrOfFrames = current.getData(stratid).length;
        String dsname = current.getName();

        String userdir = System.getProperty("user.home");
        File statfolder = new File(userdir + "/motionrugs/statistics");
        if (!statfolder.exists()) {
            statfolder.mkdir();
        }

        File statfile = new File(userdir + "/motionrugs/statistics/" + dsname + "_" + stratid + "_statistics.csv");
        File framefile = new File(userdir + "/motionrugs/statistics/" + dsname + "_" + stratid + "_perframe.csv");
        File pointfile = new File(userdir + "/motionrugs/statistics/" + dsname + "_" + stratid + "_perpoint.csv");
        File timefile = new File(userdir + "/motionrugs/statistics/" + dsname + "_" + stratid + "_time.txt");
        try {
            FileWriter writer = new FileWriter(statfile);
            writer.write("statistic,mean,max,median,min,stdev\n");

            String[] statnames = stratStats.getStatNames();
            DescriptiveStatistics[] stats = stratStats.getStatArray();

            double[] ksdist;
            double[] ksrank;
            double[] ksproj;

            StringBuilder sb;

            for (int i = 0; i < stats.length; i++) {

                sb = new StringBuilder();

                DescriptiveStatistics stat = stats[i];

                sb.append(statnames[i]);
                sb.append(",");
                sb.append(stat.getMean());
                sb.append(",");
                sb.append(stat.getMax());
                sb.append(",");
                sb.append(stat.getPercentile(50));
                sb.append(",");
                sb.append(stat.getMin());
                sb.append(",");
                sb.append(stat.getStandardDeviation());
                sb.append("\n");

                writer.write(sb.toString());
            }
            writer.flush();
            writer.close();

            writer = new FileWriter(framefile);

            sb = new StringBuilder();
            sb.append("frame");

            for (int i = 0; i < statnames.length; i++) {
                sb.append(",");
                sb.append(statnames[i]);
            }
            sb.append("\n");
            writer.write(sb.toString());

            FileWriter writer2 = new FileWriter(pointfile);
            writer2.write("in order of ids, KSdist*KSrank*KSproj per point, one frame per line\n");

            for (int x = 1; x < nrOfFrames; x++) {

                sb = new StringBuilder();

                sb.append(x);

                for (int i = 0; i < stats.length; i++) {
                    // we don't have data for the first frame, but array indices go from 0 to
                    // nrOfFrames-2
                    sb.append(",");
                    // System.out.println("Size of statistic " + statnames[i] + " is " +
                    // stats[i].getValues().length);
                    sb.append(stats[i].getValues()[x - 1]);
                }
                sb.append("\n");

                writer.write(sb.toString());

                sb = new StringBuilder();

                ksdist = current.getPointKSdist(stratid, x - 1);
                ksrank = current.getPointKSrank(stratid, x - 1);
                ksproj = current.getPointKSproj(stratid, x - 1);

                for (int i = 0; i < current.getSingleFrame(x - 1).length; i++) {
                    sb.append(ksdist[i]);
                    sb.append("*");
                    sb.append(ksrank[i]);
                    sb.append("*");
                    sb.append(ksproj[i]);
                    sb.append(",");
                }

                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n");
                writer2.write(sb.toString());
            }

            writer.flush();
            writer.close();
            writer2.flush();
            writer2.close();

            writer = new FileWriter(timefile);
            writer.write("Time to execute is " + (timingInMs / 1000.0) + " seconds");
            writer.flush();
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(StatWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
