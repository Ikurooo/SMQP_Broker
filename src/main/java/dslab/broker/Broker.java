package dslab.broker;

import java.io.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dslab.ComponentFactory;
import dslab.config.BrokerConfig;

public class Broker implements IBroker {

    private final String componentId;
    private final String host;
    private final int port;
    private final String domain;
    private final String dnsHost;
    private final int dnsPort;

    private BufferedReader dnsReader;
    private BufferedWriter dnsWriter;
    private Socket dnsSocket;
    private ServerSocket serverSocket;

    private final Map<String, Exchange> exchanges = new ConcurrentHashMap<>();
    private final Map<String, NamedQueue> queues = new ConcurrentHashMap<>();

    private final ExecutorService clientHandlerPool = Executors.newCachedThreadPool();

    public Broker(BrokerConfig config) {
        this.componentId = config.componentId();
        this.host = config.host();
        this.port = config.port();
        this.domain = config.domain();
        this.dnsHost = config.dnsHost();
        this.dnsPort = config.dnsPort();
    }

    @Override
    public void run() {

        this.registerWithDNS();

        try {
            serverSocket = new ServerSocket(port);

            System.out.println("Broker is running. Listening for clients on port " + port);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

                    BrokerClientHandler clientHandler = new BrokerClientHandler(clientSocket, this.exchanges, this.queues);
                    clientHandlerPool.execute(clientHandler);

                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        System.out.println("Server socket closed. Stopping the broker.");
                        break;
                    }
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Error: Unable to start broker:" + e.getCause() + ", " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Broker shutting down...");
        if (dnsReader != null && dnsWriter != null) {
            deregisterWithDNS();
        }
        clientHandlerPool.shutdownNow();
        try {


            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
            if (dnsReader != null)
                dnsReader.close();
            if (dnsWriter != null)
                dnsWriter.close();
            if (dnsSocket != null)
                dnsSocket.close();


        } catch (IOException e) {
            System.err.println("Error: Failed to close resources. " + e.getMessage());
        }
    }

    private void registerWithDNS() {
        try {
            dnsSocket = new Socket(dnsHost, dnsPort);
            dnsReader = new BufferedReader(new InputStreamReader(dnsSocket.getInputStream()));
            dnsWriter = new BufferedWriter(new OutputStreamWriter(dnsSocket.getOutputStream()));
            if (!Objects.equals(readFromDNS(), "ok SDP"))
                return;

            String registerCommand = String.format("register %s %s:%d", domain, host, port);
            this.writeToDNS(registerCommand);
            String response = readFromDNS();
            if (response == null || !response.equalsIgnoreCase("ok")) {
                System.err.println("Error: Failed to register with DNS. Response: " + response);
            } else {
                System.out.println("Successfully registered with DNS.");
            }
        } catch (IOException e) {
            // skips registration with the DNS
            System.err.println("Failed to register with DNS");
        }

    }

    private void deregisterWithDNS() {
        String unregisterCommand = String.format("unregister %s", domain);
        this.writeToDNS(unregisterCommand);
        String response = readFromDNS();
        if (response == null || !response.equalsIgnoreCase("ok")) {
            System.err.println("Error: Failed to unregister from DNS. Response: " + response);
        } else {
            System.out.println("Successfully unregistered from DNS.");
        }
    }

    private void writeToDNS(String message) {
        try {
            dnsWriter.write(message + "\n");
            dnsWriter.flush();
        } catch (IOException e) {
            System.err.println("Error: Failed writing to DNS. " + e.getMessage());
        }
    }

    private String readFromDNS() {
        try {
            return dnsReader.readLine();
        } catch (IOException e) {
            System.err.println("Error: Failed reading from DNS. " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        ComponentFactory.createBroker(args[0]).run();
    }
}
