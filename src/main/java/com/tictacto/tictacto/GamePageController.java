package com.tictacto.tictacto;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.Arrays;

public class GamePageController {
    @FXML
    private Label dataLabel;
    private boolean turn = true;

    public void initialize() {
        startDataFetchingTask();
    }

    private void startDataFetchingTask() {
        Playerlist connect = new Playerlist();
         Server server = Server.getInstance();

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
        });

        Task<Void> dataFetchingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    server.SendCommand("get playerlist");
                    Thread.sleep(5000);
                }
            }
        };
        Thread thread = new Thread(dataFetchingTask);
        thread.setDaemon(true);
        thread.start();
    }

    public void ButtonClick(MouseEvent e) {
        System.out.println("Button clicked");
        Pane pane = (Pane) e.getSource();
        // determine what turn it is
        // if it's X's turn, add an "X" to the pane
        // if it's O's turn, add an "O" to the pane
        // if the pane already has an "X" or "O", do nothing
        if (pane.getChildren().size() > 0) {
            return;
        }

        // add "X" or "O" to the pane, the label should be the same size as the pane, and the text should be as big
        // as possible, and centered
        Label label = new Label(turn ? "X" : "O");
        turn = !turn;
        label.setPrefSize(pane.getWidth(), pane.getHeight());
        label.setTextFill(Color.WHITE);
        label.setFont(label.getFont().font(pane.getHeight() / 2));
        label.setAlignment(javafx.geometry.Pos.CENTER);
        pane.getChildren().add(label);
    }

    public void A(ActionEvent e) {
        System.out.println("A");
    }

    public void B(ActionEvent e) {
        System.out.println("B");
    }

    public void C(ActionEvent e) {
        System.out.println("C");
    }

    public void D(ActionEvent e) {
        System.out.println("D");
    }

    public void logout(ActionEvent e) throws IOException {
        Request connect = new Request();
        connect.connectToServer("logout");
        Platform.exit();
    }

    public void account(ActionEvent e) {
        System.out.println("account");
    }
}
