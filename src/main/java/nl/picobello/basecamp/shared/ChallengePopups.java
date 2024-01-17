package nl.picobello.basecamp.shared;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Map;

public class ChallengePopups {
    private static boolean running = false;
    static final Server server = Server.getInstance();
    private static Map<String, String> challenge;
    @FXML
    private Text playername;

    public void initialize() {
        if (playername == null) {
            throw new RuntimeException("playername is null");
        }
    }
    public static void startListening() {
        if (!running) {
            running = true;
            server.addEventListener(ServerEvents.CHALLENGE, event -> {
                challenge = event.getData();
                Platform.runLater(() -> {
                    try {
                        JFXUtils.ShowChallengeNotification();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }
    }

    public void denyChallenge(MouseEvent mouseEvent) {
        // String id = challenge.get("CHALLENGENUMBER");
        // server.SendCommand("challenge deny " + id);
        // close the modal
        Scene scene = playername.getScene();
        scene.getWindow().hide();
    }

    public void acceptChallenge(MouseEvent mouseEvent) {
        String id = challenge.get("CHALLENGENUMBER");
        server.sendCommand("challenge accept " + id);
        // close the modal
        Scene scene = playername.getScene();
        scene.getWindow().hide();
    }
}
