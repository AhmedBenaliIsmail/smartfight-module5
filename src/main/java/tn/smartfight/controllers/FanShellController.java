package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.session.SessionManager;
import javafx.animation.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.stage.*;
import javafx.scene.image.*;
import javafx.util.Duration;
import java.io.IOException;
import java.util.*;

public class FanShellController {

    @FXML
    private Label lblFanName;

    @FXML
    private Label lblUnreadCount;

    @FXML
    private StackPane contentArea;

    private final FanNotificationService notifService = new FanNotificationService();

    @FXML
    private void initialize() {
        lblFanName.setText(SessionManager.getCurrentUserName());
        refreshUnreadBadge();
        navigateTo("BlogReadView.fxml");
    }

    @FXML
    private void onBlogClicked() {
        navigateTo("BlogReadView.fxml");
    }

    @FXML
    private void onBookingsClicked() {
        navigateTo("BookingView.fxml");
    }

    @FXML
    private void onPredictionsClicked() {
        navigateTo("PredictionView.fxml");
    }

    @FXML
    private void onReactionsClicked() {
        navigateTo("ReactionWallView.fxml");
    }

    @FXML
    private void onNotificationsClicked() {
        navigateTo("NotificationView.fxml");
        refreshUnreadBadge();
    }

    @FXML
    private void onSwitchRoleClicked() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/smartfight/views/RolePickerView.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshUnreadBadge() {
        int count = notifService.getUnreadCount(SessionManager.getCurrentUserId());
        lblUnreadCount.setText(count > 0 ? String.valueOf(count) : "");
        lblUnreadCount.setVisible(count > 0);
        lblUnreadCount.setManaged(count > 0);
    }

    private void navigateTo(String viewName) {
        try {
            Parent view = FXMLLoader.load(
                getClass().getResource("/tn/smartfight/views/" + viewName));
            view.setTranslateX(24);
            view.setOpacity(0);
            contentArea.getChildren().setAll(view);

            FadeTransition fade = new FadeTransition(Duration.millis(200), view);
            fade.setFromValue(0);
            fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(200), view);
            slide.setFromX(24);
            slide.setToX(0);

            new ParallelTransition(fade, slide).play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
