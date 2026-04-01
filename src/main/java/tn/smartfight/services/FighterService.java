package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FighterService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ---------------------------------------------------------------
    // getAllActiveFighters
    // ---------------------------------------------------------------
    public List<Fighter> getAllActiveFighters() {
        List<Fighter> list = new ArrayList<>();
        String sql =
            "SELECT f.id, u.first_name, u.last_name, f.nickname, " +
            "       wc.name AS weight_class, f.wins, f.losses, f.draws " +
            "FROM fighter f " +
            "JOIN user u ON f.user_id = u.id " +
            "LEFT JOIN weight_class wc ON f.weight_class_id = wc.id " +
            "WHERE f.status = 'ACTIVE' " +
            "ORDER BY u.last_name, u.first_name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Fighter f = new Fighter();
                f.setId(rs.getInt("id"));
                f.setFirstName(rs.getString("first_name"));
                f.setLastName(rs.getString("last_name"));
                f.setNickname(rs.getString("nickname"));
                f.setWeightClass(rs.getString("weight_class"));
                f.setWins(rs.getInt("wins"));
                f.setLosses(rs.getInt("losses"));
                f.setDraws(rs.getInt("draws"));
                list.add(f);
            }
        } catch (SQLException e) {
            System.err.println("[FighterService] getAllActiveFighters: " + e.getMessage());
        }
        return list;
    }
}
