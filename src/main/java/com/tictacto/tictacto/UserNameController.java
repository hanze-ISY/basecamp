package com.tictacto.tictacto;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class UserNameController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField textLogin;
    @FXML
    private Label errorMessageLabel;

    public void login(ActionEvent e) throws IOException {

        String text = textLogin.getText();
        Server server = Server.getInstance();
        DataEventListener handler = new DataEventListener() {
            @Override
            public void data(DataEvent event) {
                if (event.getData().equals("OK")) {
                    Session session = Session.getInstance();
                    session.setUsername(text.toLowerCase());

                    try {
                        JFXUtils.Navigate("gamepage.fxml", (Stage) ((Node) e.getSource()).getScene().getWindow());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    Platform.runLater(() -> server.RemoveEventListener(this));
                }
            }

            @Override
            public void error(DataEvent event) {
                errorMessageLabel.setText(event.getData());
            }
        };

        if (text.isEmpty()) {
            errorMessageLabel.setText("Voer een naam in.");
        } else {
            server.AddEventListener(handler);
            server.SendCommand("login " + '"' + text.toLowerCase() + '"');
        }
    }
}
