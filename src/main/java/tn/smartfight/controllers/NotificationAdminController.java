package tn.smartfight.controllers;

import tn.smartfight.services.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;

public class NotificationAdminController {

    @FXML private TextField txtBroadcastTitle;
    @FXML private TextArea txtBroadcastMessage;
    @FXML private ComboBox<String> cbTarget;
    @FXML private ComboBox<Map<String, Object>> cbTargetDiscipline;
    @FXML private Label lblSendResult;
    @FXML private TableView<Map<String, Object>> historyTable;
    @FXML private TableColumn<Map<String, Object>, String> colTitle;
    @FXML private TableColumn<Map<String, Object>, String> colType;
    @FXML private TableColumn<Map<String, Object>, String> colDate;
    @FXML private TableColumn<Map<String, Object>, Integer> colRecipients;

    private final FanNotificationService notifService = new FanNotificationService();
    private final FanPreferenceService prefService = new FanPreferenceService();

    @FXML
    public void initialize() {
        cbTarget.getItems().addAll("All Fans", "By Discipline");
        cbTarget.setValue("All Fans");
        cbTarget.setOnAction(e -> onTargetChanged());

        List<Map<String, Object>> disciplines = prefService.getAllDisciplines();
        cbTargetDiscipline.getItems().addAll(disciplines);
        cbTargetDiscipline.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> m, boolean empty) {
                super.updateItem(m, empty);
                if (empty || m == null) setText(null);
                else setText((String) m.get("name"));
            }
        });
        cbTargetDiscipline.setButtonCell(cbTargetDiscipline.getCellFactory().call(null));
        cbTargetDiscipline.setDisable(true);

        setupHistoryColumns();
        loadHistory();
    }

    @FXML
    private void onTargetChanged() {
        cbTargetDiscipline.setDisable("All Fans".equals(cbTarget.getValue()));
    }

    @FXML
    private void onSendBroadcast() {
        String title = txtBroadcastTitle.getText().trim();
        String message = txtBroadcastMessage.getText().trim();
        if (title.isEmpty() || message.isEmpty()) {
            lblSendResult.setText("⚠ Title and message are required.");
            return;
        }
        if ("By Discipline".equals(cbTarget.getValue()) && cbTargetDiscipline.getValue() == null) {
            lblSendResult.setText("⚠ Please select a discipline.");
            return;
        }
        int sent;
        if ("All Fans".equals(cbTarget.getValue())) {
            sent = notifService.broadcastToAll(title, message);
        } else {
            int discId = ((Number) cbTargetDiscipline.getValue().get("id")).intValue();
            sent = notifService.broadcastByDiscipline(discId, title, message);
        }
        lblSendResult.setText("✅ Sent to " + sent + " fans.");
        txtBroadcastTitle.clear();
        txtBroadcastMessage.clear();
        loadHistory();
    }

    private void setupHistoryColumns() {
        colTitle.setCellValueFactory(cd ->
                new SimpleStringProperty((String) cd.getValue().getOrDefault("title", "")));
        colType.setCellValueFactory(cd ->
                new SimpleStringProperty((String) cd.getValue().getOrDefault("type", "")));
        colDate.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(cd.getValue().getOrDefault("created_at", ""))));
        colRecipients.setCellValueFactory(cd ->
                new SimpleObjectProperty<>(((Number) cd.getValue().getOrDefault("recipients", 0)).intValue()));
    }

    private void loadHistory() {
        historyTable.setItems(FXCollections.observableArrayList(notifService.getBroadcastHistory()));
    }
}
