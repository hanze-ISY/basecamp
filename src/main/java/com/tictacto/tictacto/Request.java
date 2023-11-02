package com.tictacto.tictacto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Request {

    private final String serverAddress = "145.33.225.170";
    private final int serverPort = 7789;
    private final int timeoutMillis = 600000;

    public void connectToServer(String request) {
        try {
            Socket socket = new Socket(serverAddress, serverPort);

            socket.setSoTimeout(timeoutMillis);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(request);

            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
                if (response.equals("OK")) {
                    break;
                }
            }
            System.out.println("End of loop");

            if (request.equals("logout")) {
                in.close();
                out.close();
                socket.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
