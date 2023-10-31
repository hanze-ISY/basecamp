package com.tictacto.tictacto;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class GamePageController {
    @FXML
    private Label dataLabel;

    public void initialize() {
        startDataFetchingTask();
    }

    private void startDataFetchingTask() {
        Playerlist connect = new Playerlist();

        Task<Void> dataFetchingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    String data = connect.fetchList();
                    Platform.runLater(() -> dataLabel.setText(data));
                    Thread.sleep(5000);
                }
            }
        };
        Thread thread = new Thread(dataFetchingTask);
        thread.setDaemon(true);
        thread.start();
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
