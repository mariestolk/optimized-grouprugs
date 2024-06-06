package dbvis.visualsummaries.grouprugs.metrics;

class RankDiff {

    public int id;
    public int rankdiff;

    public RankDiff(int id, int rankdiff) {
        this.id = id;
        this.rankdiff = rankdiff;
    }

    public int getId() {
        return id;
    }

    public int getRank() {
        return rankdiff;
    }
}