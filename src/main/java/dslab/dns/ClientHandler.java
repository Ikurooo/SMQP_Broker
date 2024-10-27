package dslab.dns;

import java.io.*;
import java.net.Socket;

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

            String[] command = line.split(" ");

            switch (command[0]) {
                case "unregister" -> this.handleUnregister(command);
                case "register" -> this.handleRegister(command);
                case "resolve" -> this.handleResolve(command);
                case "exit" -> this.handleExit(command);
                default -> this.writeToClient("unknown command: " + line);
            }
        }

        this.handleExit(new String[]{"exit"});
    }

    private void handleExit(String[] command) {
        if (command.length != 1 || !"exit".equals(command[0])) {
            this.writeToClient("error: exit");
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

    private void handleResolve(String[] command) {
        if (command.length != 2 || !"resolve".equals(command[0])) {
            this.writeToClient("error: resolve <name>");
            return;
        }

        String name = command[1];
        String ipPort = BrokerConfigWriter.getMapping(name);

        if (ipPort == null || ipPort.isBlank()) {
            this.writeToClient("error: no broker found for domain: " + name);
            return;
        }

        this.writeToClient(ipPort);
    }

    private void handleUnregister(String[] command) {
        if (command.length != 2 || !"unregister".equals(command[0])) {
            this.writeToClient("error: unregister <name>");
            return;
        }

        String name = command[1];

        BrokerConfigWriter.deleteMapping(name);
        this.writeToClient("ok");
    }

    private void handleRegister(String[] command) {
        if (command.length != 3 || !"register".equals(command[0])) {
            this.writeToClient("error: register <name> <ip:port>");
            return;
        }

        String name = command[1];
        String ipPort = command[2];

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
