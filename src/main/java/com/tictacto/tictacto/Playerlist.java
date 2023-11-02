package com.tictacto.tictacto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Playerlist {

    private final String serverAddress = "145.33.225.170";
    private final int serverPort = 7789;
    private final int timeoutMillis = 600000;

    public String fetchList() {

        try (Socket socket = new Socket(serverAddress, serverPort)) {
            socket.setSoTimeout(timeoutMillis);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println("get playerlist");

                StringBuilder responseBuilder = new StringBuilder();
                String response;
                while ((response = in.readLine()) != null) {
                    if (response.charAt(0) == 'S') {
                        responseBuilder.append(response).append('\n');
                        break;
                    }
                }
                int startIndex = responseBuilder.toString().indexOf("[");
                return responseBuilder.substring(startIndex);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Connection error: " + ex.getMessage();
        }
    }
}
