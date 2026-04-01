package tn.smartfight.models;

public class LeaderboardRow {

    private int rank;
    private String fanName;
    private String fanEmail;
    private int totalPoints;
    private int correctPredictions;
    private int totalPredictions;
    private double accuracyPercent;

    public LeaderboardRow() {}

    public LeaderboardRow(int rank, String fanName, String fanEmail,
                          int totalPoints, int correctPredictions,
                          int totalPredictions, double accuracyPercent) {
        this.rank = rank;
        this.fanName = fanName;
        this.fanEmail = fanEmail;
        this.totalPoints = totalPoints;
        this.correctPredictions = correctPredictions;
        this.totalPredictions = totalPredictions;
        this.accuracyPercent = accuracyPercent;
    }

    // --- Helpers ---

    public String getAccuracyDisplay() {
        return String.format("%.1f%%", accuracyPercent);
    }

    public String getRankBadge() {
        switch (rank) {
            case 1: return "#1";
            case 2: return "#2";
            case 3: return "#3";
            default: return "#" + rank;
        }
    }

    @Override
    public String toString() {
        return getRankBadge() + " " + fanName + " (" + totalPoints + " pts)";
    }

    // --- Getters & Setters ---

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getFanName() { return fanName; }
    public void setFanName(String fanName) { this.fanName = fanName; }

    public String getFanEmail() { return fanEmail; }
    public void setFanEmail(String fanEmail) { this.fanEmail = fanEmail; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getCorrectPredictions() { return correctPredictions; }
    public void setCorrectPredictions(int correctPredictions) { this.correctPredictions = correctPredictions; }

    public int getTotalPredictions() { return totalPredictions; }
    public void setTotalPredictions(int totalPredictions) { this.totalPredictions = totalPredictions; }

    public double getAccuracyPercent() { return accuracyPercent; }
    public void setAccuracyPercent(double accuracyPercent) { this.accuracyPercent = accuracyPercent; }
}
