package tn.smartfight.models;

public class FanPreference {

    private int id;
    private int fanId;
    private Integer favoriteDisciplineId;
    private String favoriteDisciplineName;
    private Integer favoriteFighterId;
    private String favoriteFighterName;
    private String createdAt;
    private String updatedAt;

    public FanPreference() {}

    public FanPreference(int id, int fanId,
                         Integer favoriteDisciplineId, String favoriteDisciplineName,
                         Integer favoriteFighterId, String favoriteFighterName,
                         String createdAt, String updatedAt) {
        this.id = id;
        this.fanId = fanId;
        this.favoriteDisciplineId = favoriteDisciplineId;
        this.favoriteDisciplineName = favoriteDisciplineName;
        this.favoriteFighterId = favoriteFighterId;
        this.favoriteFighterName = favoriteFighterName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Preferences[fan=" + fanId + ", discipline=" + favoriteDisciplineName
                + ", fighter=" + favoriteFighterName + "]";
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFanId() { return fanId; }
    public void setFanId(int fanId) { this.fanId = fanId; }

    public Integer getFavoriteDisciplineId() { return favoriteDisciplineId; }
    public void setFavoriteDisciplineId(Integer favoriteDisciplineId) { this.favoriteDisciplineId = favoriteDisciplineId; }

    public String getFavoriteDisciplineName() { return favoriteDisciplineName; }
    public void setFavoriteDisciplineName(String favoriteDisciplineName) { this.favoriteDisciplineName = favoriteDisciplineName; }

    public Integer getFavoriteFighterId() { return favoriteFighterId; }
    public void setFavoriteFighterId(Integer favoriteFighterId) { this.favoriteFighterId = favoriteFighterId; }

    public String getFavoriteFighterName() { return favoriteFighterName; }
    public void setFavoriteFighterName(String favoriteFighterName) { this.favoriteFighterName = favoriteFighterName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
