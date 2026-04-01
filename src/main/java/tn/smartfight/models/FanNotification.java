package tn.smartfight.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class FanNotification {

    private int id;
    private int fanId;
    private String type;
    private String title;
    private String message;
    private Integer relatedEventId;
    private Integer relatedFightId;
    private boolean isRead;
    private String createdAt;

    public FanNotification() {}

    public FanNotification(int id, int fanId, String type, String title, String message,
                           Integer relatedEventId, Integer relatedFightId,
                           boolean isRead, String createdAt) {
        this.id = id;
        this.fanId = fanId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedEventId = relatedEventId;
        this.relatedFightId = relatedFightId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // --- Helpers ---

    public String getTypeIcon() {
        if (type == null) return "\ud83d\udcac";
        switch (type) {
            case "NEW_EVENT":          return "\ud83d\udcc5";
            case "PREDICTION_SCORED":  return "\ud83d\uddf3";
            case "LEADERBOARD_CHANGE": return "\ud83c\udfc6";
            case "ADMIN_BROADCAST":    return "\ud83d\udce3";
            default:                   return "\ud83d\udcac";
        }
    }

    public String getTimeAgo() {
        if (createdAt == null || createdAt.isBlank()) return createdAt;
        try {
            LocalDateTime then = LocalDateTime.parse(createdAt,
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
            return createdAt;
        }
    }

    @Override
    public String toString() {
        return getTypeIcon() + " " + title;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFanId() { return fanId; }
    public void setFanId(int fanId) { this.fanId = fanId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getRelatedEventId() { return relatedEventId; }
    public void setRelatedEventId(Integer relatedEventId) { this.relatedEventId = relatedEventId; }

    public Integer getRelatedFightId() { return relatedFightId; }
    public void setRelatedFightId(Integer relatedFightId) { this.relatedFightId = relatedFightId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
