package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.*;

public class FanPredictionService {

    private final Connection conn = DBConnection.getInstance().getConnection();
    private final FanNotificationService notificationService = new FanNotificationService();

    // ---------------------------------------------------------------
    // Shared JOIN fragment
    // ---------------------------------------------------------------
    private static final String PREDICTION_BASE =
        "SELECT fp.id, fp.match_proposal_id, fp.fan_id, fp.predicted_winner_id, " +
        "       fp.predicted_method, fp.is_locked, fp.is_scored, fp.points_earned, " +
        "       fp.season, fp.submitted_at, " +
        "       CONCAT(u.first_name,' ',u.last_name) AS fan_name, " +
        "       e.name AS event_name, e.id AS event_id, " +
        "       CONCAT(u1.first_name,' ',u1.last_name) AS fighter1_name, " +
        "       CONCAT(u2.first_name,' ',u2.last_name) AS fighter2_name, " +
        "       CONCAT(uw.first_name,' ',uw.last_name) AS predicted_winner_name " +
        "FROM fan_prediction fp " +
        "JOIN user u ON fp.fan_id = u.id " +
        "JOIN match_proposal mp ON fp.match_proposal_id = mp.id " +
        "JOIN event e ON mp.event_id = e.id " +
        "JOIN fighter f1 ON mp.fighter1_id = f1.id " +
        "JOIN user u1 ON f1.user_id = u1.id " +
        "JOIN fighter f2 ON mp.fighter2_id = f2.id " +
        "JOIN user u2 ON f2.user_id = u2.id " +
        "LEFT JOIN fighter fw ON fp.predicted_winner_id = fw.id " +
        "LEFT JOIN user uw ON fw.user_id = uw.id ";

    // ---------------------------------------------------------------
    // submitPrediction
    // ---------------------------------------------------------------
    public void submitPrediction(FanPrediction p) {
        String sql = "INSERT INTO fan_prediction " +
                     "(match_proposal_id, fan_id, predicted_winner_id, predicted_method, is_locked, season) " +
                     "VALUES (?, ?, ?, ?, 1, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getMatchProposalId());
            ps.setInt(2, p.getFanId());
            ps.setInt(3, p.getPredictedWinnerId());
            ps.setString(4, p.getPredictedMethod());
            ps.setString(5, p.getSeason());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] submitPrediction: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // hasPredicted
    // ---------------------------------------------------------------
    public boolean hasPredicted(int fanId, int matchProposalId) {
        String sql = "SELECT COUNT(*) FROM fan_prediction WHERE fan_id=? AND match_proposal_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            ps.setInt(2, matchProposalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] hasPredicted: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // getPredictionByFanAndMatch
    // ---------------------------------------------------------------
    public FanPrediction getPredictionByFanAndMatch(int fanId, int matchProposalId) {
        String sql = PREDICTION_BASE +
                     "WHERE fp.fan_id=? AND fp.match_proposal_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            ps.setInt(2, matchProposalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPrediction(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] getPredictionByFanAndMatch: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // getPredictionsByFan
    // ---------------------------------------------------------------
    public List<FanPrediction> getPredictionsByFan(int fanId) {
        List<FanPrediction> list = new ArrayList<>();
        String sql = PREDICTION_BASE +
                     "WHERE fp.fan_id=? ORDER BY fp.submitted_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPrediction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] getPredictionsByFan: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getPredictionsByMatch
    // ---------------------------------------------------------------
    public List<FanPrediction> getPredictionsByMatch(int matchProposalId) {
        List<FanPrediction> list = new ArrayList<>();
        String sql = PREDICTION_BASE +
                     "WHERE fp.match_proposal_id=? ORDER BY fp.submitted_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchProposalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPrediction(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] getPredictionsByMatch: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // lockPredictionsForEvent
    // ---------------------------------------------------------------
    public int lockPredictionsForEvent(int eventId) {
        String sql = "UPDATE fan_prediction fp " +
                     "JOIN match_proposal mp ON fp.match_proposal_id = mp.id " +
                     "SET fp.is_locked = 1 " +
                     "WHERE mp.event_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] lockPredictionsForEvent: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // scorePredictionsForFight
    // ---------------------------------------------------------------
    public int scorePredictionsForFight(int matchProposalId, int fightResultId) {
        // 1. Fetch fight result
        int actualWinnerId = 0;
        String actualMethod = null;
        Integer roundEnded = null;
        String sqlFightResult = "SELECT winner_id, method, round_ended FROM fight_result WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlFightResult)) {
            ps.setInt(1, fightResultId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    actualWinnerId = rs.getInt("winner_id");
                    actualMethod = rs.getString("method");
                    roundEnded = rs.getObject("round_ended", Integer.class);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] scorePredictionsForFight (fetch result): " + e.getMessage());
            return 0;
        }

        // 2. Fetch unscored predictions
        List<FanPrediction> predictions = new ArrayList<>();
        String sqlPreds = "SELECT * FROM fan_prediction WHERE match_proposal_id=? AND is_scored=0";
        try (PreparedStatement ps = conn.prepareStatement(sqlPreds)) {
            ps.setInt(1, matchProposalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FanPrediction p = new FanPrediction();
                    p.setId(rs.getInt("id"));
                    p.setFanId(rs.getInt("fan_id"));
                    p.setPredictedWinnerId(rs.getInt("predicted_winner_id"));
                    p.setPredictedMethod(rs.getString("predicted_method"));
                    predictions.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] scorePredictionsForFight (fetch preds): " + e.getMessage());
            return 0;
        }

        // 3 & 4. Score and update each prediction
        int scored = 0;
        String sqlUpdate = "UPDATE fan_prediction SET points_earned=?, is_scored=1 WHERE id=?";
        for (FanPrediction p : predictions) {
            int points = 0;
            boolean correctWinner = (p.getPredictedWinnerId() == actualWinnerId);
            if (correctWinner) {
                boolean correctMethod = (actualMethod != null && actualMethod.equalsIgnoreCase(p.getPredictedMethod()));
                if (!correctMethod) {
                    points = 1;
                } else {
                    // right winner + right method
                    if (roundEnded != null && roundEnded <= 2) {
                        points = 5;
                    } else {
                        points = 3;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, points);
                ps.setInt(2, p.getId());
                ps.executeUpdate();
                scored++;
            } catch (SQLException e) {
                System.err.println("[FanPredictionService] scorePredictionsForFight (update): " + e.getMessage());
            }

            // 5. Notify fan
            // Fetch winner name for notification
            String winnerName = "the winner";
            String sqlWinnerName = "SELECT CONCAT(u.first_name,' ',u.last_name) AS wname " +
                                   "FROM fighter f JOIN user u ON f.user_id=u.id WHERE f.id=?";
            try (PreparedStatement ps2 = conn.prepareStatement(sqlWinnerName)) {
                ps2.setInt(1, actualWinnerId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) winnerName = rs2.getString("wname");
                }
            } catch (SQLException e) {
                System.err.println("[FanPredictionService] scorePredictionsForFight (winner name): " + e.getMessage());
            }
            notificationService.triggerPredictionScoredNotification(p.getFanId(), winnerName, points);
        }
        return scored;
    }

    // ---------------------------------------------------------------
    // getLeaderboardBySeason
    // ---------------------------------------------------------------
    public List<LeaderboardRow> getLeaderboardBySeason(String season) {
        List<LeaderboardRow> list = new ArrayList<>();
        String sql =
            "SELECT CONCAT(u.first_name,' ',u.last_name) AS fan_name, " +
            "       u.email AS fan_email, " +
            "       COALESCE(SUM(fp.points_earned),0) AS total_points, " +
            "       COUNT(CASE WHEN fp.points_earned > 0 THEN 1 END) AS correct_predictions, " +
            "       COUNT(fp.id) AS total_predictions, " +
            "       ROUND(COUNT(CASE WHEN fp.points_earned > 0 THEN 1 END)*100.0/NULLIF(COUNT(fp.id),0),1) AS accuracy_percent " +
            "FROM fan_prediction fp " +
            "JOIN user u ON fp.fan_id = u.id " +
            "WHERE fp.is_scored = 1 AND fp.season = ? " +
            "GROUP BY fp.fan_id, u.first_name, u.last_name, u.email " +
            "ORDER BY total_points DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, season);
            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    LeaderboardRow row = new LeaderboardRow();
                    row.setRank(index++);
                    row.setFanName(rs.getString("fan_name"));
                    row.setFanEmail(rs.getString("fan_email"));
                    row.setTotalPoints(rs.getInt("total_points"));
                    row.setCorrectPredictions(rs.getInt("correct_predictions"));
                    row.setTotalPredictions(rs.getInt("total_predictions"));
                    row.setAccuracyPercent(rs.getDouble("accuracy_percent"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] getLeaderboardBySeason: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getLeaderboardByEvent
    // ---------------------------------------------------------------
    public List<LeaderboardRow> getLeaderboardByEvent(int eventId) {
        List<LeaderboardRow> list = new ArrayList<>();
        String sql =
            "SELECT CONCAT(u.first_name,' ',u.last_name) AS fan_name, " +
            "       u.email AS fan_email, " +
            "       COALESCE(SUM(fp.points_earned),0) AS total_points, " +
            "       COUNT(CASE WHEN fp.points_earned > 0 THEN 1 END) AS correct_predictions, " +
            "       COUNT(fp.id) AS total_predictions, " +
            "       ROUND(COUNT(CASE WHEN fp.points_earned > 0 THEN 1 END)*100.0/NULLIF(COUNT(fp.id),0),1) AS accuracy_percent " +
            "FROM fan_prediction fp " +
            "JOIN user u ON fp.fan_id = u.id " +
            "JOIN match_proposal mp ON fp.match_proposal_id = mp.id " +
            "WHERE mp.event_id=? AND fp.is_scored=1 " +
            "GROUP BY fp.fan_id, u.first_name, u.last_name, u.email " +
            "ORDER BY total_points DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    LeaderboardRow row = new LeaderboardRow();
                    row.setRank(index++);
                    row.setFanName(rs.getString("fan_name"));
                    row.setFanEmail(rs.getString("fan_email"));
                    row.setTotalPoints(rs.getInt("total_points"));
                    row.setCorrectPredictions(rs.getInt("correct_predictions"));
                    row.setTotalPredictions(rs.getInt("total_predictions"));
                    row.setAccuracyPercent(rs.getDouble("accuracy_percent"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] getLeaderboardByEvent: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getPredictionPercentages
    // ---------------------------------------------------------------
    public Map<String, Object> getPredictionPercentages(int matchProposalId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Get fighter IDs from match_proposal
        int fighter1Id = 0;
        int fighter2Id = 0;
        String sqlFighters = "SELECT fighter1_id, fighter2_id FROM match_proposal WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlFighters)) {
            ps.setInt(1, matchProposalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fighter1Id = rs.getInt("fighter1_id");
                    fighter2Id = rs.getInt("fighter2_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] getPredictionPercentages (fighters): " + e.getMessage());
        }

        // Fetch grouped prediction counts
        String sqlCounts =
            "SELECT predicted_winner_id, predicted_method, COUNT(*) AS cnt " +
            "FROM fan_prediction WHERE match_proposal_id=? " +
            "GROUP BY predicted_winner_id, predicted_method";

        int total = 0;
        int f1Votes = 0;
        int f2Votes = 0;
        Map<String, Integer> methodMap = new LinkedHashMap<>();
        methodMap.put("KO", 0);
        methodMap.put("TKO", 0);
        methodMap.put("SUBMISSION", 0);
        methodMap.put("DECISION", 0);
        methodMap.put("DRAW", 0);

        try (PreparedStatement ps = conn.prepareStatement(sqlCounts)) {
            ps.setInt(1, matchProposalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int winnerId = rs.getInt("predicted_winner_id");
                    String method = rs.getString("predicted_method");
                    int cnt = rs.getInt("cnt");
                    total += cnt;
                    if (winnerId == fighter1Id) f1Votes += cnt;
                    else if (winnerId == fighter2Id) f2Votes += cnt;
                    if (method != null) {
                        String key = method.toUpperCase();
                        methodMap.put(key, methodMap.getOrDefault(key, 0) + cnt);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPredictionService] getPredictionPercentages (counts): " + e.getMessage());
        }

        double divisor = (total > 0) ? total : 1.0;
        result.put("total", total);
        result.put("fighter1_pct", Math.round(f1Votes * 100.0 / divisor * 10) / 10.0);
        result.put("fighter2_pct", Math.round(f2Votes * 100.0 / divisor * 10) / 10.0);
        result.put("KO_pct",         Math.round(methodMap.get("KO")         * 100.0 / divisor * 10) / 10.0);
        result.put("TKO_pct",        Math.round(methodMap.get("TKO")        * 100.0 / divisor * 10) / 10.0);
        result.put("SUBMISSION_pct", Math.round(methodMap.get("SUBMISSION") * 100.0 / divisor * 10) / 10.0);
        result.put("DECISION_pct",   Math.round(methodMap.get("DECISION")   * 100.0 / divisor * 10) / 10.0);
        result.put("DRAW_pct",       Math.round(methodMap.get("DRAW")       * 100.0 / divisor * 10) / 10.0);
        return result;
    }

    // ---------------------------------------------------------------
    // Private helper
    // ---------------------------------------------------------------
    private FanPrediction mapPrediction(ResultSet rs) throws SQLException {
        FanPrediction p = new FanPrediction();
        p.setId(rs.getInt("id"));
        p.setMatchProposalId(rs.getInt("match_proposal_id"));
        p.setFanId(rs.getInt("fan_id"));
        p.setPredictedWinnerId(rs.getInt("predicted_winner_id"));
        p.setPredictedMethod(rs.getString("predicted_method"));
        p.setLocked(rs.getBoolean("is_locked"));
        p.setScored(rs.getBoolean("is_scored"));
        p.setPointsEarned(rs.getInt("points_earned"));
        p.setSeason(rs.getString("season"));
        p.setFanName(rs.getString("fan_name"));
        p.setEventName(rs.getString("event_name"));
        p.setFighter1Name(rs.getString("fighter1_name"));
        p.setFighter2Name(rs.getString("fighter2_name"));
        p.setPredictedWinnerName(rs.getString("predicted_winner_name"));
        java.sql.Timestamp submittedAt = rs.getTimestamp("submitted_at");
        if (submittedAt != null) p.setSubmittedAt(submittedAt.toString());
        return p;
    }
}
