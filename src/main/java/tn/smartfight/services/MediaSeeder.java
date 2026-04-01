package tn.smartfight.services;

import tn.smartfight.database.DBConnection;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class MediaSeeder {

    private static final List<String> IMAGE_FILES = Arrays.asList(
        "09dfab70-c534-11f0-9839-6584ec7afe70.jpg",
        "63b4559eeed82c1956d19ac3_Riveros-Hit-Rivero Vs Biacho-Bilbao Arena Spain December 3 2021 (optimized) SILVER.jpeg",
        "Sanneh-UFC-10-30-23.webp",
        "Sanneh-Boxing-Virtuosos.webp",
        "062825-Ilia-Topuria-Knockout-GettyImages-2222692229.avif",
        "img_1774945665692.png"
    );

    private static final List<String> VIDEO_FILES = Arrays.asList(
        "videoplayback.mp4",
        "videoplayback (1).mp4",
        "videoplayback (2).mp4"
    );

    public static void seed() {
        Connection conn = DBConnection.getInstance().getConnection();
        Path mediaDir = MediaCache.MEDIA_DIR;

        try {
            // --- Images ---
            List<Integer> noImage = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM blog_article WHERE image_data IS NULL ORDER BY id")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) noImage.add(rs.getInt("id"));
            }
            int imgSeeded = 0;
            for (int i = 0; i < noImage.size(); i++) {
                String imgFile = IMAGE_FILES.get(i % IMAGE_FILES.size());
                Path f = mediaDir.resolve(imgFile);
                if (Files.exists(f)) {
                    byte[] bytes = Files.readAllBytes(f);
                    int articleId = noImage.get(i);
                    // Save to DB
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE blog_article SET image_data=? WHERE id=?")) {
                        ps.setBytes(1, bytes);
                        ps.setInt(2, articleId);
                        ps.executeUpdate();
                    }
                    // Cache locally with article ID as filename
                    String ext = imgFile.substring(imgFile.lastIndexOf('.'));
                    Path cached = mediaDir.resolve("img_" + articleId + ext);
                    if (Files.notExists(cached)) Files.copy(f, cached);
                    imgSeeded++;
                }
            }

            // --- Videos ---
            List<Integer> noVideo = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM blog_article WHERE video_data IS NULL ORDER BY id")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) noVideo.add(rs.getInt("id"));
            }
            int videoSeeded = 0;
            int videoCount = Math.min(noVideo.size(), VIDEO_FILES.size());
            for (int i = 0; i < videoCount; i++) {
                Path f = mediaDir.resolve(VIDEO_FILES.get(i));
                if (Files.exists(f)) {
                    byte[] bytes = Files.readAllBytes(f);
                    int articleId = noVideo.get(i);
                    // Save to DB
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE blog_article SET video_data=? WHERE id=?")) {
                        ps.setBytes(1, bytes);
                        ps.setInt(2, articleId);
                        ps.executeUpdate();
                    }
                    // Cache locally with article ID as filename
                    Path cached = mediaDir.resolve("vid_" + articleId + ".mp4");
                    if (Files.notExists(cached)) Files.copy(f, cached);
                    videoSeeded++;
                }
            }

            System.out.println("[MediaSeeder] Seeded images=" + imgSeeded + ", videos=" + videoSeeded);
        } catch (Exception e) {
            System.err.println("[MediaSeeder] " + e.getMessage());
        }
    }
}
