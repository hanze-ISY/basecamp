package com.tictacto.tictacto;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class GameChoiceController {

    @FXML
    public void handleTicTacToeButton(ActionEvent event) throws IOException {
        // Load the new FXML file (gamepage.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gamepage.fxml"));
        Parent gamePage = loader.load();

        // Get the stage (window) from the button's event source
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // Set the new scene on the stage
        Scene scene = new Scene(gamePage);
        stage.setScene(scene);
    }
}
