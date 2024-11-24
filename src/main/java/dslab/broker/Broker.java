package dslab.broker;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;
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
            return Optional.empty();
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Broker shutting down...");
        this.running = false;
        this.clientHandlerPool.shutdownNow();
        this.deregisterWithDNS();

        try {
            if (this.serverSocket != null && !this.serverSocket.isClosed())
                this.serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error: Failed to close resources. " + e.getMessage());
        }
    }

    private void registerWithDNS() {
        try {
            Socket dnsSocket = new Socket(this.dnsHost, this.dnsPort);
            BufferedReader dnsReader = new BufferedReader(new InputStreamReader(dnsSocket.getInputStream()));
            BufferedWriter dnsWriter = new BufferedWriter(new OutputStreamWriter(dnsSocket.getOutputStream()));

            if (!"ok SDP".equals(this.readFromDNS(dnsReader))) {
                System.err.println("Error: Failed to deregister with DNS.");
                return;
            }

            this.writeToDNS(dnsWriter, String.format("register %s %s:%d", domain, host, port));

            if (!"ok".equals(this.readFromDNS(dnsReader))) {
                System.err.println("Error: Failed to register with DNS.");
            } else {
                System.out.println("Successfully registered with DNS.");
            }
            dnsWriter.close();
            dnsReader.close();
            if (!dnsSocket.isClosed())
                dnsSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to register with DNS");
        }
    }

    private void deregisterWithDNS() {
        try {
            Socket dnsSocket = new Socket(this.dnsHost, this.dnsPort);
            BufferedReader dnsReader = new BufferedReader(new InputStreamReader(dnsSocket.getInputStream()));
            BufferedWriter dnsWriter = new BufferedWriter(new OutputStreamWriter(dnsSocket.getOutputStream()));

            if (!"ok SDP".equals(this.readFromDNS(dnsReader))) {
                System.err.println("Error: Failed to register with DNS.");
                return;
            }

            this.writeToDNS(dnsWriter, String.format("unregister %s", domain));

            if (!"ok".equals(this.readFromDNS(dnsReader))) {
                System.err.println("Error: Failed to deregister from DNS.");
            } else {
                System.out.println("Successfully deregistered from DNS.");
            }
            dnsWriter.close();
            dnsReader.close();
            if (!dnsSocket.isClosed())
                dnsSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to deregister from DNS");
        }
    }

    private void writeToDNS(BufferedWriter dnsWriter, String message) {
        try {
            dnsWriter.write(message + "\n");
            dnsWriter.flush();
        } catch (IOException e) {
            System.err.println("Error: Failed writing to DNS. " + e.getMessage());
        }
    }

    private String readFromDNS(BufferedReader dnsReader) {
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
