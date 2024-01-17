package nl.picobello.basecamp.battleship;

import javafx.application.Platform;
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
import javafx.stage.Stage;
import nl.picobello.basecamp.shared.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class BattleShipController {
    private final Server server = Server.getInstance();

    @FXML
    public Text state;
    @FXML
    private Label dataLabel;
    @FXML
    private Text userLabel;

    @FXML
    private TextField challengeNameField;
    @FXML
    private TextField debugCommand;
    private GameState currentState = GameState.WAITING_FOR_OPPONENT;

    private BattleshipBoard gameBoard = new BattleshipBoard(server, Session.getInstance().getUsername());

    public BattleShipController() {
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
        //System.out.println(gameBoard.getPlayerName()); //debug

    }

    private void startDataFetchingTask() {
        server.addEventListener(ServerEvents.PLAYER_LIST, event -> {
            HashMap<String, String> data = event.getData();
            // TODO: update the player list
            Platform.runLater(() -> dataLabel.setText(Arrays.toString(data.get("LIST").split(" "))));
        });
        server.addEventListener(ServerEvents.LOSE, event -> {
            currentState = GameState.YOU_LOST;
            Platform.runLater(this::updateStateHeader);
        });
        server.addEventListener(ServerEvents.WIN, event -> {
            currentState = GameState.YOU_WON;
            Platform.runLater(this::updateStateHeader);
        });
        server.addEventListener(ServerEvents.MOVE, event -> {
            HashMap<String, String> data = event.getData();
            //Platform.runLater(() -> changeTile((Pane) state.getScene().lookup("#" + data.get("MOVE"))));
            //ALLEMAAL VOOR AI
            int move = Integer.parseInt(data.get("MOVE"));
            int length = 10;
            if(data.get("LENGTH") != null) {
                length = Integer.parseInt(data.get("LENGTH"));
            }
            gameBoard.updateBoards(move, data.get("PLAYER"), data.get("RESULT"), length);
            System.out.println("Board: \n" + gameBoard);
            System.out.println("Opponent Board: \n" + gameBoard.oppToString());
            currentState = Objects.equals(Session.getInstance().getUsername(), data.get("PLAYER")) ? GameState.YOUR_TURN : GameState.OPPONENTS_TURN;
            Platform.runLater(this::updateStateHeader);
        });
        server.addEventListener(ServerEvents.YOUR_TURN, event -> {
            currentState = GameState.YOUR_TURN;
            if(!gameBoard.shipsPlaced()) { //Voor nu alleen AI
                gameBoard.aiPlaceShips();
                System.out.println(gameBoard);
            } else {
                int move = gameBoard.aiMove();
                server.sendCommand("move " + move);
            }
            Platform.runLater(this::updateStateHeader);
        });
        server.addEventListener(ServerEvents.DRAW, event -> {
            currentState = GameState.DRAW;
            Platform.runLater(this::updateStateHeader);
        });
        server.addEventListener(ServerEvents.NEW_MATCH, event -> {
            HashMap<String, String> data = event.getData();
            currentState =
                    data.get("PLAYERTOMOVE").equals(Session.getInstance().getUsername())
                            ? GameState.YOUR_TURN
                            : GameState.OPPONENTS_TURN;
            Platform.runLater(this::updateStateHeader);
            gameBoard.resetBoard();//bord resetten
            //Platform.runLater(this::resetAllTiles);
        });

        Thread playerTask = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        playerTask.setDaemon(true);
        playerTask.start();
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
        server.sendCommand("move " + pane.getId());
    }

    public void challengePlayer(ActionEvent e) {
        String challengeName = challengeNameField.getText().toLowerCase();
        String spelType = "Battleship";
        String challengeRequest = "challenge " + challengeName + " " + spelType;
        server.sendCommand(challengeRequest);
    }

    public void debugSendCommand(ActionEvent e) {
        String command = debugCommand.getText();
        System.out.println(command);
        server.sendCommand(command);
    }

    public void logout(ActionEvent e) throws IOException {
        Platform.exit();
    }

    public void switchGame(ActionEvent e) throws IOException {
        JFXUtils.Navigate(this.getClass().getResource("gamechoice.fxml"), (Stage) ((Node) e.getSource()).getScene().getWindow());
    }
}