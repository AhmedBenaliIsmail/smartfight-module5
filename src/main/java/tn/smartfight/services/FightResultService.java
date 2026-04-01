package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FightResultService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    private static final String BASE_SELECT =
        "SELECT fr.id, fr.event_id, e.name AS event_name, " +
        "       fr.fighter_red_id, " +
        "       CONCAT(ur.first_name,' ',ur.last_name) AS red_name, " +
        "       fr.fighter_blue_id, " +
        "       CONCAT(ub.first_name,' ',ub.last_name) AS blue_name, " +
        "       fr.winner_id, " +
        "       CONCAT(uw.first_name,' ',uw.last_name) AS winner_name, " +
        "       fr.method, fr.round_ended, fr.fight_date " +
        "FROM fight_result fr " +
        "JOIN event e ON fr.event_id = e.id " +
        "JOIN fighter fr2 ON fr.fighter_red_id = fr2.id " +
        "JOIN user ur ON fr2.user_id = ur.id " +
        "JOIN fighter fb ON fr.fighter_blue_id = fb.id " +
        "JOIN user ub ON fb.user_id = ub.id " +
        "LEFT JOIN fighter fw ON fr.winner_id = fw.id " +
        "LEFT JOIN user uw ON fw.user_id = uw.id ";

    // ---------------------------------------------------------------
    // getCompletedFightResults
    // ---------------------------------------------------------------
    public List<FightResult> getCompletedFightResults() {
        List<FightResult> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY fr.fight_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapFightResult(rs));
            }
        } catch (SQLException e) {
            System.err.println("[FightResultService] getCompletedFightResults: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getFightResultById
    // ---------------------------------------------------------------
    public FightResult getFightResultById(int id) {
        String sql = BASE_SELECT + "WHERE fr.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFightResult(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FightResultService] getFightResultById: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // getFightResultByMatchId
    // ---------------------------------------------------------------
    public FightResult getFightResultByMatchId(int matchId) {
        String sql = "SELECT * FROM fight_result WHERE match_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FightResult fr = new FightResult();
                    fr.setId(rs.getInt("id"));
                    fr.setEventId(rs.getInt("event_id"));
                    fr.setFighterRedId(rs.getInt("fighter_red_id"));
                    fr.setFighterBlueId(rs.getInt("fighter_blue_id"));
                    fr.setWinnerId(rs.getInt("winner_id"));
                    fr.setMethod(rs.getString("method"));
                    fr.setRoundEnded(rs.getObject("round_ended", Integer.class));
                    java.sql.Date fd = rs.getDate("fight_date");
                    if (fd != null) fr.setFightDate(fd.toString());
                    return fr;
                }
            }
        } catch (SQLException e) {
            System.err.println("[FightResultService] getFightResultByMatchId: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------
    private FightResult mapFightResult(ResultSet rs) throws SQLException {
        FightResult fr = new FightResult();
        fr.setId(rs.getInt("id"));
        fr.setEventId(rs.getInt("event_id"));
        fr.setEventName(rs.getString("event_name"));
        fr.setFighterRedId(rs.getInt("fighter_red_id"));
        fr.setFighterRedName(rs.getString("red_name"));
        fr.setFighterBlueId(rs.getInt("fighter_blue_id"));
        fr.setFighterBlueName(rs.getString("blue_name"));
        fr.setWinnerId(rs.getInt("winner_id"));
        fr.setWinnerName(rs.getString("winner_name"));
        fr.setMethod(rs.getString("method"));
        fr.setRoundEnded(rs.getObject("round_ended", Integer.class));
        java.sql.Date fd = rs.getDate("fight_date");
        if (fd != null) fr.setFightDate(fd.toString());
        return fr;
    }
}
