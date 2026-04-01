package tn.smartfight.models;

public class BlogCategory {

    private int id;
    private String name;
    private String description;
    private String slug;

    public BlogCategory() {}

    public BlogCategory(int id, String name, String description, String slug) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slug = slug;
    }

    @Override
    public String toString() {
        return name;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
}
