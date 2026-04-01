package tn.smartfight.database;

import java.sql.*;

/**
 * Runs once on startup to migrate the blog_article table
 * from file-path columns (image_url, video_url) to BLOB columns
 * (image_data MEDIUMBLOB, video_data LONGBLOB).
 */
public class DBMigration {

    public static void run() {
        Connection conn = DBConnection.getInstance().getConnection();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            boolean hasImageUrl  = columnExists(meta, "blog_article", "image_url");
            boolean hasVideoUrl  = columnExists(meta, "blog_article", "video_url");
            boolean hasImageData = columnExists(meta, "blog_article", "image_data");
            boolean hasVideoData = columnExists(meta, "blog_article", "video_data");

            try (Statement st = conn.createStatement()) {
                if (!hasImageData) {
                    st.execute("ALTER TABLE blog_article ADD COLUMN image_data MEDIUMBLOB DEFAULT NULL");
                    System.out.println("[DBMigration] Added image_data column");
                }
                if (!hasVideoData) {
                    st.execute("ALTER TABLE blog_article ADD COLUMN video_data LONGBLOB DEFAULT NULL");
                    System.out.println("[DBMigration] Added video_data column");
                }
                if (hasImageUrl) {
                    st.execute("ALTER TABLE blog_article DROP COLUMN image_url");
                    System.out.println("[DBMigration] Dropped image_url column");
                }
                if (hasVideoUrl) {
                    st.execute("ALTER TABLE blog_article DROP COLUMN video_url");
                    System.out.println("[DBMigration] Dropped video_url column");
                }
            }
        } catch (SQLException e) {
            System.err.println("[DBMigration] " + e.getMessage());
        }
    }

    private static boolean columnExists(DatabaseMetaData meta, String table, String column) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            return rs.next();
        }
    }
}
