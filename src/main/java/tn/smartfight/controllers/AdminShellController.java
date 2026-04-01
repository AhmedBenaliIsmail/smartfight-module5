package tn.smartfight.controllers;

import tn.smartfight.session.SessionManager;
import javafx.animation.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import java.io.IOException;

public class AdminShellController {

    @FXML private VBox    sidebar;
    @FXML private VBox    vboxBrand;
    @FXML private VBox    vboxUser;
    @FXML private Label   lblBrand;
    @FXML private Label   lblSubtitle;
    @FXML private Label   lblSignedIn;
    @FXML private Label   lblAdminName;
    @FXML private Button  btnBlog;
    @FXML private Button  btnBookings;
    @FXML private Button  btnPredictions;
    @FXML private Button  btnReactions;
    @FXML private Button  btnBroadcasts;
    @FXML private Button  btnSwitchRole;
    @FXML private Button  btnCollapse;
    @FXML private StackPane contentArea;

    private boolean collapsed = false;
    private static final double W_EXPANDED  = 256;
    private static final double W_COLLAPSED = 64;

    // [full text, collapsed icon]
    private static final String[][] NAV = {
        {"BLOG", "B"}, {"BOOKINGS", "Bk"}, {"PREDICTIONS", "P"},
        {"REACTIONS", "R"}, {"BROADCASTS", "Bc"}, {"SWITCH ROLE", "↩"}
    };

    @FXML
    private void initialize() {
        lblAdminName.setText(SessionManager.getCurrentUserName());
        navigateTo("BlogAdminView.fxml");
    }

    @FXML private void onBlogClicked()          { navigateTo("BlogAdminView.fxml"); }
    @FXML private void onBookingsClicked()      { navigateTo("BookingAdminView.fxml"); }
    @FXML private void onPredictionsClicked()   { navigateTo("PredictionAdminView.fxml"); }
    @FXML private void onReactionsClicked()     { navigateTo("ReactionAdminView.fxml"); }
    @FXML private void onNotificationsClicked() { navigateTo("NotificationAdminView.fxml"); }

    @FXML
    private void onSwitchRoleClicked() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/smartfight/views/RolePickerView.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onToggleSidebar() {
        collapsed = !collapsed;
        double target = collapsed ? W_COLLAPSED : W_EXPANDED;

        // Animate width
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(200),
            new KeyValue(sidebar.prefWidthProperty(), target, Interpolator.EASE_BOTH),
            new KeyValue(sidebar.maxWidthProperty(),  target, Interpolator.EASE_BOTH)
        ));

        if (collapsed) {
            // Hide text immediately before animating in
            setLabelsVisible(false);
            setNavTexts(NAV[0][1], NAV[1][1], NAV[2][1], NAV[3][1], NAV[4][1], NAV[5][1]);
            setNavAlignment(true);
            btnCollapse.setText("›");
        } else {
            tl.setOnFinished(e -> {
                setLabelsVisible(true);
                setNavTexts(NAV[0][0], NAV[1][0], NAV[2][0], NAV[3][0], NAV[4][0], NAV[5][0]);
                setNavAlignment(false);
            });
            btnCollapse.setText("‹");
        }

        tl.play();
    }

    private void setLabelsVisible(boolean visible) {
        for (Node n : new Node[]{lblBrand, lblSubtitle, lblSignedIn, lblAdminName}) {
            n.setVisible(visible);
            n.setManaged(visible);
        }
        vboxUser.setStyle(visible
            ? "-fx-padding: 4 16 24 20; -fx-border-color: transparent transparent #27272a transparent; -fx-border-width: 0 0 1 0;"
            : "-fx-padding: 0; -fx-border-width: 0;"
        );
    }

    private void setNavTexts(String blog, String book, String pred, String react, String broad, String sw) {
        btnBlog.setText(blog);
        btnBookings.setText(book);
        btnPredictions.setText(pred);
        btnReactions.setText(react);
        btnBroadcasts.setText(broad);
        btnSwitchRole.setText(sw);
    }

    private void setNavAlignment(boolean center) {
        String align = center ? "-fx-alignment: CENTER;" : "-fx-alignment: CENTER-LEFT;";
        for (Button b : new Button[]{btnBlog, btnBookings, btnPredictions, btnReactions, btnBroadcasts, btnSwitchRole}) {
            b.setStyle(b.getStyle().replaceAll("-fx-alignment:[^;]+;", "") + align);
        }
    }

    private void navigateTo(String viewName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/tn/smartfight/views/" + viewName));
            view.setTranslateX(24);
            view.setOpacity(0);
            contentArea.getChildren().setAll(view);

            FadeTransition fade = new FadeTransition(Duration.millis(200), view);
            fade.setFromValue(0); fade.setToValue(1);

            TranslateTransition slide = new TranslateTransition(Duration.millis(200), view);
            slide.setFromX(24); slide.setToX(0);

            new ParallelTransition(fade, slide).play();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
