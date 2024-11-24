package dslab.dns;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.stream.Stream;

import dslab.ComponentFactory;
import dslab.config.DNSServerConfig;

public class DNSServer implements IDNSServer {

    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private volatile boolean isRunning;

    public DNSServer(DNSServerConfig config) {
        this.isRunning = true;
        this.threadPool = Executors.newWorkStealingPool();
        try {
            this.serverSocket = new ServerSocket(config.port());
        } catch (IOException e) {
            System.err.println("Couldn't start DNS server. " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("Server is listening on port: " + this.serverSocket.getLocalPort());
        Stream.generate(this::tryAcceptClient)
                .takeWhile(clientSocket -> isRunning)
                .forEach(clientSocket -> clientSocket.ifPresent(socket ->
                        threadPool.submit(new ClientHandler(socket)))
                );
    }

    private Optional<Socket> tryAcceptClient() {
        try {
            return Optional.of(serverSocket.accept());
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void shutdown() {
        System.out.println("Shutting down DNS server...");
        this.isRunning = false;
        this.threadPool.shutdown();

        try {
            if (this.serverSocket != null && !this.serverSocket.isClosed())
                this.serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ComponentFactory.createDNSServer(args[0]).run();
    }
}
