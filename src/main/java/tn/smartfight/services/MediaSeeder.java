package tn.smartfight.services;

import tn.smartfight.database.DBConnection;

import java.io.File;
import java.sql.*;
import java.util.*;

public class MediaSeeder {

    private static final String MEDIA_DIR = System.getProperty("user.home") + "/smartfight_media/";

    private static final List<String> IMAGE_FILES = Arrays.asList(
        "09dfab70-c534-11f0-9839-6584ec7afe70.jpg",
        "63b4559eeed82c1956d19ac3_Riveros-Hit-Rivero Vs Biacho-Bilbao Arena Spain December 3 2021 (optimized) SILVER.jpeg",
        "Sanneh-UFC-10-30-23.webp",
        "Sanneh-Boxing-Virtuosos.webp",
        "062825-Ilia-Topuria-Knockout-GettyImages-2222692229.avif"
    );

    private static final List<String> VIDEO_FILES = Arrays.asList(
        "videoplayback.mp4",
        "videoplayback (1).mp4",
        "videoplayback (2).mp4"
    );

    public static void seed() {
        Connection conn = DBConnection.getInstance().getConnection();
        try {
            // Assign images to articles that have none
            List<Integer> noImage = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM blog_article WHERE image_url IS NULL OR image_url = '' ORDER BY id")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) noImage.add(rs.getInt("id"));
            }
            for (int i = 0; i < noImage.size(); i++) {
                String imgFile = IMAGE_FILES.get(i % IMAGE_FILES.size());
                File f = new File(MEDIA_DIR, imgFile);
                if (f.exists()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE blog_article SET image_url=? WHERE id=?")) {
                        ps.setString(1, f.toURI().toString());
                        ps.setInt(2, noImage.get(i));
                        ps.executeUpdate();
                    }
                }
            }

            // Assign videos to articles that have none (up to the number of video files available)
            List<Integer> noVideo = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM blog_article WHERE video_url IS NULL OR video_url = '' ORDER BY id")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) noVideo.add(rs.getInt("id"));
            }
            int videoCount = Math.min(noVideo.size(), VIDEO_FILES.size());
            for (int i = 0; i < videoCount; i++) {
                File f = new File(MEDIA_DIR, VIDEO_FILES.get(i));
                if (f.exists()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE blog_article SET video_url=? WHERE id=?")) {
                        ps.setString(1, f.toURI().toString());
                        ps.setInt(2, noVideo.get(i));
                        ps.executeUpdate();
                    }
                }
            }

            System.out.println("[MediaSeeder] Seeded images=" + noImage.size() + ", videos=" + videoCount);
        } catch (Exception e) {
            System.err.println("[MediaSeeder] " + e.getMessage());
        }
    }
}