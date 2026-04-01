package tn.smartfight.services;

import tn.smartfight.models.BlogArticle;

import java.io.*;
import java.nio.file.*;

/**
 * Hybrid media store: local media/ folder (fast) + DB BLOBs (portable).
 *
 * Reading priority:  local file → DB bytes
 * Writing:           always saves to both local folder and DB
 *
 * The media/ folder sits at the project root (next to src/, pom.xml).
 * On a new machine with no local files, everything falls back to the DB
 * and is automatically cached locally on first access.
 */
public class MediaCache {

    public static final Path MEDIA_DIR =
            Paths.get(System.getProperty("user.dir"), "media");

    public static void init() {
        try {
            Files.createDirectories(MEDIA_DIR);
        } catch (IOException e) {
            System.err.println("[MediaCache] Could not create media dir: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // SAVE
    // ------------------------------------------------------------------

    /** Copy image file to media/ folder and return its bytes for DB storage. */
    public static byte[] saveImage(File source, int articleId) throws IOException {
        byte[] bytes = Files.readAllBytes(source.toPath());
        Path dest = MEDIA_DIR.resolve("img_" + articleId + getExt(source.getName()));
        Files.write(dest, bytes);
        return bytes;
    }

    /** Copy video file to media/ folder and return its bytes for DB storage. */
    public static byte[] saveVideo(File source, int articleId) throws IOException {
        byte[] bytes = Files.readAllBytes(source.toPath());
        Path dest = MEDIA_DIR.resolve("vid_" + articleId + getExt(source.getName()));
        Files.write(dest, bytes);
        return bytes;
    }

    // ------------------------------------------------------------------
    // READ — images
    // ------------------------------------------------------------------

    /**
     * Returns an InputStream for the article's image.
     * Tries the local media/ folder first; falls back to DB bytes.
     */
    public static InputStream getImageStream(BlogArticle article) {
        if (!article.hasImage()) return null;

        // 1. Try local file
        Path local = findLocalFile("img_" + article.getId());
        if (local != null) {
            try { return Files.newInputStream(local); }
            catch (IOException ignored) {}
        }

        // 2. Fall back to DB bytes, and cache locally for next time
        byte[] bytes = article.getImageData();
        if (bytes != null && bytes.length > 0) {
            cacheImageFromDb(article.getId(), bytes);
            return new ByteArrayInputStream(bytes);
        }
        return null;
    }

    // ------------------------------------------------------------------
    // READ — videos
    // ------------------------------------------------------------------

    /**
     * Returns a file:// URI for the article's video.
     * If no local file exists, writes the DB bytes to media/ first (persistent cache).
     * Returns null if no video is available.
     */
    public static String getVideoUri(int articleId, byte[] videoBytes) {
        if (videoBytes == null || videoBytes.length == 0) return null;

        // 1. Try local file
        Path local = findLocalFile("vid_" + articleId);
        if (local != null) {
            return local.toUri().toString();
        }

        // 2. Write DB bytes to media/ and use that URI
        try {
            Path dest = MEDIA_DIR.resolve("vid_" + articleId + ".mp4");
            Files.write(dest, videoBytes);
            return dest.toUri().toString();
        } catch (IOException e) {
            System.err.println("[MediaCache] Could not cache video: " + e.getMessage());
            // Last resort: temp file
            try {
                File tmp = File.createTempFile("smf_vid_", ".mp4");
                tmp.deleteOnExit();
                Files.write(tmp.toPath(), videoBytes);
                return tmp.toURI().toString();
            } catch (IOException ex) {
                return null;
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Find first file in media/ matching prefix (any extension). */
    private static Path findLocalFile(String prefix) {
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(MEDIA_DIR, prefix + ".*")) {
            for (Path p : stream) return p;
        } catch (IOException ignored) {}
        return null;
    }

    private static void cacheImageFromDb(int articleId, byte[] bytes) {
        Path dest = MEDIA_DIR.resolve("img_" + articleId + ".jpg");
        if (Files.notExists(dest)) {
            try { Files.write(dest, bytes); }
            catch (IOException ignored) {}
        }
    }

    private static String getExt(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".bin";
    }
}
