package com.tictacto.tictacto;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

enum GameState {
    WAITING_FOR_OPPONENT,
    YOUR_TURN,
    OPPONENTS_TURN,
    YOU_WON,
    YOU_LOST,
    DRAW
}

public class GamePageController {
    private final Server server = Server.getInstance();
    @FXML
    public Text state;
    public VBox chatbox;

    @FXML
    private Label dataLabel;
    @FXML
    private Text userLabel;
    @FXML
    private TextField challengeNameField;
    private GameState currentState = GameState.WAITING_FOR_OPPONENT;

    public GamePageController() {
        startDataFetchingTask();
        ChallengePopups.startListening();
    }

    private void updateStateHeader() {
        switch (currentState) {
            case WAITING_FOR_OPPONENT:
                state.setText("Op tegenstander wachten...");
                break;
            case YOUR_TURN:
                state.setText("Het is jouw beurt!");
                break;
            case OPPONENTS_TURN:
                state.setText("Tegenstander is aan de beurt...");
                break;
            case YOU_WON:
                state.setText("Je hebt gewonnen!");
                break;
            case YOU_LOST:
                state.setText("Je hebt verloren :(");
                break;
            case DRAW:
                state.setText("Gelijkspel!");
                break;
        }
    }

    public void initialize() {
        userLabel.setText(Session.getInstance().getUsername());
        Platform.runLater(this::updateStateHeader);
    }

    private void startDataFetchingTask() {
        server.AddEventListener(ServerEvents.PLAYER_LIST, event -> {
            HashMap<String, String> data = event.getData();
            // TODO: update the player list
            Platform.runLater(() -> dataLabel.setText(Arrays.toString(data.get("LIST").split(" "))));
        });
        server.AddEventListener(ServerEvents.LOSE, event -> {
            currentState = GameState.YOU_LOST;
            Platform.runLater(this::updateStateHeader);
        });
        server.AddEventListener(ServerEvents.WIN, event -> {
            currentState = GameState.YOU_WON;
            Platform.runLater(this::updateStateHeader);
        });
        server.AddEventListener(ServerEvents.MOVE, event -> {
            HashMap<String, String> data = event.getData();
            Platform.runLater(() -> changeTile((Pane) state.getScene().lookup("#" + data.get("MOVE"))));
            currentState = Objects.equals(Session.getInstance().getUsername(), data.get("PLAYER")) ? GameState.YOUR_TURN : GameState.OPPONENTS_TURN;
            Platform.runLater(this::updateStateHeader);
        });
        server.AddEventListener(ServerEvents.YOUR_TURN, event -> {
            currentState = GameState.YOUR_TURN;
            Platform.runLater(this::updateStateHeader);
        });
        server.AddEventListener(ServerEvents.DRAW, event -> {
            currentState = GameState.DRAW;
            Platform.runLater(this::updateStateHeader);
        });
        server.AddEventListener(ServerEvents.NEW_MATCH, event -> {
            HashMap<String, String> data = event.getData();
            currentState =
                    data.get("PLAYERTOMOVE").equals(Session.getInstance().getUsername())
                            ? GameState.YOUR_TURN
                            : GameState.OPPONENTS_TURN;
            Platform.runLater(this::updateStateHeader);
            Platform.runLater(this::resetAllTiles);
        });

        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    server.SendCommand("get playerlist");
                    Thread.sleep(5000);
                }
            }
        }).start();
    }

    public void ButtonClick(MouseEvent e) throws IOException {
        if (currentState != GameState.YOUR_TURN) {
            // its not our turn, so we can't do anything
            return;
        }
        System.out.println("Button clicked");
        Pane pane = (Pane) e.getSource();
        // determine what turn it is
        // if it's X's turn, add an "X" to the pane
        // if it's O's turn, add an "O" to the pane
        // if the pane already has an "X" or "O", do nothing
        if (!pane.getChildren().isEmpty()) {
            return;
        }
        server.SendCommand("move " + pane.getId());
    }

    public void challengePlayer(ActionEvent e) {
        String challengeName = challengeNameField.getText().toLowerCase();
        String spelType = "Tic-tac-toe";
        String challengeRequest = "challenge " + challengeName + " " + spelType;
        server.SendCommand(challengeRequest);
    }

    private void changeTile(MouseEvent e) {
        Pane pane = (Pane) e.getSource();
        // add "X" or "O" to the pane, the label should be the same size as the pane, and the text should be as big
        // as possible, and centered
        Label label = new Label(currentState == GameState.YOUR_TURN ? "X" : "O");
        currentState = currentState == GameState.YOUR_TURN ? GameState.OPPONENTS_TURN : GameState.YOUR_TURN;
        label.setPrefSize(pane.getWidth(), pane.getHeight());
        label.setTextFill(Color.WHITE);
        label.getFont();
        label.setFont(Font.font(pane.getHeight() / 2));
        label.setAlignment(javafx.geometry.Pos.CENTER);
        pane.getChildren().add(label);
    }

    private void changeTile(Pane pane) {
        // add "X" or "O" to the pane, the label should be the same size as the pane, and the text should be as big
        // as possible, and centered
        Label label = new Label(currentState == GameState.YOUR_TURN ? "X" : "O");
        currentState = currentState == GameState.YOUR_TURN ? GameState.OPPONENTS_TURN : GameState.YOUR_TURN;
        label.setPrefSize(pane.getWidth(), pane.getHeight());
        label.setTextFill(Color.WHITE);
        label.getFont();
        label.setFont(Font.font(pane.getHeight() / 2));
        label.setAlignment(javafx.geometry.Pos.CENTER);
        pane.getChildren().add(label);
    }

    private void resetAllTiles() {
        // remove all labels from all panes
        Set<Node> nodes = state.getScene().lookup("#gameboard").lookupAll(".grid-panes");
        System.out.println("Trying to clear tiles: " + nodes.size());
        for (Node node : nodes) {
            Pane pane = (Pane) node;
            pane.getChildren().clear();
        }
    }

    public void logout(ActionEvent e) throws IOException {
        server.CloseConnection();
        Platform.exit();
    }
}
