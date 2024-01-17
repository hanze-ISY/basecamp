package nl.picobello.basecamp.shared;

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
import nl.picobello.basecamp.shared.JFXUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class GameChoiceController {

    public void ticTacToe(ActionEvent e) throws IOException {
        JFXUtils.Navigate(this.getClass().getResource("/tictactoe.fxml"), (Stage) ((Node) e.getSource()).getScene().getWindow());
    }

    public void battleShip(ActionEvent e) throws IOException {
        JFXUtils.Navigate(this.getClass().getResource("battleShip.fxml"), (Stage) ((Node) e.getSource()).getScene().getWindow());
    }
}
