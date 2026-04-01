package tn.smartfight.models;

public class BlogArticle {

    private int id;
    private int categoryId;
    private String categoryName;
    private int authorId;
    private String authorName;
    private String title;
    private String content;
    private String summary;
    private String imageUrl;
    private String videoUrl;
    private String status;
    private int viewCount;
    private String createdAt;
    private String updatedAt;

    public BlogArticle() {}

    public BlogArticle(int id, int categoryId, String categoryName, int authorId, String authorName,
                       String title, String content, String summary, String imageUrl, String videoUrl,
                       String status, int viewCount, String createdAt, String updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.authorId = authorId;
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.status = status;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- Helpers ---

    public boolean isPublished() {
        return "PUBLISHED".equals(status);
    }

    public boolean isDraft() {
        return "DRAFT".equals(status);
    }

    @Override
    public String toString() {
        return title;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public boolean hasImage() { return imageUrl != null && !imageUrl.isBlank(); }
    public boolean hasVideo() { return videoUrl != null && !videoUrl.isBlank(); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
