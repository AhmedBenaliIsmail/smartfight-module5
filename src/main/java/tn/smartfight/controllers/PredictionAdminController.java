package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

public class PredictionAdminController {

    @FXML private ComboBox<Event> cbEvent;
    @FXML private ListView<MatchProposalService.MatchUp> matchList;
    @FXML private Label lblTotalPreds;
    @FXML private Label lblFighter1Pct;
    @FXML private Label lblFighter2Pct;
    @FXML private VBox methodBreakdown;
    @FXML private Button btnScore;
    @FXML private Button btnLockPredictions;
    @FXML private Label lblScoreResult;

    private final EventService eventService = new EventService();
    private final MatchProposalService matchService = new MatchProposalService();
    private final FanPredictionService predictionService = new FanPredictionService();
    private final FightResultService fightResultService = new FightResultService();
    private int selectedMatchId = -1;

    @FXML
    public void initialize() {
        cbEvent.getItems().addAll(eventService.getScheduledPublicEvents());
        cbEvent.setOnAction(e -> loadMatchesForEvent());
        btnScore.setDisable(true);
        btnScore.setOnAction(e -> onScorePredictions());
        btnLockPredictions.setOnAction(e -> onLockPredictions());
    }

    private void loadMatchesForEvent() {
        Event e = cbEvent.getValue();
        if (e == null) return;
        matchList.setItems(FXCollections.observableArrayList(
                matchService.getAcceptedMatchesByEvent(e.getId())));
        matchList.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv != null) loadPredictionStats(nv.id);
        });
    }

    private void loadPredictionStats(int matchId) {
        selectedMatchId = matchId;
        Map<String, Object> stats = predictionService.getPredictionPercentages(matchId);
        lblTotalPreds.setText(stats.getOrDefault("total", 0) + " predictions");
        lblFighter1Pct.setText(String.format("%.1f%%", (double) stats.getOrDefault("fighter1_pct", 0.0)));
        lblFighter2Pct.setText(String.format("%.1f%%", (double) stats.getOrDefault("fighter2_pct", 0.0)));

        methodBreakdown.getChildren().clear();
        for (String method : new String[]{"KO", "TKO", "SUBMISSION", "DECISION", "DRAW"}) {
            double pct = (double) stats.getOrDefault(method + "_pct", 0.0);
            Label lbl = new Label(method + ": " + String.format("%.1f%%", pct));
            lbl.setStyle("-fx-text-fill:#cccccc; -fx-font-size:13px;");
            methodBreakdown.getChildren().add(lbl);
        }

        FightResult result = fightResultService.getFightResultByMatchId(matchId);
        btnScore.setDisable(result == null);
        if (result == null) {
            lblScoreResult.setText("No fight result posted yet for this match.");
        } else {
            lblScoreResult.setText("Fight result found: " + result.getSummary());
        }
    }

    private void onScorePredictions() {
        if (selectedMatchId < 0) return;
        FightResult result = fightResultService.getFightResultByMatchId(selectedMatchId);
        if (result == null) {
            lblScoreResult.setText("No fight result posted yet.");
            return;
        }
        int scored = predictionService.scorePredictionsForFight(selectedMatchId, result.getId());
        lblScoreResult.setText("✅ Scored " + scored + " predictions successfully!");
    }

    private void onLockPredictions() {
        Event e = cbEvent.getValue();
        if (e == null) return;
        int count = predictionService.lockPredictionsForEvent(e.getId());
        lblScoreResult.setText("🔒 Locked " + count + " predictions for event.");
    }
}
