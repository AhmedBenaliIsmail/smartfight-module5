package tn.smartfight.controllers;

import tn.smartfight.models.FanReaction;
import tn.smartfight.services.FanReactionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.smartfight.Main;

import java.util.List;
import java.util.Map;

public class ReactionAdminController {

    @FXML private ListView<Map<String, Object>> fightList;
    @FXML private Label lblFightTitle;
    @FXML private Label lblTotalReactions;
    @FXML private Label lblPinResult;
    @FXML private VBox reactionStats;
    @FXML private VBox reactionsContainer;

    private final FanReactionService reactionService = new FanReactionService();
    private int selectedFightId = -1;

    @FXML
    public void initialize() {
        fightList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String red = (String) item.get("red_name");
                String blue = (String) item.get("blue_name");
                Object cnt = item.get("reaction_count");
                setText(red + " vs " + blue + " (" + cnt + " reactions)");
            }
        });

        fightList.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv != null) loadFightReactions(nv);
        });

        loadFightList();
    }

    private void loadFightList() {
        fightList.setItems(FXCollections.observableArrayList(
                reactionService.getFightsWithReactionCounts()));
    }

    private void loadFightReactions(Map<String, Object> selected) {
        selectedFightId = ((Number) selected.get("id")).intValue();
        lblFightTitle.setText(selected.get("red_name") + " vs " + selected.get("blue_name"));
        lblTotalReactions.setText(selected.get("reaction_count") + " reactions");
        lblPinResult.setText("");

        Map<String, Integer> counts = reactionService.getReactionCounts(selectedFightId);
        reactionStats.getChildren().clear();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Label lbl = new Label(getEmoji(entry.getKey()) + " " + entry.getKey() + ": " + entry.getValue());
            lbl.setStyle("-fx-text-fill:#cccccc; -fx-font-size:13px;");
            reactionStats.getChildren().add(lbl);
        }

        buildAdminWall();
    }

    private void buildAdminWall() {
        reactionsContainer.getChildren().clear();
        List<FanReaction> reactions = reactionService.getReactionsForFight(selectedFightId);
        if (reactions.isEmpty()) {
            Label e = new Label("No reactions for this fight.");
            e.getStyleClass().add("empty-state");
            reactionsContainer.getChildren().add(e);
            return;
        }
        for (FanReaction r : reactions) {
            VBox card = new VBox(8);
            card.getStyleClass().add("reaction-card");
            if (r.isPinned()) card.getStyleClass().add("reaction-pinned");

            HBox header = new HBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label emoji = new Label(r.getEmoji()); emoji.getStyleClass().add("reaction-emoji");
            Label name = new Label(r.getFanName()); name.getStyleClass().add("reaction-fan");
            Label time = new Label(r.getTimeAgo()); time.getStyleClass().add("reaction-time");
            HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().addAll(emoji, name, spacer, time);
            card.getChildren().add(header);

            if (r.getComment() != null && !r.getComment().isEmpty()) {
                Label comment = new Label(r.getComment());
                comment.setStyle("-fx-text-fill:#cccccc; -fx-font-size:13px;");
                comment.setWrapText(true);
                card.getChildren().add(comment);
            }

            if (r.isPinned()) {
                Label pin = new Label("📌 Pinned");
                pin.setStyle("-fx-text-fill:#c9a227; -fx-font-size:11px;");
                card.getChildren().add(pin);
            }

            HBox actions = new HBox(8);
            Button btnPin = new Button("📌 Pin");
            btnPin.getStyleClass().add("btn-secondary");
            btnPin.setOnAction(e -> {
                reactionService.pinReaction(r.getId());
                loadFightList();
                Map<String, Object> sel = fightList.getSelectionModel().getSelectedItem();
                if (sel != null) loadFightReactions(sel);
                lblPinResult.setText("Reaction pinned!");
            });

            Button btnDel = new Button("🗑 Delete");
            btnDel.getStyleClass().add("btn-danger");
            btnDel.setOnAction(e -> {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete this reaction?",
                        ButtonType.YES, ButtonType.NO);
                Main.styleAlertDialog(a);
                a.showAndWait().ifPresent(bt -> {
                    if (bt == ButtonType.YES) {
                        reactionService.deleteReaction(r.getId());
                        loadFightList();
                        Map<String, Object> sel = fightList.getSelectionModel().getSelectedItem();
                        if (sel != null) loadFightReactions(sel);
                    }
                });
            });
            actions.getChildren().addAll(btnPin, btnDel);
            card.getChildren().add(actions);
            reactionsContainer.getChildren().add(card);
        }
    }

    private String getEmoji(String type) {
        return switch (type) {
            case "FIRE" -> "🔥";
            case "SHOCK" -> "😱";
            case "RESPECT" -> "👏";
            case "DOMINANT" -> "💪";
            case "CONTROVERSIAL" -> "🤔";
            default -> "💬";
        };
    }
}
