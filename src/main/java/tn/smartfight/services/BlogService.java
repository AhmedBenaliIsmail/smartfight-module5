package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlogService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ---------------------------------------------------------------
    // Shared JOIN fragment
    // ---------------------------------------------------------------
    private static final String ARTICLE_BASE =
        "SELECT ba.*, bc.name AS category_name, " +
        "       CONCAT(u.first_name,' ',u.last_name) AS author_name " +
        "FROM blog_article ba " +
        "JOIN blog_category bc ON ba.category_id = bc.id " +
        "JOIN user u ON ba.author_id = u.id ";

    // ---------------------------------------------------------------
    // getAllCategories
    // ---------------------------------------------------------------
    public List<BlogCategory> getAllCategories() {
        List<BlogCategory> list = new ArrayList<>();
        String sql = "SELECT id, name, description, slug FROM blog_category ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BlogCategory c = new BlogCategory();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                c.setSlug(rs.getString("slug"));
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getAllCategories: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getAllPublished
    // ---------------------------------------------------------------
    public List<BlogArticle> getAllPublished() {
        List<BlogArticle> list = new ArrayList<>();
        String sql = ARTICLE_BASE +
                     "WHERE ba.status = 'PUBLISHED' ORDER BY ba.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapArticle(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getAllPublished: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getAllForAdmin
    // ---------------------------------------------------------------
    public List<BlogArticle> getAllForAdmin() {
        List<BlogArticle> list = new ArrayList<>();
        String sql = ARTICLE_BASE + "ORDER BY ba.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapArticle(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getAllForAdmin: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getById
    // ---------------------------------------------------------------
    public BlogArticle getById(int id) {
        String sql = ARTICLE_BASE + "WHERE ba.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapArticle(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getById: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // getByCategory
    // ---------------------------------------------------------------
    public List<BlogArticle> getByCategory(int categoryId) {
        List<BlogArticle> list = new ArrayList<>();
        String sql = ARTICLE_BASE +
                     "WHERE ba.status = 'PUBLISHED' AND ba.category_id = ? " +
                     "ORDER BY ba.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapArticle(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] getByCategory: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // search
    // ---------------------------------------------------------------
    public List<BlogArticle> search(String keyword) {
        List<BlogArticle> list = new ArrayList<>();
        String sql = ARTICLE_BASE +
                     "WHERE ba.status = 'PUBLISHED' " +
                     "AND (ba.title LIKE ? OR ba.content LIKE ?) " +
                     "ORDER BY ba.created_at DESC";
        String pattern = "%" + keyword + "%";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapArticle(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BlogService] search: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // create
    // ---------------------------------------------------------------
    public void create(BlogArticle a) {
        String sql = "INSERT INTO blog_article " +
                     "(category_id, author_id, title, content, summary, image_url, video_url, status) " +
                     "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getCategoryId());
            ps.setInt(2, a.getAuthorId());
            ps.setString(3, a.getTitle());
            ps.setString(4, a.getContent());
            ps.setString(5, a.getSummary());
            ps.setString(6, a.getImageUrl());
            ps.setString(7, a.getVideoUrl());
            ps.setString(8, a.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BlogService] create: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // update
    // ---------------------------------------------------------------
    public void update(BlogArticle a) {
        String sql = "UPDATE blog_article " +
                     "SET category_id=?, title=?, content=?, summary=?, " +
                     "image_url=?, video_url=?, status=?, " +
                     "updated_at=CURRENT_TIMESTAMP " +
                     "WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getCategoryId());
            ps.setString(2, a.getTitle());
            ps.setString(3, a.getContent());
            ps.setString(4, a.getSummary());
            ps.setString(5, a.getImageUrl());
            ps.setString(6, a.getVideoUrl());
            ps.setString(7, a.getStatus());
            ps.setInt(8, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BlogService] update: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------
    public void delete(int id) {
        String sql = "DELETE FROM blog_article WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BlogService] delete: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // setStatus
    // ---------------------------------------------------------------
    public void setStatus(int id, String status) {
        String sql = "UPDATE blog_article SET status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BlogService] setStatus: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // incrementViewCount
    // ---------------------------------------------------------------
    public void incrementViewCount(int id) {
        String sql = "UPDATE blog_article SET view_count = view_count+1 WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BlogService] incrementViewCount: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Private helper
    // ---------------------------------------------------------------
    private String safeGetString(ResultSet rs, String column) {
        try { return rs.getString(column); } catch (SQLException e) { return null; }
    }

    private BlogArticle mapArticle(ResultSet rs) throws SQLException {
        BlogArticle a = new BlogArticle();
        a.setId(rs.getInt("id"));
        a.setCategoryId(rs.getInt("category_id"));
        a.setAuthorId(rs.getInt("author_id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setSummary(rs.getString("summary"));
        a.setImageUrl(safeGetString(rs, "image_url"));
        a.setVideoUrl(safeGetString(rs, "video_url"));
        a.setStatus(rs.getString("status"));
        a.setViewCount(rs.getInt("view_count"));
        a.setCategoryName(rs.getString("category_name"));
        a.setAuthorName(rs.getString("author_name"));
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) a.setCreatedAt(createdAt.toString());
        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) a.setUpdatedAt(updatedAt.toString());
        return a;
    }
}
