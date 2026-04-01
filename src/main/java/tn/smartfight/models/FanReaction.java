package tn.smartfight.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class FanReaction {

    private int id;
    private int fightResultId;
    private String fightSummary;
    private int fanId;
    private String fanName;
    private String reactionType;
    private String comment;
    private String reactedAt;
    private boolean isPinned;
    private boolean isDeleted;

    public FanReaction() {}

    public FanReaction(int id, int fightResultId, String fightSummary,
                       int fanId, String fanName, String reactionType,
                       String comment, String reactedAt,
                       boolean isPinned, boolean isDeleted) {
        this.id = id;
        this.fightResultId = fightResultId;
        this.fightSummary = fightSummary;
        this.fanId = fanId;
        this.fanName = fanName;
        this.reactionType = reactionType;
        this.comment = comment;
        this.reactedAt = reactedAt;
        this.isPinned = isPinned;
        this.isDeleted = isDeleted;
    }

    // --- Helpers ---

    public String getEmoji() {
        if (reactionType == null) return "\ud83d\udcac";
        switch (reactionType) {
            case "FIRE":          return "\ud83d\udd25";
            case "SHOCK":         return "\ud83d\ude31";
            case "RESPECT":       return "\ud83d\udc4f";
            case "DOMINANT":      return "\ud83d\udcaa";
            case "CONTROVERSIAL": return "\ud83e\udd14";
            default:              return "\ud83d\udcac";
        }
    }

    public String getTimeAgo() {
        if (reactedAt == null || reactedAt.isBlank()) return reactedAt;
        try {
            LocalDateTime then = LocalDateTime.parse(reactedAt,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime now = LocalDateTime.now();
            long minutes = ChronoUnit.MINUTES.between(then, now);
            if (minutes < 60) {
                return minutes + " minutes ago";
            }
            long hours = ChronoUnit.HOURS.between(then, now);
            if (hours < 24) {
                return hours + " hours ago";
            }
            long days = ChronoUnit.DAYS.between(then, now);
            return days + " days ago";
        } catch (Exception e) {
            return reactedAt;
        }
    }

    @Override
    public String toString() {
        return getEmoji() + " " + fanName + ": " + comment;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFightResultId() { return fightResultId; }
    public void setFightResultId(int fightResultId) { this.fightResultId = fightResultId; }

    public String getFightSummary() { return fightSummary; }
    public void setFightSummary(String fightSummary) { this.fightSummary = fightSummary; }

    public int getFanId() { return fanId; }
    public void setFanId(int fanId) { this.fanId = fanId; }

    public String getFanName() { return fanName; }
    public void setFanName(String fanName) { this.fanName = fanName; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getReactedAt() { return reactedAt; }
    public void setReactedAt(String reactedAt) { this.reactedAt = reactedAt; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
