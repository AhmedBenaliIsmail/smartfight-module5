package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.session.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.stage.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.geometry.*;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;

import java.util.*;
import java.util.stream.Collectors;

public class BookingAdminController {

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblConfirmed;

    @FXML
    private Label lblCancelled;

    @FXML
    private Label lblRevenue;

    @FXML
    private ComboBox<String> cbEventFilter;

    @FXML
    private ComboBox<String> cbStatusFilter;

    @FXML
    private TableView<EventBooking> bookingsTable;

    @FXML
    private TableColumn<EventBooking, String> colEvent;

    @FXML
    private TableColumn<EventBooking, String> colFan;

    @FXML
    private TableColumn<EventBooking, String> colEmail;

    @FXML
    private TableColumn<EventBooking, String> colType;

    @FXML
    private TableColumn<EventBooking, String> colDate;

    @FXML
    private TableColumn<EventBooking, String> colStatus;

    @FXML
    private TableColumn<EventBooking, Integer> colQty;

    @FXML
    private TableColumn<EventBooking, Double> colPrice;

    @FXML
    private TableColumn<EventBooking, Void> colActions;

    private final EventBookingService bookingService = new EventBookingService();
    private List<EventBooking> allBookings;

    @FXML
    private void initialize() {
        allBookings = bookingService.getAllBookings();

        Set<String> eventNames = new LinkedHashSet<>();
        eventNames.add("All Events");
        for (EventBooking b : allBookings) {
            if (b.getEventName() != null) {
                eventNames.add(b.getEventName());
            }
        }
        cbEventFilter.getItems().addAll(eventNames);
        cbEventFilter.setValue("All Events");

        cbStatusFilter.getItems().addAll("All", "CONFIRMED", "PENDING", "CANCELLED");
        cbStatusFilter.setValue("All");

        cbEventFilter.setOnAction(e -> applyFilters());
        cbStatusFilter.setOnAction(e -> applyFilters());

        setupColumns();
        refreshStats();
        bookingsTable.setItems(FXCollections.observableArrayList(allBookings));
    }

    private void applyFilters() {
        List<EventBooking> result = new ArrayList<>(allBookings);

        String evtFilter = cbEventFilter.getValue();
        if (evtFilter != null && !"All Events".equals(evtFilter)) {
            result.removeIf(b -> !evtFilter.equals(b.getEventName()));
        }

        String statusFilter = cbStatusFilter.getValue();
        if (statusFilter != null && !"All".equals(statusFilter)) {
            result.removeIf(b -> !statusFilter.equals(b.getBookingStatus()));
        }

        bookingsTable.setItems(FXCollections.observableArrayList(result));
    }

    private void refreshStats() {
        Map<String, Integer> counts = bookingService.getCountByStatus();
        double revenue = bookingService.getTotalRevenue();
        lblTotal.setText(String.valueOf(allBookings.size()));
        lblConfirmed.setText(String.valueOf(counts.getOrDefault("CONFIRMED", 0)));
        lblCancelled.setText(String.valueOf(counts.getOrDefault("CANCELLED", 0)));
        lblRevenue.setText(String.format("%.2f TND", revenue));
    }

    private void setupColumns() {
        colEvent.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEventName()));
        colFan.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFanName()));
        colEmail.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFanEmail()));
        colType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTicketType()));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getBookingDate()));
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getBookingStatus()));
        colQty.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getTicketQuantity()));
        colPrice.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getTotalPrice()));
        addAdminActionColumn();
    }

    private void addAdminActionColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnCancel = new Button("Cancel");
            private final Button btnQR = new Button("QR");
            private final HBox box = new HBox(6, btnQR, btnCancel);

            {
                box.setAlignment(Pos.CENTER);
                btnCancel.getStyleClass().add("btn-danger");
                btnCancel.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
                btnQR.setStyle("-fx-background-color: #18181b; -fx-text-fill: #fafafa; " +
                        "-fx-border-color: #dc2626; -fx-border-radius: 4; -fx-background-radius: 4; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-cursor: hand;");
                btnCancel.setOnAction(e -> {
                    EventBooking b = getTableView().getItems().get(getIndex());
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                            "Cancel booking " + b.getBookingReference() + "?",
                            ButtonType.YES, ButtonType.NO);
                    a.showAndWait().ifPresent(bt -> {
                        if (bt == ButtonType.YES) {
                            bookingService.cancelBooking(b.getId());
                            allBookings = bookingService.getAllBookings();
                            refreshStats();
                            applyFilters();
                        }
                    });
                });
                btnQR.setOnAction(e -> {
                    EventBooking b = getTableView().getItems().get(getIndex());
                    showQRCode(b);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EventBooking b = getTableView().getItems().get(getIndex());
                    setGraphic(b.isConfirmed() ? box : null);
                }
            }
        });
    }

    private void showQRCode(EventBooking b) {
        String qrContent = "SMARTFIGHT TICKET\n" +
                "Ref: " + b.getBookingReference() + "\n" +
                "Event: " + b.getEventName() + "\n" +
                "Date: " + (b.getEventStartDate() != null ? b.getEventStartDate() : "TBD") + "\n" +
                "Venue: " + (b.getVenueCity() != null ? b.getVenueCity() : "TBD") + "\n" +
                "Fan: " + b.getFanName() + "\n" +
                "Type: " + b.getTicketType() + "\n" +
                "Qty: " + b.getTicketQuantity() + "\n" +
                "Total: " + String.format("%.2f TND", b.getTotalPrice());

        WritableImage qrImage = generateQRImage(qrContent, 250);
        if (qrImage == null) return;

        Stage popup = new Stage();
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.initOwner(bookingsTable.getScene().getWindow());

        // --- Root container ---
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #18181b; -fx-background-radius: 12; " +
                "-fx-border-color: #dc2626; -fx-border-width: 2; -fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(220,38,38,0.4), 20, 0, 0, 0);");
        root.setPrefWidth(360);

        // --- Title bar ---
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-background-color: #dc2626; -fx-background-radius: 10 10 0 0; -fx-padding: 12 16;");
        Label titleLabel = new Label("TICKET QR CODE");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900; -fx-font-style: italic;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnClose = new Button("\u2715");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> popup.close());
        titleBar.getChildren().addAll(titleLabel, spacer, btnClose);

        // make title bar draggable
        final double[] dragOffset = new double[2];
        titleBar.setOnMousePressed(e -> { dragOffset[0] = e.getScreenX() - popup.getX(); dragOffset[1] = e.getScreenY() - popup.getY(); });
        titleBar.setOnMouseDragged(e -> { popup.setX(e.getScreenX() - dragOffset[0]); popup.setY(e.getScreenY() - dragOffset[1]); });

        // --- QR image ---
        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(220);
        qrView.setFitHeight(220);
        qrView.setPreserveRatio(true);
        StackPane qrPane = new StackPane(qrView);
        qrPane.setStyle("-fx-padding: 24 0 16 0;");

        // --- Ticket details ---
        VBox details = new VBox(8);
        details.setStyle("-fx-padding: 0 28 20 28;");
        details.getChildren().addAll(
            ticketRow("REFERENCE", b.getBookingReference()),
            ticketRow("EVENT", b.getEventName()),
            ticketRow("DATE", b.getEventStartDate() != null ? b.getEventStartDate() : "TBD"),
            ticketRow("VENUE", b.getVenueCity() != null ? b.getVenueCity() : "TBD"),
            ticketRow("FAN", b.getFanName()),
            ticketRow("TYPE", b.getTicketType()),
            ticketRow("QTY", String.valueOf(b.getTicketQuantity())),
            ticketRow("TOTAL", String.format("%.2f TND", b.getTotalPrice()))
        );

        // --- Separator ---
        Region sep = new Region();
        sep.setStyle("-fx-background-color: #27272a; -fx-pref-height: 1; -fx-max-height: 1;");
        VBox.setMargin(sep, new Insets(0, 28, 0, 28));

        // --- Close button ---
        Button btnDone = new Button("CLOSE");
        btnDone.setStyle("-fx-background-color: #27272a; -fx-text-fill: #fafafa; -fx-font-weight: bold; " +
                "-fx-padding: 10 40; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
        btnDone.setOnAction(e -> popup.close());
        HBox bottomBar = new HBox(btnDone);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-padding: 12 0 20 0;");

        root.getChildren().addAll(titleBar, qrPane, sep, details, bottomBar);

        Scene scene = new Scene(root, Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();
    }

    private HBox ticketRow(String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
        lbl.setMinWidth(80);
        Label val = new Label(value != null ? value : "-");
        val.setStyle("-fx-text-fill: #fafafa; -fx-font-size: 12px; -fx-font-weight: bold;");
        HBox row = new HBox(12, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private WritableImage generateQRImage(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            WritableImage image = new WritableImage(size, size);
            PixelWriter pw = image.getPixelWriter();
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    pw.setColor(x, y, matrix.get(x, y) ? Color.web("#fafafa") : Color.web("#18181b"));
                }
            }
            return image;
        } catch (Exception e) {
            System.err.println("[BookingAdmin] QR generation failed: " + e.getMessage());
            return null;
        }
    }
}