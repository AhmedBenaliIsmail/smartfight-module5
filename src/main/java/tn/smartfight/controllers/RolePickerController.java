package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.session.SessionManager;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.text.Font;
import java.io.IOException;

public class RolePickerController {

    @FXML private VBox  root;
    @FXML private HBox  splitBox;
    @FXML private VBox  btnAdmin;
    @FXML private VBox  btnFan;
    @FXML private Region divider;
    @FXML private Label lblBrandA;
    @FXML private Label lblBrandB;
    @FXML private Label lblAdmin;
    @FXML private Label lblFan;
    @FXML private Label lblAdminDesc;
    @FXML private Label lblFanDesc;
    @FXML private Label lblSubtitle;

    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        // Wait for scene to be attached, then start listening
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, ov, nv) -> adapt(nv.doubleValue(), newScene.getHeight()));
                newScene.heightProperty().addListener((o, ov, nv) -> adapt(newScene.getWidth(), nv.doubleValue()));
                adapt(newScene.getWidth(), newScene.getHeight());
            }
        });
    }

    private void adapt(double w, double h) {
        // --- Hero label font: scales with width, clamped 28–80px ---
        double heroSize = Math.max(28, Math.min(80, w * 0.052));
        setFontSize(lblAdmin, heroSize, true);
        setFontSize(lblFan,   heroSize, true);

        // --- Brand labels scale with width ---
        double brandSize = Math.max(16, Math.min(32, w * 0.022));
        setFontSize(lblBrandA, brandSize, true);
        setFontSize(lblBrandB, brandSize, true);

        // --- Description text: scale between 11–15px ---
        double descSize = Math.max(11, Math.min(15, w * 0.011));
        setFontSize(lblAdminDesc, descSize, false);
        setFontSize(lblFanDesc,   descSize, false);

        // --- Subtitle: hide on very narrow windows ---
        lblSubtitle.setVisible(w >= 700);
        lblSubtitle.setManaged(w >= 700);

        // --- Vertical spacing in each half scales with height ---
        double spacing = Math.max(12, Math.min(32, h * 0.03));
        btnAdmin.setSpacing(spacing);
        btnFan.setSpacing(spacing);

        // --- Narrow layout: stack halves vertically below 700px ---
        if (w < 700) {
            if (!(root.getChildren().get(1) instanceof VBox)) {
                switchToVertical();
            }
        } else {
            if (!(root.getChildren().get(1) instanceof HBox)) {
                switchToHorizontal();
            }
        }
    }

    private void switchToVertical() {
        VBox vSplit = new VBox(0);
        VBox.setVgrow(vSplit, Priority.ALWAYS);

        // Divider becomes horizontal
        Region hDivider = new Region();
        hDivider.setMinHeight(2);
        hDivider.setMaxHeight(2);
        hDivider.setStyle("-fx-background-color: #e8002d;");
        VBox.setVgrow(hDivider, Priority.NEVER);

        VBox.setVgrow(btnAdmin, Priority.ALWAYS);
        VBox.setVgrow(btnFan, Priority.ALWAYS);
        // Remove from HBox parent references
        splitBox.getChildren().clear();
        vSplit.getChildren().addAll(btnAdmin, hDivider, btnFan);
        root.getChildren().set(1, vSplit);
    }

    private void switchToHorizontal() {
        HBox.setHgrow(btnAdmin, Priority.ALWAYS);
        HBox.setHgrow(btnFan,   Priority.ALWAYS);
        splitBox.getChildren().clear();
        splitBox.getChildren().addAll(btnAdmin, divider, btnFan);
        root.getChildren().set(1, splitBox);
    }

    private void setFontSize(Label lbl, double size, boolean bold) {
        lbl.setStyle(lbl.getStyle()
            .replaceAll("-fx-font-size:\\s*[\\d.]+px;?", "")
            .replaceAll("-fx-font-weight:[^;]+;?", "")
            + "-fx-font-size:" + size + "px;"
            + (bold ? "-fx-font-weight:bold;" : ""));
    }

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
