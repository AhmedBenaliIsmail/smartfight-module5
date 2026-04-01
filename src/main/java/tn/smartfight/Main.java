package tn.smartfight;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import tn.smartfight.services.MediaSeeder;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        MediaSeeder.seed();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/smartfight/views/RolePickerView.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);
        stage.setTitle("SmartFight — Module 5");
        stage.setMinWidth(1024);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void styleAlertDialog(Alert alert) {
        if (alert != null && alert.getDialogPane() != null) {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                Objects.requireNonNull(Main.class.getResource("/tn/smartfight/styles/smartfight.css")).toExternalForm()
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
