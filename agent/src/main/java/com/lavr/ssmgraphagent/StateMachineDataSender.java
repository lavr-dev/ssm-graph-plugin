package com.lavr.ssmgraphagent;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class StateMachineDataSender {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 4321;

    public static void send(String graphData) {
        System.out.println("[SSM Graph Agent] Sent graph data start");

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 3000);
            socket.setSoTimeout(3000);
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
                out.println(graphData);
                out.flush();
                System.out.println("[SSM Graph Agent] Sent graph data successfully");
            }
        } catch (Exception e) {
            System.err.println("[SSM Graph Agent] Cannot send data to plugin: " + e.getMessage());
            System.err.flush();
        }
    }
}