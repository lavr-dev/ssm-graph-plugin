package com.lavr.ssmgraphagent;

import java.io.PrintWriter;
import java.net.Socket;

public class StateMachineDataSender {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4321;

    public static void send(String graphData) {
        System.out.println("[SSM Agent] Sent graph data start");
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(graphData);
//            System.out.println("[SSM Agent] Sent graph data: " + graphData);
            System.out.println("[SSM Agent] Sent graph data: ");
        } catch (Exception e) {
            System.err.println("[SSM Agent] Cannot send data to plugin: " + e.getMessage());
        }
    }
}