package com.tictacto.tictacto;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import static com.tictacto.tictacto.ChallengePopups.server;


public class GamePageBattelshipController {


    @FXML
    private TextField challengeNameField;

    public void logout(ActionEvent e) throws IOException {
        server.closeConnection();
        Platform.exit();
    }

    public void challengePlayer(ActionEvent e) {
        String challengeName = challengeNameField.getText().toLowerCase();
        String spelType = "Battleship";
        String challengeRequest = "challenge " + challengeName + " " + spelType;
        server.sendCommand(challengeRequest);
    }



    public void changetictacto(ActionEvent e) throws IOException {
        try {
            JFXUtils.Navigate("gamepage.fxml", (Stage) ((Node) e.getSource()).getScene().getWindow());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


}
