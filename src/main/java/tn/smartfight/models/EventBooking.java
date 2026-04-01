package tn.smartfight.models;

public class EventBooking {

    private int id;
    private int eventId;
    private String eventName;
    private String eventStartDate;
    private String venueCity;
    private int userId;
    private String fanName;
    private String fanEmail;
    private String bookingStatus;
    private int ticketQuantity;
    private double totalPrice;
    private String ticketType;
    private String bookingDate;
    private String bookingReference;

    public EventBooking() {}

    public EventBooking(int id, int eventId, String eventName, String eventStartDate,
                        String venueCity, int userId, String fanName, String fanEmail,
                        String bookingStatus, int ticketQuantity, double totalPrice,
                        String ticketType, String bookingDate, String bookingReference) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventStartDate = eventStartDate;
        this.venueCity = venueCity;
        this.userId = userId;
        this.fanName = fanName;
        this.fanEmail = fanEmail;
        this.bookingStatus = bookingStatus;
        this.ticketQuantity = ticketQuantity;
        this.totalPrice = totalPrice;
        this.ticketType = ticketType;
        this.bookingDate = bookingDate;
        this.bookingReference = bookingReference;
    }

    // --- Helpers ---

    public boolean isConfirmed() {
        return "CONFIRMED".equals(bookingStatus);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(bookingStatus);
    }

    @Override
    public String toString() {
        return eventName + " \u2014 " + bookingReference;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(String eventStartDate) { this.eventStartDate = eventStartDate; }

    public String getVenueCity() { return venueCity; }
    public void setVenueCity(String venueCity) { this.venueCity = venueCity; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFanName() { return fanName; }
    public void setFanName(String fanName) { this.fanName = fanName; }

    public String getFanEmail() { return fanEmail; }
    public void setFanEmail(String fanEmail) { this.fanEmail = fanEmail; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public int getTicketQuantity() { return ticketQuantity; }
    public void setTicketQuantity(int ticketQuantity) { this.ticketQuantity = ticketQuantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
}
