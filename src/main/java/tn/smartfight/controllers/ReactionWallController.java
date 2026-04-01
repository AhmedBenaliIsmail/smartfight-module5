package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class ReactionWallController {

    @FXML private ComboBox<FightResult> cbFight;
    @FXML private Label lblFightSummary;
    @FXML private Label lblFireCount;
    @FXML private Label lblShockCount;
    @FXML private Label lblRespectCount;
    @FXML private Label lblDominantCount;
    @FXML private Label lblControversialCount;
    @FXML private VBox reactionForm;
    @FXML private ToggleButton btnFire;
    @FXML private ToggleButton btnShock;
    @FXML private ToggleButton btnRespect;
    @FXML private ToggleButton btnDominant;
    @FXML private ToggleButton btnControversial;
    @FXML private TextField txtComment;
    @FXML private Label lblCharCount;
    @FXML private Label lblError;
    @FXML private Label lblMyReaction;
    @FXML private VBox reactionsContainer;

    private ToggleGroup reactionToggleGroup;
    private final FightResultService fightResultService = new FightResultService();
    private final FanReactionService reactionService = new FanReactionService();
    private int fanId;
    private int selectedFightId = -1;

    @FXML
    public void initialize() {
        fanId = SessionManager.getCurrentUserId();

        reactionToggleGroup = new ToggleGroup();
        btnFire.setToggleGroup(reactionToggleGroup);     btnFire.setUserData("FIRE");
        btnShock.setToggleGroup(reactionToggleGroup);    btnShock.setUserData("SHOCK");
        btnRespect.setToggleGroup(reactionToggleGroup);  btnRespect.setUserData("RESPECT");
        btnDominant.setToggleGroup(reactionToggleGroup); btnDominant.setUserData("DOMINANT");
        btnControversial.setToggleGroup(reactionToggleGroup); btnControversial.setUserData("CONTROVERSIAL");

        cbFight.setOnAction(e -> loadWall());
        txtComment.textProperty().addListener((obs, ov, nv) -> {
            lblCharCount.setText(nv.length() + "/140");
            if (nv.length() > 140) lblError.setText("Comment too long");
            else lblError.setText("");
        });

        List<FightResult> fights = fightResultService.getCompletedFightResults();
        cbFight.getItems().addAll(fights);
        if (!fights.isEmpty()) {
            cbFight.getSelectionModel().selectFirst();
            loadWall();
        }
    }

    private void loadWall() {
        FightResult fight = cbFight.getValue();
        if (fight == null) return;
        selectedFightId = fight.getId();
        lblFightSummary.setText(fight.getSummary());

        Map<String, Integer> counts = reactionService.getReactionCounts(selectedFightId);
        lblFireCount.setText(String.valueOf(counts.getOrDefault("FIRE", 0)));
        lblShockCount.setText(String.valueOf(counts.getOrDefault("SHOCK", 0)));
        lblRespectCount.setText(String.valueOf(counts.getOrDefault("RESPECT", 0)));
        lblDominantCount.setText(String.valueOf(counts.getOrDefault("DOMINANT", 0)));
        lblControversialCount.setText(String.valueOf(counts.getOrDefault("CONTROVERSIAL", 0)));

        boolean hasReacted = reactionService.hasReacted(fanId, selectedFightId);
        if (hasReacted) {
            reactionForm.setVisible(false); reactionForm.setManaged(false);
            FanReaction mine = reactionService.getFanReaction(fanId, selectedFightId);
            if (mine != null) {
                lblMyReaction.setText("Your reaction: " + mine.getEmoji() +
                        (mine.getComment() != null && !mine.getComment().isEmpty() ? " — " + mine.getComment() : ""));
            }
            lblMyReaction.setVisible(true); lblMyReaction.setManaged(true);
        } else {
            reactionForm.setVisible(true); reactionForm.setManaged(true);
            lblMyReaction.setVisible(false); lblMyReaction.setManaged(false);
        }
        buildReactionWall();
    }

    private void buildReactionWall() {
        reactionsContainer.getChildren().clear();
        List<FanReaction> reactions = reactionService.getReactionsForFight(selectedFightId);
        if (reactions.isEmpty()) {
            Label empty = new Label("No reactions yet. Be the first!");
            empty.getStyleClass().add("empty-state");
            reactionsContainer.getChildren().add(empty);
            return;
        }
        for (FanReaction r : reactions) {
            reactionsContainer.getChildren().add(buildReactionCard(r));
        }
    }

    private VBox buildReactionCard(FanReaction r) {
        VBox card = new VBox(6);
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
            comment.getStyleClass().add("reaction-comment");
            comment.setWrapText(true);
            card.getChildren().add(comment);
        }
        if (r.isPinned()) {
            Label pin = new Label("📌 Pinned by admin");
            pin.setStyle("-fx-text-fill:#c9a227; -fx-font-size:11px;");
            card.getChildren().add(pin);
        }
        return card;
    }

    @FXML
    private void onPostReaction() {
        if (reactionToggleGroup.getSelectedToggle() == null) {
            lblError.setText("Please select a reaction type.");
            return;
        }
        if (txtComment.getText().length() > 140) {
            lblError.setText("Comment max 140 characters.");
            return;
        }
        lblError.setText("");
        String type = (String) reactionToggleGroup.getSelectedToggle().getUserData();
        FanReaction r = new FanReaction();
        r.setFightResultId(selectedFightId);
        r.setFanId(fanId);
        r.setReactionType(type);
        String comment = txtComment.getText().trim();
        r.setComment(comment.isEmpty() ? null : comment);
        reactionService.submitReaction(r);
        txtComment.clear();
        reactionToggleGroup.selectToggle(null);
        loadWall();
    }
}
