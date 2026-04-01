package tn.smartfight.models;

public class FanPrediction {

    private int id;
    private int matchProposalId;
    private String eventName;
    private int fighter1Id;
    private String fighter1Name;
    private int fighter2Id;
    private String fighter2Name;
    private int fanId;
    private String fanName;
    private int predictedWinnerId;
    private String predictedWinnerName;
    private String predictedMethod;
    private String submittedAt;
    private boolean isLocked;
    private Integer pointsEarned;
    private boolean isScored;
    private String season;

    public FanPrediction() {}

    public FanPrediction(int id, int matchProposalId, String eventName,
                         int fighter1Id, String fighter1Name,
                         int fighter2Id, String fighter2Name,
                         int fanId, String fanName,
                         int predictedWinnerId, String predictedWinnerName,
                         String predictedMethod, String submittedAt,
                         boolean isLocked, Integer pointsEarned, boolean isScored, String season) {
        this.id = id;
        this.matchProposalId = matchProposalId;
        this.eventName = eventName;
        this.fighter1Id = fighter1Id;
        this.fighter1Name = fighter1Name;
        this.fighter2Id = fighter2Id;
        this.fighter2Name = fighter2Name;
        this.fanId = fanId;
        this.fanName = fanName;
        this.predictedWinnerId = predictedWinnerId;
        this.predictedWinnerName = predictedWinnerName;
        this.predictedMethod = predictedMethod;
        this.submittedAt = submittedAt;
        this.isLocked = isLocked;
        this.pointsEarned = pointsEarned;
        this.isScored = isScored;
        this.season = season;
    }

    // --- Helpers ---

    public boolean isPending() {
        return !isScored;
    }

    public String getResultLabel() {
        if (pointsEarned == null) return "\u23f3 Pending";
        if (pointsEarned == 0) return "\u274c 0 pts";
        return "\u2705 " + pointsEarned + " pts";
    }

    public String getAccuracyDisplay() {
        return predictedWinnerName + " by " + predictedMethod;
    }

    @Override
    public String toString() {
        return fighter1Name + " vs " + fighter2Name + " (" + eventName + ")";
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMatchProposalId() { return matchProposalId; }
    public void setMatchProposalId(int matchProposalId) { this.matchProposalId = matchProposalId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public int getFighter1Id() { return fighter1Id; }
    public void setFighter1Id(int fighter1Id) { this.fighter1Id = fighter1Id; }

    public String getFighter1Name() { return fighter1Name; }
    public void setFighter1Name(String fighter1Name) { this.fighter1Name = fighter1Name; }

    public int getFighter2Id() { return fighter2Id; }
    public void setFighter2Id(int fighter2Id) { this.fighter2Id = fighter2Id; }

    public String getFighter2Name() { return fighter2Name; }
    public void setFighter2Name(String fighter2Name) { this.fighter2Name = fighter2Name; }

    public int getFanId() { return fanId; }
    public void setFanId(int fanId) { this.fanId = fanId; }

    public String getFanName() { return fanName; }
    public void setFanName(String fanName) { this.fanName = fanName; }

    public int getPredictedWinnerId() { return predictedWinnerId; }
    public void setPredictedWinnerId(int predictedWinnerId) { this.predictedWinnerId = predictedWinnerId; }

    public String getPredictedWinnerName() { return predictedWinnerName; }
    public void setPredictedWinnerName(String predictedWinnerName) { this.predictedWinnerName = predictedWinnerName; }

    public String getPredictedMethod() { return predictedMethod; }
    public void setPredictedMethod(String predictedMethod) { this.predictedMethod = predictedMethod; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public boolean isScored() { return isScored; }
    public void setScored(boolean scored) { isScored = scored; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
}
