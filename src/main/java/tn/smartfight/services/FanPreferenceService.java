package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.*;

public class FanPreferenceService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ---------------------------------------------------------------
    // getPreference
    // ---------------------------------------------------------------
    public FanPreference getPreference(int fanId) {
        String sql =
            "SELECT fp.*, d.name AS discipline_name, " +
            "       CONCAT(u.first_name,' ',u.last_name) AS fighter_name " +
            "FROM fan_preference fp " +
            "LEFT JOIN discipline d ON fp.favorite_discipline_id = d.id " +
            "LEFT JOIN fighter f ON fp.favorite_fighter_id = f.id " +
            "LEFT JOIN user u ON f.user_id = u.id " +
            "WHERE fp.fan_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FanPreference pref = new FanPreference();
                    pref.setId(rs.getInt("id"));
                    pref.setFanId(rs.getInt("fan_id"));
                    pref.setFavoriteDisciplineId(rs.getObject("favorite_discipline_id", Integer.class));
                    pref.setFavoriteFighterId(rs.getObject("favorite_fighter_id", Integer.class));
                    pref.setFavoriteDisciplineName(rs.getString("discipline_name"));
                    pref.setFavoriteFighterName(rs.getString("fighter_name"));
                    java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) pref.setUpdatedAt(updatedAt.toString());
                    return pref;
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPreferenceService] getPreference: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // savePreference
    // ---------------------------------------------------------------
    public void savePreference(FanPreference p) {
        String sql =
            "INSERT INTO fan_preference (fan_id, favorite_discipline_id, favorite_fighter_id) " +
            "VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "favorite_discipline_id=VALUES(favorite_discipline_id), " +
            "favorite_fighter_id=VALUES(favorite_fighter_id), " +
            "updated_at=CURRENT_TIMESTAMP";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getFanId());
            ps.setObject(2, p.getFavoriteDisciplineId() == 0 ? null : p.getFavoriteDisciplineId());
            ps.setObject(3, p.getFavoriteFighterId() == 0 ? null : p.getFavoriteFighterId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FanPreferenceService] savePreference: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // getFanIdsByDiscipline
    // ---------------------------------------------------------------
    public List<Integer> getFanIdsByDiscipline(int disciplineId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT fan_id FROM fan_preference WHERE favorite_discipline_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, disciplineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("fan_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FanPreferenceService] getFanIdsByDiscipline: " + e.getMessage());
        }
        return ids;
    }

    // ---------------------------------------------------------------
    // getAllFanIds
    // ---------------------------------------------------------------
    public List<Integer> getAllFanIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id FROM user WHERE role_id=5";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("[FanPreferenceService] getAllFanIds: " + e.getMessage());
        }
        return ids;
    }

    // ---------------------------------------------------------------
    // getAllDisciplines
    // ---------------------------------------------------------------
    public List<Map<String, Object>> getAllDisciplines() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, name FROM discipline ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("name", rs.getString("name"));
                list.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[FanPreferenceService] getAllDisciplines: " + e.getMessage());
        }
        return list;
    }
}
