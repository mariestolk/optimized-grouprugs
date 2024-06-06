package dbvis.visualsummaries.grouprugs.tgs;

import java.io.IOException;
import java.util.List;
import java.util.PriorityQueue;

import dbvis.visualsummaries.data.*;
import dbvis.visualsummaries.grouprugs.events.Event;
import dbvis.visualsummaries.grouprugs.events.EventComputer;
import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.*;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.PostProcessing;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.RGWriter;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraph;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.ReebGraphBuilder;
import dbvis.visualsummaries.grouprugs.tgs.reebgraph.StatusGraph;

/**
 * The TGS algorithm.
 */
public class TGS {

    double epsilon;
    double delta;
    int m;
    String filename;

    List<MaximalGroup> maximalGroups;
    ReebGraph reebGraph;

    public TGS(double epsilon, double delta, int m, String filename) {
        this.epsilon = epsilon;
        this.delta = delta;
        this.m = m;
        this.filename = filename;

        this.reebGraph = null;
        this.maximalGroups = null;
    }

    public ReebGraph compute() {
        SessionData sessionData = SessionData.getInstance();
        DataSet dataset = sessionData.getDataset(filename);

        // compute events
        PriorityQueue<Event> events = EventComputer.computeEvents(dataset.getName(), this.epsilon);

        // initialize status and reeb graph
        DataPoint[] firstFrame = dataset.getBaseData()[0];
        int frameNum = dataset.getBaseData().length - 1;

        StatusGraph statusGraph = new StatusGraph(firstFrame, this.epsilon);
        ReebGraph rg = ReebGraphBuilder.build(statusGraph, events, frameNum);

        return rg;
    }

    public ReebGraph postprocess(ReebGraph rg) {
        return PostProcessing.postProcess(rg);
    }

    public static void main(String[] args) throws IOException {

        // Load data
        String[] datapath = new String[1];
        datapath[0] = "data/";
        CSVDataLoader.checkAndLoadCSVDataSets(datapath);

        String filename = "fishdatamerge";

        /*
         * fishdata: EPSILON == 400d
         * fishdatamerge: EPSILON == 250d
         * 4clustersextracted: EPSILON == 200d
         * 
         * fake_fish: EPSILON == 1d
         * fake_fish1: EPSILON == 1d
         * allmerge: EPSILON == 1d
         * allsplit: EPSILON == 1d
         */
        double EPSILON = 250d;
        double DELTA = 1d;
        int M = 1;

        TGS tgs = new TGS(EPSILON, DELTA, M, filename);
        ReebGraph rg = tgs.compute();
        rg = tgs.postprocess(rg);

        String reebFile = filename + "_" + EPSILON + ".txt";

        RGWriter.write(rg, reebFile);
        RGWriter.read(reebFile);

        System.out.println("Done");
    }

}