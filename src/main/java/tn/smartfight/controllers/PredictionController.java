package tn.smartfight.controllers;

import tn.smartfight.models.*;
import tn.smartfight.services.*;
import tn.smartfight.session.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class PredictionController {

    @FXML private ComboBox<Event> cbPredictEvent;
    @FXML private VBox matchupsContainer;
    @FXML private TableView<FanPrediction> historyTable;
    @FXML private TableColumn<FanPrediction, String> colMatch;
    @FXML private TableColumn<FanPrediction, String> colPick;
    @FXML private TableColumn<FanPrediction, String> colMethod;
    @FXML private TableColumn<FanPrediction, String> colResult;
    @FXML private TableColumn<FanPrediction, Integer> colPoints;
    @FXML private Label lblTotalPoints;
    @FXML private Label lblAccuracy;
    @FXML private ToggleButton btnSeasonBoard;
    @FXML private ToggleButton btnEventBoard;
    @FXML private ComboBox<Event> cbLeaderboardEvent;
    @FXML private TableView<LeaderboardRow> leaderboardTable;
    @FXML private TableColumn<LeaderboardRow, String> colRank;
    @FXML private TableColumn<LeaderboardRow, String> colFan;
    @FXML private TableColumn<LeaderboardRow, Integer> colCorrect;
    @FXML private TableColumn<LeaderboardRow, Integer> colTotal;
    @FXML private TableColumn<LeaderboardRow, String> colAccuracy;
    @FXML private TableColumn<LeaderboardRow, Integer> colLeaderPoints;

    private final EventService eventService = new EventService();
    private final FanPredictionService predictionService = new FanPredictionService();
    private final MatchProposalService matchService = new MatchProposalService();
    private int fanId;

    @FXML
    public void initialize() {
        fanId = SessionManager.getCurrentUserId();
        List<Event> events = eventService.getScheduledPublicEvents();
        cbPredictEvent.getItems().addAll(events);
        cbLeaderboardEvent.getItems().addAll(events);
        cbPredictEvent.setOnAction(e -> loadMatchups());
        setupHistoryColumns();
        setupLeaderboardColumns();
        loadMyHistory();
        loadLeaderboardSeason();

        ToggleGroup boardGroup = new ToggleGroup();
        btnSeasonBoard.setToggleGroup(boardGroup);
        btnEventBoard.setToggleGroup(boardGroup);
        btnSeasonBoard.setSelected(true);
        btnSeasonBoard.setOnAction(e -> loadLeaderboardSeason());
        btnEventBoard.setOnAction(e -> loadLeaderboardByEvent());
    }

    private void loadMatchups() {
        Event e = cbPredictEvent.getValue();
        if (e == null) return;
        matchupsContainer.getChildren().clear();
        List<MatchProposalService.MatchUp> matches = matchService.getAcceptedMatchesByEvent(e.getId());
        if (matches.isEmpty()) {
            Label lbl = new Label("No accepted matches for this event yet.");
            lbl.setStyle("-fx-text-fill:#666666; -fx-font-size:14px; -fx-padding:20;");
            matchupsContainer.getChildren().add(lbl);
            return;
        }
        for (MatchProposalService.MatchUp m : matches) {
            boolean hasPredicted = predictionService.hasPredicted(fanId, m.id);
            matchupsContainer.getChildren().add(buildMatchCard(m, hasPredicted));
        }
    }

    private VBox buildMatchCard(MatchProposalService.MatchUp m, boolean hasPredicted) {
        VBox card = new VBox(10);
        card.getStyleClass().add("match-card");

        Label title = new Label(m.fighter1Name + "  VS  " + m.fighter2Name);
        title.getStyleClass().add("match-title");
        card.getChildren().add(title);

        if (hasPredicted) {
            FanPrediction existing = predictionService.getPredictionByFanAndMatch(fanId, m.id);
            String display = existing != null ? existing.getAccuracyDisplay() : "Your prediction";
            Label locked = new Label("✅ Locked: " + display);
            locked.getStyleClass().add("prediction-locked");
            card.getChildren().add(locked);
        } else {
            ComboBox<String> cbWinner = new ComboBox<>();
            cbWinner.getItems().addAll(m.fighter1Name, m.fighter2Name);
            cbWinner.setPromptText("Pick winner");
            cbWinner.getStyleClass().add("dark-combo");
            cbWinner.setMaxWidth(Double.MAX_VALUE);

            ComboBox<String> cbMethod = new ComboBox<>();
            cbMethod.getItems().addAll("KO", "TKO", "SUBMISSION", "DECISION", "DRAW");
            cbMethod.setPromptText("Predicted method");
            cbMethod.getStyleClass().add("dark-combo");
            cbMethod.setMaxWidth(Double.MAX_VALUE);

            Label lblError = new Label();
            lblError.setStyle("-fx-text-fill:#e8002d; -fx-font-size:11px;");

            Button btnLock = new Button("🔒 Lock In Prediction");
            btnLock.getStyleClass().add("btn-primary");
            btnLock.setOnAction(ev -> {
                if (cbWinner.getValue() == null || cbMethod.getValue() == null) {
                    lblError.setText("Please select a winner and method.");
                    return;
                }
                int winnerId = cbWinner.getValue().equals(m.fighter1Name) ? m.fighter1Id : m.fighter2Id;
                onSubmitPrediction(m.id, winnerId, cbMethod.getValue());
            });

            card.getChildren().addAll(cbWinner, cbMethod, btnLock, lblError);
        }
        return card;
    }

    private void onSubmitPrediction(int matchId, int winnerId, String method) {
        FanPrediction p = new FanPrediction();
        p.setMatchProposalId(matchId);
        p.setFanId(fanId);
        p.setPredictedWinnerId(winnerId);
        p.setPredictedMethod(method);
        p.setSeason("2026");
        predictionService.submitPrediction(p);
        loadMatchups();
        loadMyHistory();
    }

    private void loadMyHistory() {
        List<FanPrediction> list = predictionService.getPredictionsByFan(fanId);
        historyTable.setItems(FXCollections.observableArrayList(list));
        int totalPts = list.stream()
                .filter(p -> !p.isPending() && p.getPointsEarned() != null)
                .mapToInt(FanPrediction::getPointsEarned).sum();
        long correct = list.stream()
                .filter(p -> !p.isPending() && p.getPointsEarned() != null && p.getPointsEarned() > 0).count();
        long scored = list.stream().filter(p -> !p.isPending()).count();
        double acc = scored > 0 ? correct * 100.0 / scored : 0.0;
        lblTotalPoints.setText(totalPts + " pts");
        lblAccuracy.setText(String.format("%.1f%% accuracy", acc));
    }

    @FXML
    private void loadLeaderboardSeason() {
        List<LeaderboardRow> rows = predictionService.getLeaderboardBySeason("2026");
        for (int i = 0; i < rows.size(); i++) rows.get(i).setRank(i + 1);
        leaderboardTable.setItems(FXCollections.observableArrayList(rows));
        applyCurrentFanHighlight();
    }

    @FXML
    private void loadLeaderboardByEvent() {
        Event e = cbLeaderboardEvent.getValue();
        if (e == null) return;
        List<LeaderboardRow> rows = predictionService.getLeaderboardByEvent(e.getId());
        for (int i = 0; i < rows.size(); i++) rows.get(i).setRank(i + 1);
        leaderboardTable.setItems(FXCollections.observableArrayList(rows));
        applyCurrentFanHighlight();
    }

    private void applyCurrentFanHighlight() {
        String myEmail = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getEmail() : "";
        leaderboardTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LeaderboardRow row, boolean empty) {
                super.updateItem(row, empty);
                getStyleClass().remove("current-fan-row");
                if (!empty && row != null && row.getFanEmail().equals(myEmail)) {
                    getStyleClass().add("current-fan-row");
                }
            }
        });
    }

    private void setupHistoryColumns() {
        colMatch.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getFighter1Name() + " vs " + cd.getValue().getFighter2Name()));
        colPick.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPredictedWinnerName()));
        colMethod.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPredictedMethod()));
        colResult.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getResultLabel()));
        colPoints.setCellValueFactory(cd -> new SimpleObjectProperty<>(
                cd.getValue().getPointsEarned() != null ? cd.getValue().getPointsEarned() : 0));
    }

    private void setupLeaderboardColumns() {
        colRank.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRankBadge()));
        colFan.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFanName()));
        colCorrect.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getCorrectPredictions()));
        colTotal.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getTotalPredictions()));
        colAccuracy.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAccuracyDisplay()));
        colLeaderPoints.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getTotalPoints()));
    }
}
