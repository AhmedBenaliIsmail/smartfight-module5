package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.*;

public class FanReactionService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // Reaction type constants
    private static final List<String> REACTION_TYPES =
        Arrays.asList("FIRE", "SHOCKED", "LOVE", "ANGRY", "GOAT");

    // ---------------------------------------------------------------
    // getReactionsForFight
    // ---------------------------------------------------------------
    public List<FanReaction> getReactionsForFight(int fightResultId) {
        List<FanReaction> list = new ArrayList<>();
        String sql =
            "SELECT rx.*, " +
            "       CONCAT(u.first_name,' ',u.last_name) AS fan_name, " +
            "       CONCAT(ur.first_name,' ',ur.last_name,' vs ',ub.first_name,' ',ub.last_name) AS fight_summary " +
            "FROM fan_reaction rx " +
            "JOIN user u ON rx.fan_id = u.id " +
            "JOIN fight_result fres ON rx.fight_result_id = fres.id " +
            "JOIN fighter fred ON fres.fighter_red_id = fred.id " +
            "JOIN user ur ON fred.user_id = ur.id " +
            "JOIN fighter fblue ON fres.fighter_blue_id = fblue.id " +
            "JOIN user ub ON fblue.user_id = ub.id " +
            "WHERE rx.fight_result_id=? AND rx.is_deleted=0 " +
            "ORDER BY rx.is_pinned DESC, rx.reacted_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fightResultId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReaction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanReactionService] getReactionsForFight: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getReactionCounts
    // ---------------------------------------------------------------
    public Map<String, Integer> getReactionCounts(int fightResultId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        // Initialize all types to 0
        for (String type : REACTION_TYPES) {
            counts.put(type, 0);
        }
        String sql = "SELECT reaction_type, COUNT(*) AS cnt FROM fan_reaction " +
                     "WHERE fight_result_id=? AND is_deleted=0 GROUP BY reaction_type";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fightResultId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("reaction_type"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanReactionService] getReactionCounts: " + e.getMessage());
        }
        return counts;
    }

    // ---------------------------------------------------------------
    // hasReacted
    // ---------------------------------------------------------------
    public boolean hasReacted(int fanId, int fightResultId) {
        String sql = "SELECT COUNT(*) FROM fan_reaction " +
                     "WHERE fan_id=? AND fight_result_id=? AND is_deleted=0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            ps.setInt(2, fightResultId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanReactionService] hasReacted: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // getFanReaction
    // ---------------------------------------------------------------
    public FanReaction getFanReaction(int fanId, int fightResultId) {
        String sql = "SELECT * FROM fan_reaction WHERE fan_id=? AND fight_result_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            ps.setInt(2, fightResultId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FanReaction r = new FanReaction();
                    r.setId(rs.getInt("id"));
                    r.setFightResultId(rs.getInt("fight_result_id"));
                    r.setFanId(rs.getInt("fan_id"));
                    r.setReactionType(rs.getString("reaction_type"));
                    r.setComment(rs.getString("comment"));
                    r.setPinned(rs.getBoolean("is_pinned"));
                    r.setDeleted(rs.getBoolean("is_deleted"));
                    java.sql.Timestamp reactedAt = rs.getTimestamp("reacted_at");
                    if (reactedAt != null) r.setReactedAt(reactedAt.toString());
                    return r;
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanReactionService] getFanReaction: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // submitReaction
    // ---------------------------------------------------------------
    public void submitReaction(FanReaction r) {
        String sql = "INSERT INTO fan_reaction (fight_result_id, fan_id, reaction_type, comment) " +
                     "VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getFightResultId());
            ps.setInt(2, r.getFanId());
            ps.setString(3, r.getReactionType());
            ps.setString(4, r.getComment());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanReactionService] submitReaction: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // pinReaction
    // ---------------------------------------------------------------
    public void pinReaction(int reactionId) {
        // Step 1: Get the fight_result_id for this reaction
        int fightResultId = -1;
        String sqlGet = "SELECT fight_result_id FROM fan_reaction WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlGet)) {
            ps.setInt(1, reactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fightResultId = rs.getInt("fight_result_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanReactionService] pinReaction (get fightResultId): " + e.getMessage());
            return;
        }
        if (fightResultId == -1) return;

        // Step 2: Unpin all reactions for that fight
        String sqlUnpin = "UPDATE fan_reaction SET is_pinned=0 WHERE fight_result_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlUnpin)) {
            ps.setInt(1, fightResultId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanReactionService] pinReaction (unpin all): " + e.getMessage());
            return;
        }

        // Step 3: Pin the specific reaction
        String sqlPin = "UPDATE fan_reaction SET is_pinned=1 WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlPin)) {
            ps.setInt(1, reactionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanReactionService] pinReaction (pin): " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // deleteReaction
    // ---------------------------------------------------------------
    public void deleteReaction(int reactionId) {
        String sql = "UPDATE fan_reaction SET is_deleted=1 WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reactionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanReactionService] deleteReaction: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // getFightsWithReactionCounts
    // ---------------------------------------------------------------
    public List<Map<String, Object>> getFightsWithReactionCounts() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
            "SELECT fres.id, fres.fight_date, " +
            "       CONCAT(ur.first_name,' ',ur.last_name) AS red_name, " +
            "       CONCAT(ub.first_name,' ',ub.last_name) AS blue_name, " +
            "       COUNT(rx.id) AS reaction_count " +
            "FROM fight_result fres " +
            "JOIN fighter fred ON fres.fighter_red_id = fred.id " +
            "JOIN user ur ON fred.user_id = ur.id " +
            "JOIN fighter fblue ON fres.fighter_blue_id = fblue.id " +
            "JOIN user ub ON fblue.user_id = ub.id " +
            "LEFT JOIN fan_reaction rx ON fres.id = rx.fight_result_id AND rx.is_deleted=0 " +
            "GROUP BY fres.id, fres.fight_date, red_name, blue_name " +
            "ORDER BY reaction_count DESC, fres.fight_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                java.sql.Date fightDate = rs.getDate("fight_date");
                row.put("fight_date", fightDate != null ? fightDate.toLocalDate() : null);
                row.put("red_name", rs.getString("red_name"));
                row.put("blue_name", rs.getString("blue_name"));
                row.put("reaction_count", rs.getInt("reaction_count"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[FanReactionService] getFightsWithReactionCounts: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // Private helper
    // ---------------------------------------------------------------
    private FanReaction mapReaction(ResultSet rs) throws SQLException {
        FanReaction r = new FanReaction();
        r.setId(rs.getInt("id"));
        r.setFightResultId(rs.getInt("fight_result_id"));
        r.setFanId(rs.getInt("fan_id"));
        r.setReactionType(rs.getString("reaction_type"));
        r.setComment(rs.getString("comment"));
        r.setPinned(rs.getBoolean("is_pinned"));
        r.setDeleted(rs.getBoolean("is_deleted"));
        r.setFanName(rs.getString("fan_name"));
        r.setFightSummary(rs.getString("fight_summary"));
        java.sql.Timestamp reactedAt = rs.getTimestamp("reacted_at");
        if (reactedAt != null) r.setReactedAt(reactedAt.toString());
        return r;
    }
}
