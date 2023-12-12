package com.tictacto.tictacto;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("username.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);
        stage.setTitle("Picobello");
        stage.setScene(scene);
        stage.show();
    }
}