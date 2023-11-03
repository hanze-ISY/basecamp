package com.tictacto.tictacto;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public interface JFXUtils {
    static void ShowChallengeNotification() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JFXUtils.class.getResource("popup.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());

        // paint the background #0e65a3
        scene.setFill(javafx.scene.paint.Color.valueOf("#0e65a3"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("A new foe has appeared!");
        stage.setScene(scene);
        TranslateTransition shakeTransition = new TranslateTransition(Duration.millis(50), stage.getScene().getRoot());
        shakeTransition.setByX(10);
        shakeTransition.setCycleCount(5);
        shakeTransition.setAutoReverse(true);
        stage.show();
        shakeTransition.play();
    }
    static void Navigate(String fxml, Stage current) throws IOException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(JFXUtils.class.getResource(fxml));
                    Scene scene = new Scene(fxmlLoader.load());
                    current.setScene(scene);
                    current.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
