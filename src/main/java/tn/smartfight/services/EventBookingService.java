package tn.smartfight.services;

import tn.smartfight.database.DBConnection;
import tn.smartfight.models.*;

import java.sql.*;
import java.util.*;

public class EventBookingService {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ---------------------------------------------------------------
    // Pricing constants
    // ---------------------------------------------------------------
    public static final double PRICE_VIP      = 50.0;
    public static final double PRICE_REGULAR  = 25.0;
    public static final double PRICE_STANDING = 10.0;

    // ---------------------------------------------------------------
    // getPricePerTicket
    // ---------------------------------------------------------------
    public static double getPricePerTicket(String ticketType) {
        if (ticketType == null) return PRICE_REGULAR;
        switch (ticketType.toUpperCase()) {
            case "VIP":      return PRICE_VIP;
            case "STANDING": return PRICE_STANDING;
            default:         return PRICE_REGULAR;
        }
    }

    // ---------------------------------------------------------------
    // Shared JOIN fragment
    // ---------------------------------------------------------------
    private static final String BOOKING_BASE =
        "SELECT eb.*, e.name AS event_name, e.start_date AS event_start_date, " +
        "       v.city AS venue_city, u.first_name, u.last_name, u.email " +
        "FROM event_booking eb " +
        "JOIN event e ON eb.event_id = e.id " +
        "LEFT JOIN venue v ON e.venue_id = v.id " +
        "JOIN user u ON eb.user_id = u.id ";

    // ---------------------------------------------------------------
    // getAllBookings
    // ---------------------------------------------------------------
    public List<EventBooking> getAllBookings() {
        List<EventBooking> list = new ArrayList<>();
        String sql = BOOKING_BASE + "ORDER BY eb.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EventBookingService] getAllBookings: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getBookingsByUser
    // ---------------------------------------------------------------
    public List<EventBooking> getBookingsByUser(int userId) {
        List<EventBooking> list = new ArrayList<>();
        String sql = BOOKING_BASE + "WHERE eb.user_id = ? ORDER BY eb.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventBookingService] getBookingsByUser: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // getBookingsByEvent
    // ---------------------------------------------------------------
    public List<EventBooking> getBookingsByEvent(int eventId) {
        List<EventBooking> list = new ArrayList<>();
        String sql = BOOKING_BASE + "WHERE eb.event_id = ? ORDER BY eb.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventBookingService] getBookingsByEvent: " + e.getMessage());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // isAlreadyBooked
    // ---------------------------------------------------------------
    public boolean isAlreadyBooked(int eventId, int userId) {
        String sql = "SELECT COUNT(*) FROM event_booking " +
                     "WHERE event_id=? AND user_id=? AND booking_status != 'CANCELLED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventBookingService] isAlreadyBooked: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // getTotalConfirmedQty
    // ---------------------------------------------------------------
    public int getTotalConfirmedQty(int eventId) {
        String sql = "SELECT COALESCE(SUM(ticket_quantity),0) FROM event_booking " +
                     "WHERE event_id=? AND booking_status='CONFIRMED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventBookingService] getTotalConfirmedQty: " + e.getMessage());
        }
        return 0;
    }

    // ---------------------------------------------------------------
    // getTotalRevenue
    // ---------------------------------------------------------------
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_price),0.0) FROM event_booking " +
                     "WHERE booking_status='CONFIRMED'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[EventBookingService] getTotalRevenue: " + e.getMessage());
        }
        return 0.0;
    }

    // ---------------------------------------------------------------
    // getRevenueByEvent
    // ---------------------------------------------------------------
    public double getRevenueByEvent(int eventId) {
        String sql = "SELECT COALESCE(SUM(total_price),0.0) FROM event_booking " +
                     "WHERE booking_status='CONFIRMED' AND event_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventBookingService] getRevenueByEvent: " + e.getMessage());
        }
        return 0.0;
    }

    // ---------------------------------------------------------------
    // getCountByStatus
    // ---------------------------------------------------------------
    public Map<String, Integer> getCountByStatus() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT booking_status, COUNT(*) AS cnt FROM event_booking GROUP BY booking_status";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("booking_status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[EventBookingService] getCountByStatus: " + e.getMessage());
        }
        return map;
    }

    // ---------------------------------------------------------------
    // insertBooking
    // ---------------------------------------------------------------
    public EventBooking insertBooking(EventBooking b) {
        String ref = "SF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double totalPrice = getPricePerTicket(b.getTicketType()) * b.getTicketQuantity();
        b.setBookingReference(ref);
        b.setTotalPrice(totalPrice);

        String sql = "INSERT INTO event_booking " +
                     "(event_id, user_id, booking_status, ticket_quantity, total_price, " +
                     " ticket_type, booking_date, booking_reference) " +
                     "VALUES (?, ?, 'CONFIRMED', ?, ?, ?, CURDATE(), ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getEventId());
            ps.setInt(2, b.getUserId());
            ps.setInt(3, b.getTicketQuantity());
            ps.setDouble(4, totalPrice);
            ps.setString(5, b.getTicketType());
            ps.setString(6, ref);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    b.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventBookingService] insertBooking: " + e.getMessage());
        }
        return b;
    }

    // ---------------------------------------------------------------
    // cancelBooking
    // ---------------------------------------------------------------
    public boolean cancelBooking(int bookingId) {
        String sql = "UPDATE event_booking SET booking_status='CANCELLED', " +
                     "updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[EventBookingService] cancelBooking: " + e.getMessage());
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Private helper
    // ---------------------------------------------------------------
    private EventBooking mapBooking(ResultSet rs) throws SQLException {
        EventBooking b = new EventBooking();
        b.setId(rs.getInt("id"));
        b.setEventId(rs.getInt("event_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setTicketQuantity(rs.getInt("ticket_quantity"));
        b.setTotalPrice(rs.getDouble("total_price"));
        b.setTicketType(rs.getString("ticket_type"));
        b.setBookingReference(rs.getString("booking_reference"));
        b.setEventName(rs.getString("event_name"));
        b.setVenueCity(rs.getString("venue_city"));
        b.setFanName(rs.getString("first_name") + " " + rs.getString("last_name"));
        b.setFanEmail(rs.getString("email"));
        java.sql.Date bookingDate = rs.getDate("booking_date");
        if (bookingDate != null) b.setBookingDate(bookingDate.toString());
        java.sql.Date eventStartDate = rs.getDate("event_start_date");
        if (eventStartDate != null) b.setEventStartDate(eventStartDate.toString());
        return b;
    }
}
