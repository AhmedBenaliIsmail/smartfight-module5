package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class NotificationController {

    @FXML private Label lblUnreadCount;
    @FXML private Button btnMarkAllRead;
    @FXML private Button btnShowPreferences;
    @FXML private VBox notificationsContainer;
    @FXML private VBox preferencesForm;
    @FXML private ComboBox<Object> cbDiscipline;
    @FXML private ComboBox<Fighter> cbFighter;
    @FXML private Button btnSavePreferences;
    @FXML private Label lblPrefResult;

    private final FanNotificationService notifService = new FanNotificationService();
    private final FanPreferenceService prefService = new FanPreferenceService();
    private final FighterService fighterService = new FighterService();
    private int fanId;

    @FXML
    public void initialize() {
        fanId = SessionManager.getCurrentUserId();

        btnMarkAllRead.setOnAction(e -> onMarkAllRead());
        btnShowPreferences.setOnAction(e -> {
            boolean vis = preferencesForm.isVisible();
            preferencesForm.setVisible(!vis);
            preferencesForm.setManaged(!vis);
        });
        btnSavePreferences.setOnAction(e -> onSavePreferences());

        // Build discipline combo
        cbDiscipline.getItems().add("No preference");
        List<Map<String, Object>> disciplines = prefService.getAllDisciplines();
        cbDiscipline.getItems().addAll(disciplines);
        cbDiscipline.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Object o, boolean empty) {
                super.updateItem(o, empty);
                if (empty || o == null) setText(null);
                else if (o instanceof String) setText((String) o);
                else setText((String) ((Map<?, ?>) o).get("name"));
            }
        });
        cbDiscipline.setButtonCell(cbDiscipline.getCellFactory().call(null));
        cbDiscipline.setValue("No preference");

        // Build fighter combo
        cbFighter.getItems().add(null);
        cbFighter.getItems().addAll(fighterService.getAllActiveFighters());
        cbFighter.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Fighter f, boolean empty) {
                super.updateItem(f, empty);
                if (empty) setText(null);
                else if (f == null) setText("No preference");
                else setText(f.getFullName());
            }
        });
        cbFighter.setButtonCell(cbFighter.getCellFactory().call(null));

        // Load existing preference
        FanPreference existing = prefService.getPreference(fanId);
        if (existing != null) {
            if (existing.getFavoriteDisciplineId() != null) {
                for (Object o : cbDiscipline.getItems()) {
                    if (o instanceof Map) {
                        int id = ((Number) ((Map<?, ?>) o).get("id")).intValue();
                        if (id == existing.getFavoriteDisciplineId()) {
                            cbDiscipline.setValue(o);
                            break;
                        }
                    }
                }
            }
            if (existing.getFavoriteFighterId() != null) {
                int favId = existing.getFavoriteFighterId();
                cbFighter.getItems().stream()
                        .filter(f -> f != null && f.getId() == favId)
                        .findFirst().ifPresent(cbFighter::setValue);
            }
        }

        preferencesForm.setVisible(false);
        preferencesForm.setManaged(false);

        loadNotifications();
    }

    private void loadNotifications() {
        int unread = notifService.getUnreadCount(fanId);
        lblUnreadCount.setText(unread > 0 ? unread + " unread" : "All caught up!");
        btnMarkAllRead.setDisable(unread == 0);

        notificationsContainer.getChildren().clear();
        List<FanNotification> list = notifService.getNotificationsForFan(fanId);
        if (list.isEmpty()) {
            Label empty = new Label("No notifications yet.");
            empty.getStyleClass().add("empty-state");
            notificationsContainer.getChildren().add(empty);
            return;
        }
        for (FanNotification n : list) {
            notificationsContainer.getChildren().add(buildNotifCard(n));
        }
    }

    private VBox buildNotifCard(FanNotification n) {
        VBox card = new VBox(6);
        card.getStyleClass().add(n.isRead() ? "notif-card-read" : "notif-card-unread");

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label icon = new Label(n.getTypeIcon()); icon.getStyleClass().add("notif-icon");
        Label title = new Label(n.getTitle());
        title.getStyleClass().add(n.isRead() ? "notif-title" : "notif-title-unread");
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label time = new Label(n.getTimeAgo()); time.getStyleClass().add("notif-time");
        header.getChildren().addAll(icon, title, spacer, time);

        Label msg = new Label(n.getMessage());
        msg.setWrapText(true);
        msg.getStyleClass().add("notif-message");

        card.getChildren().addAll(header, msg);
        card.setOnMouseClicked(e -> {
            notifService.markAsRead(n.getId());
            loadNotifications();
        });
        return card;
    }

    @FXML
    private void onMarkAllRead() {
        notifService.markAllAsRead(fanId);
        loadNotifications();
    }

    @FXML
    private void onSavePreferences() {
        Integer disciplineId = null;
        Object selDisc = cbDiscipline.getValue();
        if (selDisc instanceof Map) {
            disciplineId = ((Number) ((Map<?, ?>) selDisc).get("id")).intValue();
        }
        Integer fighterId = cbFighter.getValue() != null ? cbFighter.getValue().getId() : null;

        FanPreference pref = new FanPreference();
        pref.setFanId(fanId);
        pref.setFavoriteDisciplineId(disciplineId);
        pref.setFavoriteFighterId(fighterId);
        prefService.savePreference(pref);
        lblPrefResult.setText("✅ Preferences saved!");
    }
}
