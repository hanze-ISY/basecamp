package com.tictacto.tictacto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class Server {
    private static final Server instance = new Server(Constants.SERVER_IP, Constants.SERVER_PORT, Constants.TIMEOUT_MILLIS);
    private final List<DataEventListener> listeners = new ArrayList<>();
    protected BufferedReader in;
    protected PrintWriter out;
    private Socket socket;

    private Server(String serverAddress, int serverPort, int timeoutMillis) {
        System.out.printf("Connecting to server %s:%d%n", serverAddress, serverPort);
        try {
            socket = new Socket(serverAddress, serverPort);
            socket.setSoTimeout(timeoutMillis);

            System.out.println("Connected to server");

            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);

            // listen for server messages
            // the server can respond with "OK" to accepted commands
            // or can return data in the form of "SVR <data>"
            // if the server responds with "ERR <error message>", the command was not accepted
            // and the error message should be displayed to the user
            // SVR messages should be dispatched to the appropriate event handler

            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("Received response: " + response);
                        if (response.equals("OK")) {
                            // display success message
                            for (DataEventListener listener : listeners) {
                                listener.data(new DataEvent(this, "OK"));
                            }
                        } else if (response.startsWith("SVR")) {
                            // dispatch to event handler
                            for (DataEventListener listener : listeners) {
                                listener.data(new DataEvent(this, response.substring(4)));
                            }
                        } else if (response.startsWith("ERR")) {
                            // display error message
                            for (DataEventListener listener : listeners) {
                                listener.error(new DataEvent(this, response.substring(4)));
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                finally {
                    CloseConnection();
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Server getInstance() {
        return instance;
    }

    public void SendCommand(String command) {
        System.out.println("Sending command: " + command);
        out.println(command);
    }

    // event handlers
    public void AddEventListener(DataEventListener listener) {
        listeners.add(listener);
    }

    public void RemoveEventListener(DataEventListener listener) {
        listeners.remove(listener);
    }

    public void CloseConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
