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
    YOU_LOST
}
public class BattleshipController {
    private final Server server = Server.getInstance();
    private final BattleshipBoard battleshipBoard = new BattleshipBoard(server, Session.getInstance().getUsername());
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

    public BattleshipController() {
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
                battleshipBoard.resetBoard();
                //Have to start sequence for placing ships
            }
            if (data.startsWith("GAME LOSS")) {
                currentState = GameState.YOU_LOST;
            }
            if (data.startsWith("GAME WIN")) {
                currentState = GameState.YOU_WON;
            }
            //update visual trackers of boards with either hit or miss
            if (data.startsWith("SVR GAME MOVE")) {
                String[] parts = data.split("RESULT: \"");
                if (parts.length >= 2) {
                    String result = parts[1].split("\"")[0];
                    int index = extractIndexFromData(data);
                    String playerName = extractPlayerNameFromData(data);
                    // Update boards based on the result
                    //battleshipBoard.updateBoards(index, playerName, result);

                    currentState = Objects.equals(Session.getInstance().getUsername(), playerName) ? GameState.OPPONENTS_TURN : GameState.YOUR_TURN;
                }
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

    private int extractIndexFromData(String data) {
        String[] moveParts = data.split("MOVE: ");
        if (moveParts.length >= 2) {
            String indexString = moveParts[1].split(",")[0].trim(); // Assuming the index is followed by a comma
            return Integer.parseInt(indexString);
        }
        return -1; // Return a default value or handle the case when index extraction fails
    }

    private String extractPlayerNameFromData(String data) {
        String[] playerParts = data.split("PLAYER: ");
        if (playerParts.length >= 2) {
            String playerName = playerParts[1].split(",")[0].trim(); // Assuming the player name is followed by a comma
            return playerName;
        }
        return "Unknown"; // Return a default value or handle the case when player name extraction fails
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
        //changeTile(e);
    }

    public void challengePlayer(ActionEvent e) {
        String challengeName = challengeNameField.getText().toLowerCase();
        String spelType = "Tic-tac-toe";
        String challengeRequest = "challenge " + challengeName + " " + spelType;
        server.SendCommand(challengeRequest);
    }

    public void logout(ActionEvent e) throws IOException {
        server.CloseConnection();
        Platform.exit();
    }
}
