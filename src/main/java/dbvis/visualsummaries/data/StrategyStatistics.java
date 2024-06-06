/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.data;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Juri Buchmueller <motionrugs@dbvis.inf.uni-konstanz.de>
 */
public class StrategyStatistics {

    String strategyId;
    int totaljumps;
    DescriptiveStatistics jumps;
    
    int totalcross;
    DescriptiveStatistics crosses;
    
    DescriptiveStatistics kendalls;
    
    DescriptiveStatistics KSdist;
    DescriptiveStatistics KSrank;
    DescriptiveStatistics KSdistInput;
    DescriptiveStatistics KSrankInput;
    DescriptiveStatistics KSprojection;

    public StrategyStatistics(String strategyId, int totaljumps, DescriptiveStatistics jumps, int totalcross, DescriptiveStatistics crosses, DescriptiveStatistics kendalls, DescriptiveStatistics KSdist, DescriptiveStatistics KSrank, DescriptiveStatistics KSdistInput, DescriptiveStatistics KSrankInput, DescriptiveStatistics KSproj) {
        this.strategyId = strategyId;
        this.totaljumps = totaljumps;
        this.jumps = jumps;
        this.totalcross = totalcross;
        this.crosses = crosses;
        this.kendalls = kendalls;
        this.KSdist = KSdist;
        this.KSrank = KSrank;
        this.KSdistInput = KSdistInput;
        this.KSrankInput = KSrankInput;
        this.KSprojection = KSproj;
    }
    
    public String[] getStatNames() {
        String[] output = {"jumps", "crosses", "kendalls", "Spatial quality (dist)", "Spatial quality (rank)", "Stability"};
        return output;
    }
    
    public DescriptiveStatistics[] getStatArray() {
        DescriptiveStatistics[] stats = {jumps, crosses, kendalls, KSdist, KSrank, KSprojection};
        return stats;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public int getTotaljumps() {
        return totaljumps;
    }
    
    public double[] getJumpsValues() {
        return jumps.getValues();
    }

    public double getJumpsmean() {
        return jumps.getMean();
    }

    public double getJumpsmedian() {
        return jumps.getPercentile(50);
    }

    public double getJumpsstddev() {
        return jumps.getStandardDeviation();
    }
    
    public double getJumpsmax() {
        return jumps.getMax();
    }
    
    public double getJumpsmin() {
        return jumps.getMin();
    }

    public int getTotalcross() {
        return totalcross;
    }
    
    public double[] getCrossValues() {
        return crosses.getValues();
    }

    public double getCrossmean() {
        return crosses.getMean();
    }

    public double getCrossmedian() {
        return crosses.getPercentile(50);
    }

    public double getCrossstddev() {
        return crosses.getStandardDeviation();
    }
    
    public double getCrossmax() {
        return crosses.getMax();
    }
    
    public double getCrossmin() {
        return crosses.getMin();
    }
    
    public double[] getKendallsValues() {
        return kendalls.getValues();
    }

    public double getKendallsmean() {
        return kendalls.getMean();
    }

    public double getKendallsmedian() {
        return kendalls.getPercentile(50);
    }

    public double getKendallsstddev() {
        return kendalls.getStandardDeviation();
    }
    
    public double getKendallsmax() {
        return kendalls.getMax();
    }
    
    public double getKendallsmin() {
        return kendalls.getMin();
    }
    
    public double[] getKSdistValues() {
        return KSdist.getValues();
    }

    public double getKSdistmean() {
        return KSdist.getMean();
    }

    public double getKSdistmedian() {
        return KSdist.getPercentile(50);
    }

    public double getKSdiststddev() {
        return KSdist.getStandardDeviation();
    }
    
    public double getKSdistmax() {
        return KSdist.getMax();
    }
    
    public double getKSdistmin() {
        return KSdist.getMin();
    }
    
    public double[] getKSrankValues() {
        return KSrank.getValues();
    }

    public double getKSrankmean() {
        return KSrank.getMean();
    }

    public double getKSrankmedian() {
        return KSrank.getPercentile(50);
    }

    public double getKSrankstddev() {
        return KSrank.getStandardDeviation();
    }
    
    public double getKSrankmax() {
        return KSrank.getMax();
    }
    
    public double getKSrankmin() {
        return KSrank.getMin();
    }

    public double[] getKSdistInputValues() {
        return KSdistInput.getValues();
    }
    
    public double getKSdistInputmean() {
        return KSdistInput.getMean();
    }

    public double getKSdistInputmedian() {
        return KSdistInput.getPercentile(50);
    }

    public double getKSdistInputstddev() {
        return KSdistInput.getStandardDeviation();
    }
    
    public double getKSdistInputmax() {
        return KSdistInput.getMax();
    }
    
    public double getKSdistInputmin() {
        return KSdistInput.getMin();
    }
    
    public double[] getKSrankInputValues() {
        return KSrankInput.getValues();
    }
    
    public double getKSrankInputmean() {
        return KSrankInput.getMean();
    }

    public double getKSrankInputmedian() {
        return KSrankInput.getPercentile(50);
    }

    public double getKSrankInputstddev() {
        return KSrankInput.getStandardDeviation();
    }
    
    public double getKSrankInputmax() {
        return KSrankInput.getMax();
    }
    
    public double getKSrankInputmin() {
        return KSrankInput.getMin();
    }
    
    public double[] getKSprojValues() {
        return KSprojection.getValues();
    }
    
    public double getKSprojmean() {
        return KSprojection.getMean();
    }

    public double getKSprojmedian() {
        return KSprojection.getPercentile(50);
    }

    public double getKSprojstddev() {
        return KSprojection.getStandardDeviation();
    }
    
    public double getKSprojmax() {
        return KSprojection.getMax();
    }
    
    public double getKSprojmin() {
        return KSprojection.getMin();
    }

}
