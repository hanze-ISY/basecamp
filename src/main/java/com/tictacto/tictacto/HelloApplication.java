package com.tictacto.tictacto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;

public class HelloApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("username.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        BackgroundImage myBI= new BackgroundImage(
                new Image(getClass().getResource("images/background.gif").toURI().toString()),
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
                    BorderPane root = (BorderPane) scene.getRoot().lookup("#root");
                    root.setBackground(new Background(myBI));

        stage.setTitle("Picobello B.V.");
        Image ico = new Image(getClass().getResource("images/taskbarlogo.png").toURI().toString());
        stage.getIcons().add(ico);



        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
}}