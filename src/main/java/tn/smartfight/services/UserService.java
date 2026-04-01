package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ---------------------------------------------------------------
    // getUserById
    // ---------------------------------------------------------------
    public User getUserById(int id) {
        String sql = "SELECT id, first_name, last_name, email, phone, role_id, is_active " +
                     "FROM user WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserService] getUserById: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // getFanUsers
    // ---------------------------------------------------------------
    public List<User> getFanUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, phone, role_id, is_active " +
                     "FROM user WHERE role_id = 5";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserService] getFanUsers: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------
    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRoleId(rs.getInt("role_id"));
        u.setActive(rs.getBoolean("is_active"));
        return u;
    }
}
