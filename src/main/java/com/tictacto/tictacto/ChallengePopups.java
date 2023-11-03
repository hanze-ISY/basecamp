package com.tictacto.tictacto;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
            server.AddEventListener(event -> {
                String data = event.getData();
                if (data.startsWith("GAME CHALLENGE")) {
                    // a challenge notification consists of the following:
                    // GAME CHALLENGE { CHALLENGER: "username", GAMETYPE: "gametype", CHALLENGENUMBER: "id" }
                    // substring(15) removes the "GAME CHALLENGE " part
                    // e.g. GAME CHALLENGE { CHALLENGER: "username", GAMETYPE: "gametype", CHALLENGENUMBER: "id" } -> { CHALLENGER: "username", GAMETYPE: "gametype", CHALLENGENUMBER: "id" }
                    // the arguments are not necessarily in this order, so a map is used to store them in whichever order they come in
                    challenge = Arrays
                            .stream(data
                                    .substring(15)
                                    .replace("{", "")
                                    .replace("}", "")
                                    .replace("\"", "")
                                    .split(", ")
                            ).collect(Collectors.toMap(s -> s.split(": ")[0], s -> s.split(": ")[1]));
                    // playername.setText(challenge.get("CHALLENGER"));
                    Platform.runLater(() -> {
                        try {
                            // playername.setText(challenge.get("CHALLENGER"));
                            JFXUtils.ShowChallengeNotification();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
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
        server.SendCommand("challenge accept " + id);
        // close the modal
        Scene scene = playername.getScene();
        scene.getWindow().hide();
    }
}
