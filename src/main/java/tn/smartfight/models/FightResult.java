package tn.smartfight.models;

public class FightResult {

    private int id;
    private int eventId;
    private String eventName;
    private int fighterRedId;
    private String fighterRedName;
    private int fighterBlueId;
    private String fighterBlueName;
    private int winnerId;
    private String winnerName;
    private String method;
    private Integer roundEnded;
    private String fightDate;

    public FightResult() {}

    public FightResult(int id, int eventId, String eventName,
                       int fighterRedId, String fighterRedName,
                       int fighterBlueId, String fighterBlueName,
                       int winnerId, String winnerName,
                       String method, Integer roundEnded, String fightDate) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.fighterRedId = fighterRedId;
        this.fighterRedName = fighterRedName;
        this.fighterBlueId = fighterBlueId;
        this.fighterBlueName = fighterBlueName;
        this.winnerId = winnerId;
        this.winnerName = winnerName;
        this.method = method;
        this.roundEnded = roundEnded;
        this.fightDate = fightDate;
    }

    // --- Helpers ---

    public String getSummary() {
        return fighterRedName + " vs " + fighterBlueName + " \u2014 " + winnerName + " by " + method;
    }

    @Override
    public String toString() {
        return getSummary();
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public int getFighterRedId() { return fighterRedId; }
    public void setFighterRedId(int fighterRedId) { this.fighterRedId = fighterRedId; }

    public String getFighterRedName() { return fighterRedName; }
    public void setFighterRedName(String fighterRedName) { this.fighterRedName = fighterRedName; }

    public int getFighterBlueId() { return fighterBlueId; }
    public void setFighterBlueId(int fighterBlueId) { this.fighterBlueId = fighterBlueId; }

    public String getFighterBlueName() { return fighterBlueName; }
    public void setFighterBlueName(String fighterBlueName) { this.fighterBlueName = fighterBlueName; }

    public int getWinnerId() { return winnerId; }
    public void setWinnerId(int winnerId) { this.winnerId = winnerId; }

    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Integer getRoundEnded() { return roundEnded; }
    public void setRoundEnded(Integer roundEnded) { this.roundEnded = roundEnded; }

    public String getFightDate() { return fightDate; }
    public void setFightDate(String fightDate) { this.fightDate = fightDate; }
}
