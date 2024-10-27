package dslab.dns;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private volatile boolean shouldRun = true;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.err.println("error: failed to initialize reader/writer for client.");
        }
    }

    @Override
    public void run() {
        this.writeToClient("ok SDP");
        System.out.println("Client connected.");

        while (shouldRun) {
            String line = this.readFromClient();

            if (line == null || line.length() != line.strip().length()) {
                continue;
            }

            List<String> command = List.of(line.split(" "));

            switch (command.getFirst()) {
                case "unregister" -> this.handleUnregister(command);
                case "register" -> this.handleRegister(command);
                case "resolve" -> this.handleResolve(command);
                case "exit" -> this.handleExit(command);
                default -> this.writeToClient("unknown command: " + line);
            }
        }

        this.handleExit(List.of("exit"));
    }

    private void handleExit(List<String> command) {
        if (command.size() != 1 || !"exit".equals(command.getFirst())) {
            this.writeToClient("error: invalid arguments.");
            return;
        }

        this.writeToClient("ok bye");
        try {
            this.reader.close();
            this.writer.close();
            this.clientSocket.close();
        } catch (IOException e) {
            System.err.println("error: failed closing resources.");
        }

        shouldRun = false; // Signal the run loop to exit
    }

    private void handleResolve(List<String> command) {
        if (command.size() != 2 || !"resolve".equals(command.getFirst())) {
            this.writeToClient("error: invalid arguments.");
            return;
        }

        String name = command.get(1);
        String ipPort = BrokerConfigWriter.getMapping(name);

        if (ipPort == null || ipPort.isBlank()) {
            this.writeToClient("error: no broker found for domain: " + name);
            return;
        }

        this.writeToClient(ipPort);
    }

    private void handleUnregister(List<String> command) {
        if (command.size() != 2 || !"unregister".equals(command.getFirst())) {
            this.writeToClient("error: invalid arguments.");
            return;
        }

        String name = command.get(1);

        BrokerConfigWriter.deleteMapping(name);
        this.writeToClient("ok");
    }

    private void handleRegister(List<String> command) {
        if (command.size() != 3 || !"register".equals(command.getFirst())) {
            this.writeToClient("error: invalid arguments.");
            return;
        }

        String name = command.get(1);
        String ipPort = command.get(2);

        if (ipPort.split(":").length != 2) {
            this.writeToClient("error: invalid ip and port format <ip:port>.");
            return;
        }

        BrokerConfigWriter.addMapping(name, ipPort);
        this.writeToClient("ok");
    }

    private void writeToClient(String message) {
        try {
            this.writer.write(message + "\n");
            this.writer.flush();
        } catch (IOException e) {
            System.err.println("error: failed writing to client. " + e.getMessage());
        }
    }

    private String readFromClient() {
        try {
            return this.reader.readLine();
        } catch (IOException e) {
            System.err.println("error: failed reading from client.");
            return null;
        }

    }

}
