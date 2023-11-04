package com.tictacto.tictacto;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    }

    private void startDataFetchingTask() {
        server.AddEventListener(event -> {
            String data = event.getData();
            if (data.startsWith("PLAYERLIST")) {
                // return data is a JSONified array with strings, denoting usernames
                // substring(11) removes the "PLAYERLIST " part
                // e.g. PLAYERLIST ["user1", "user2", "user3"] -> ["user1", "user2", "user3"]
                String[] players = data
                        .substring(11)
                        .replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                        .split(", ");
                // TODO: update the UI with the new playerlist
                Platform.runLater(() -> dataLabel.setText(Arrays.toString(players)));
            }
//            if (data.startsWith("GAME YOURTURN")) {
//                // its our turn!
//                currentState = GameState.YOUR_TURN;
//            }
            if (data.startsWith("GAME MATCH")) {
                // the game has started!
                currentState = data.contains("PLAYERTOMOVE: \"" + Session.getInstance().getUsername() + "\"") ? GameState.YOUR_TURN : GameState.OPPONENTS_TURN;
                Platform.runLater(this::resetAllTiles);
            }
            if (data.startsWith("GAME MOVE")) {
                // the opponent has made a move.
                // a move looks like this: GAME MOVE {PLAYER: "jemola", MOVE: "0", DETAILS: ""}
                // substring(10) removes the "GAME MOVE " part
                // e.g. GAME MOVE {PLAYER: "jemola", MOVE: "0", DETAILS: ""} -> {PLAYER: "jemola", MOVE: "0", DETAILS: ""}
                // the arguments are not necessarily in this order, so a map is used to store them in whichever order they come in
                // be careful with the "DETAILS" argument, it can be empty
                Map<String, String> move = Arrays
                        .stream(data
                                .substring(10)
                                .replace("{", "")
                                .replace("}", "")
                                .replace("\"", "")
                                .split(", ")
                        ).collect(() -> {
                            Map<String, String> map = new java.util.HashMap<>();
                            map.put("PLAYER", "");
                            map.put("MOVE", "");
                            map.put("DETAILS", "");
                            return map;
                        }, (map, s) -> {
                            String[] split = s.split(": ");
                            if (split.length == 1) {
                                map.put(split[0], "");
                            } else {
                                map.put(split[0], split[1]);
                            }
                        }, Map::putAll);
                Platform.runLater(() -> changeTile((Pane) state.getScene().lookup("#" + move.get("MOVE"))));
                currentState = Objects.equals(Session.getInstance().getUsername(), move.get("PLAYER")) ? GameState.YOUR_TURN : GameState.OPPONENTS_TURN;
            }
            if (data.startsWith("GAME LOSS")) {
                currentState = GameState.YOU_LOST;
            }
            if (data.startsWith("GAME WIN")) {
                currentState = GameState.YOU_WON;
            }
            if (data.startsWith("GAME DRAW")) {
                currentState = GameState.DRAW;
            }
            Platform.runLater(this::updateStateHeader);
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
        changeTile(e);
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
