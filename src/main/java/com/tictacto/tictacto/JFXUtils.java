package com.tictacto.tictacto;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public interface JFXUtils {
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
