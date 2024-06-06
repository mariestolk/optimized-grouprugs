package dbvis.visualsummaries.grouprugs.visualization.etpmapperstrategies.placementheuristic;

import java.util.ArrayList;
import java.util.List;

import dbvis.visualsummaries.grouprugs.tgs.maximalgroups.Component;

public class CompGroupData {

    private Component comp;
    private List<Integer> groups;
    private int startPosition;
    private double projPosition;

    private List<CompGroupData> compAboveList;
    private List<CompGroupData> compBelowList;

    public CompGroupData(Component comp, List<Integer> groups) {
        this.comp = comp;
        this.groups = groups;
        this.startPosition = -1;
        this.projPosition = -1;

        this.compAboveList = new ArrayList<>();
        this.compBelowList = new ArrayList<>();

    }

    public Component getComp() {
        return comp;
    }

    public List<Integer> getGroups() {
        return groups;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int start) {
        this.startPosition = start;
    }

    public double getProjPosition() {
        return projPosition;
    }

    public void setProjPosition(double proj) {
        this.projPosition = proj;
    }

    public List<CompGroupData> getCompAboveList() {
        return compAboveList;
    }

    public List<CompGroupData> getCompBelowList() {
        return compBelowList;
    }

    public void addCompAbove(CompGroupData compAbove) {

        // Skip if already added
        for (CompGroupData comp : this.compAboveList) {
            if (comp.getComp().equals(compAbove.getComp())) {
                return;
            }
        }

        this.compAboveList.add(compAbove);
    }

    public void addCompBelow(CompGroupData compBelow) {

        // Skip if already added
        for (CompGroupData comp : this.compBelowList) {
            if (comp.getComp().equals(compBelow.getComp())) {
                return;
            }
        }

        this.compBelowList.add(compBelow);
    }

}
