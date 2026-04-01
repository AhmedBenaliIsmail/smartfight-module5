package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    private static final String BASE_SELECT =
            "SELECT e.id, e.name, e.status, e.start_date, e.end_date, e.capacity, " +
            "       v.name AS venue_name, v.city AS venue_city, e.discipline_id " +
            "FROM event e LEFT JOIN venue v ON e.venue_id = v.id ";

    // ---------------------------------------------------------------
    // getScheduledPublicEvents
    // ---------------------------------------------------------------
    public List<Event> getScheduledPublicEvents() {
        List<Event> list = new ArrayList<>();
        String sql = BASE_SELECT +
                     "WHERE e.visibility = 'PUBLIC' AND e.status = 'SCHEDULED' " +
                     "ORDER BY e.start_date ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EventService] getScheduledPublicEvents: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getEventById
    // ---------------------------------------------------------------
    public Event getEventById(int id) {
        String sql = BASE_SELECT + "WHERE e.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEvent(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventService] getEventById: " + e.getMessage());
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------
    private Event mapEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setStatus(rs.getString("status"));
        java.sql.Date startDate = rs.getDate("start_date");
        if (startDate != null) e.setStartDate(startDate.toLocalDate());
        java.sql.Date endDate = rs.getDate("end_date");
        if (endDate != null) e.setEndDate(endDate.toLocalDate());
        e.setCapacity(rs.getInt("capacity"));
        e.setVenueName(rs.getString("venue_name"));
        e.setVenueCity(rs.getString("venue_city"));
        e.setDisciplineId(rs.getInt("discipline_id"));
        return e;
    }
}
