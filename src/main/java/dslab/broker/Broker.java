package dslab.broker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import dslab.ComponentFactory;
import dslab.config.BrokerConfig;

public class Broker implements IBroker {

    private final String componentId;
    private final String host;
    private final int port;

    private ServerSocket serverSocket;

    private final String domain;
    private final String dnsHost;
    private final int dnsPort;

    private BufferedReader dnsReader;
    private BufferedWriter dnsWriter;
    private Socket dnsSocket;

    private final Map<String, Exchange> exchanges;
    private final Map<String, NamedQueue> queues;
    private final Exchange defaultExchange;

    private final ExecutorService clientHandlerPool = Executors.newCachedThreadPool(); // Directly initialize here

    private volatile boolean running;

    public Broker(BrokerConfig config) {
        this.running = true;
        this.host = config.host();
        this.port = config.port();
        this.domain = config.domain();
        this.dnsHost = config.dnsHost();
        this.dnsPort = config.dnsPort();
        this.componentId = config.componentId();
        this.queues = new ConcurrentHashMap<>();
        this.exchanges = new ConcurrentHashMap<>();
        this.defaultExchange = new DefaultExchange("default");

        this.exchanges.putIfAbsent("default", this.defaultExchange);
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Error: Unable to start broker:" + e.getCause() + ", " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("Broker is running. Listening for clients on port " + port);
        this.registerWithDNS();
        Stream.generate(this::tryAcceptClient)
                .takeWhile(clientSocket -> this.running)
                .forEach(clientSocket -> clientSocket.ifPresent(socket -> {
                    this.clientHandlerPool.submit(new BrokerClientHandler(socket, this.exchanges, this.queues, this.defaultExchange));
                }));
    }

    private Optional<Socket> tryAcceptClient() {
        try {
            return Optional.of(this.serverSocket.accept());
        } catch (IOException e) {
            System.err.println("Error accepting client connection: " + e.getMessage());
            if (this.serverSocket.isClosed())
                this.running = false;
            return Optional.empty();
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Broker shutting down...");
        this.running = false;
        this.clientHandlerPool.shutdownNow();

        if (this.dnsReader != null && this.dnsWriter != null)
            this.deregisterWithDNS();

        try {
            if (this.serverSocket != null && !this.serverSocket.isClosed())
                this.serverSocket.close();
            if (this.dnsReader != null)
                this.dnsReader.close();
            if (this.dnsWriter != null)
                this.dnsWriter.close();
            if (this.dnsSocket != null)
                this.dnsSocket.close();

        } catch (IOException e) {
            System.err.println("Error: Failed to close resources. " + e.getMessage());
        }
    }

    private void registerWithDNS() {
        try {
            this.dnsSocket = new Socket(this.dnsHost, this.dnsPort);
            this.dnsReader = new BufferedReader(new InputStreamReader(this.dnsSocket.getInputStream()));
            this.dnsWriter = new BufferedWriter(new OutputStreamWriter(this.dnsSocket.getOutputStream()));

            if (!"ok SDP".equals(this.readFromDNS())) {
                System.err.println("Error: Failed to register with DNS.");
                return;
            }

            this.writeToDNS(String.format("register %s %s:%d", domain, host, port));

            if (!"ok".equals(this.readFromDNS())) {
                System.err.println("Error: Failed to register with DNS.");
            } else {
                System.out.println("Successfully registered with DNS.");
            }

        } catch (IOException e) {
            System.err.println("Failed to register with DNS");
        }
    }

    private void deregisterWithDNS() {
        this.writeToDNS(String.format("unregister %s", domain));
        if (!"ok".equals(this.readFromDNS())) {
            System.err.println("Error: Failed to unregister from DNS.");
        } else {
            System.out.println("Successfully unregistered from DNS.");
        }
    }


    private void writeToDNS(String message) {
        try {
            this.dnsWriter.write(message + "\n");
            this.dnsWriter.flush();
        } catch (IOException e) {
            System.err.println("Error: Failed writing to DNS. " + e.getMessage());
        }
    }

    private String readFromDNS() {
        try {
            return this.dnsReader.readLine();
        } catch (IOException e) {
            System.err.println("Error: Failed reading from DNS. " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        ComponentFactory.createBroker(args[0]).run(); // Start the broker
    }
}
