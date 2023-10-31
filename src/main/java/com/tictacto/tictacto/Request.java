package com.tictacto.tictacto;

import java.io.*;
import java.net.*;
import java.util.Objects;

public class Request{

    private final String serverAddress = "127.0.0.1";
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

            if (request.equals("logout")){
                in.close();
                out.close();
                socket.close();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
