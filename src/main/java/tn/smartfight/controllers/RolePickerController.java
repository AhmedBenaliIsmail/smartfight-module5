package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.session.SessionManager;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.stage.*;
import javafx.scene.image.*;
import java.io.IOException;
import java.util.*;

public class RolePickerController {

    @FXML
    private VBox btnAdmin;

    private final UserService userService = new UserService();

    @FXML
    private void onAdminClicked() {
        User admin = userService.getUserById(1);
        SessionManager.loginAsAdmin(admin);
        loadShell("/tn/smartfight/views/AdminShellView.fxml");
    }

    @FXML
    private void onFanClicked() {
        User fan = userService.getUserById(6);
        SessionManager.loginAsFan(fan);
        loadShell("/tn/smartfight/views/FanShellView.fxml");
    }

    private void loadShell(String fxmlPath) {
        try {
            Parent shell = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) btnAdmin.getScene().getWindow();
            stage.getScene().setRoot(shell);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
