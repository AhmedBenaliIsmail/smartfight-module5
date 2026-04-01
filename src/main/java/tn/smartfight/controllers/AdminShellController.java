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

public class AdminShellController {

    @FXML
    private Label lblAdminName;

    @FXML
    private StackPane contentArea;

    @FXML
    private void initialize() {
        lblAdminName.setText(SessionManager.getCurrentUserName());
        navigateTo("BlogAdminView.fxml");
    }

    @FXML
    private void onBlogClicked() {
        navigateTo("BlogAdminView.fxml");
    }

    @FXML
    private void onBookingsClicked() {
        navigateTo("BookingAdminView.fxml");
    }

    @FXML
    private void onPredictionsClicked() {
        navigateTo("PredictionAdminView.fxml");
    }

    @FXML
    private void onReactionsClicked() {
        navigateTo("ReactionAdminView.fxml");
    }

    @FXML
    private void onNotificationsClicked() {
        navigateTo("NotificationAdminView.fxml");
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
