package tn.smartfight.models;

public class Fighter {

    private int id;
    private String firstName;
    private String lastName;
    private String nickname;
    private String weightClass;
    private int wins;
    private int losses;
    private int draws;

    public Fighter() {}

    public Fighter(int id, String firstName, String lastName, String nickname,
                   String weightClass, int wins, int losses, int draws) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.weightClass = weightClass;
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
    }

    // --- Helpers ---

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getRecord() {
        return wins + "-" + losses + "-" + draws;
    }

    public int getTotalFights() {
        return wins + losses + draws;
    }

    @Override
    public String toString() {
        return nickname != null && !nickname.isEmpty()
                ? getFullName() + " \"" + nickname + "\""
                : getFullName();
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getWeightClass() { return weightClass; }
    public void setWeightClass(String weightClass) { this.weightClass = weightClass; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }
}
