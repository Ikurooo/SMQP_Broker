package dslab.dns;

import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;

/**
 * ClientHandler
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private BufferedWriter writer;
    private BufferedReader reader;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        this.writeToClient("ok SDP");

        while (!Thread.currentThread().isInterrupted()) {
            String line = this.readFromClient();

            // This guarantees that the line contains at least one word
            if (line == null || line.length() != line.strip().length()) {
                this.writeToClient("unknown command: " + line);
                continue;
            }

            List<String> command = List.of(line.split(" "));

            switch (command.getFirst()) {
                case "exit" -> this.handleExit(command);
                case "register" -> this.handleRegister(command);
                case "unregister" -> this.handleUnregister(command);
                case "resolve" -> this.handleResolve(command);
                default -> this.writeToClient("unknown command: " + line);
            }
        }
    }

    private void handleResolve(List<String> command) {
        if (command.size() != 2 || !"resolve".equals(command.getFirst())) {
            this.writeToClient("error: invalid arguments.");
            return;
        }

        String name = command.get(2);
        String ipPort = BrokerConfigWriter.getMapping(name);

        if (ipPort == null || ipPort.isBlank()) {
            this.writeToClient("error: no broker found for domain: " + name);
            return;
        }

        this.writeToClient("ok " + ipPort);
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

        String ipPort = command.get(2);
        String name = command.get(1);

        if (ipPort.split(":").length != 2) {
            this.writeToClient("error: invalid ip and port format <ip:port>.");
            return;
        }

        BrokerConfigWriter.addMapping(name, ipPort);
        this.writeToClient("ok");
    }

    private void handleExit(List<String> command) {
        if (command.size() != 1 || !"exit".equals(command.getFirst())) {
            this.writeToClient("error: invalid arguments.");
            return;
        }

        this.writeToClient("bye");
        try {
            this.reader.close();
            this.writer.close();
            this.clientSocket.close();
        } catch (IOException e) {
            System.err.println("error: failed closing resources.");
        }

        // TODO: this may cause some unexpected behaviour potentially..
        Thread.currentThread().interrupt();
    }

    private void writeToClient(String message) {
        try {
            this.writer.write(message + "\n");
        } catch (IOException e) {
            System.err.println("error: falied writing to client.");
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
