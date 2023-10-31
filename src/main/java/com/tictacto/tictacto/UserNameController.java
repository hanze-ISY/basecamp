package com.tictacto.tictacto;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

        if(text.isEmpty()) {
            errorMessageLabel.setText("Voer een naam in.");

        }else{
            Request connect = new Request();
            connect.connectToServer("login " + text.toLowerCase());

            Session session = Session.getInstance();
            session.setUsername(text.toLowerCase());

            Parent root = FXMLLoader.load(getClass().getResource("gamepage.fxml"));
            stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }
}
