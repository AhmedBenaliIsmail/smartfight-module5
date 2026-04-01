package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatchProposalService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ---------------------------------------------------------------
    // Static inner class MatchUp
    // ---------------------------------------------------------------
    public static class MatchUp {
        public int id;
        public int eventId;
        public int fighter1Id;
        public int fighter2Id;
        public String eventName;
        public String fighter1Name;
        public String fighter1Nickname;
        public String fighter2Name;
        public String fighter2Nickname;
        public double compatibility;

        @Override
        public String toString() {
            return fighter1Name + " vs " + fighter2Name;
        }
    }

    // ---------------------------------------------------------------
    // getAcceptedMatchesByEvent
    // ---------------------------------------------------------------
    public List<MatchUp> getAcceptedMatchesByEvent(int eventId) {
        List<MatchUp> list = new ArrayList<>();
        String sql =
            "SELECT mp.id, mp.event_id, e.name AS event_name, " +
            "       mp.fighter1_id, CONCAT(u1.first_name,' ',u1.last_name) AS f1_name, " +
            "       f1.nickname AS f1_nickname, " +
            "       mp.fighter2_id, CONCAT(u2.first_name,' ',u2.last_name) AS f2_name, " +
            "       f2.nickname AS f2_nickname, mp.compatibility " +
            "FROM match_proposal mp " +
            "JOIN event e ON mp.event_id = e.id " +
            "JOIN fighter f1 ON mp.fighter1_id = f1.id " +
            "JOIN user u1 ON f1.user_id = u1.id " +
            "JOIN fighter f2 ON mp.fighter2_id = f2.id " +
            "JOIN user u2 ON f2.user_id = u2.id " +
            "WHERE mp.event_id = ? AND mp.status = 'ACCEPTED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchUp m = new MatchUp();
                    m.id = rs.getInt("id");
                    m.eventId = rs.getInt("event_id");
                    m.eventName = rs.getString("event_name");
                    m.fighter1Id = rs.getInt("fighter1_id");
                    m.fighter1Name = rs.getString("f1_name");
                    m.fighter1Nickname = rs.getString("f1_nickname");
                    m.fighter2Id = rs.getInt("fighter2_id");
                    m.fighter2Name = rs.getString("f2_name");
                    m.fighter2Nickname = rs.getString("f2_nickname");
                    m.compatibility = rs.getDouble("compatibility");
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MatchProposalService] getAcceptedMatchesByEvent: " + e.getMessage());
        }
        return list;
    }
}
