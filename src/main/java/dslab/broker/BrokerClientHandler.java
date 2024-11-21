package dslab.broker;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;

public class BrokerClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Map<String, Exchange> exchanges;
    private final Map<String, NamedQueue> queues;
    private BufferedWriter writer;
    private BufferedReader reader;
    private NamedQueue queue;
    private Exchange exchange;

    private volatile boolean shouldRun = true;

    public BrokerClientHandler(Socket clientSocket, Map<String, Exchange> exchanges, Map<String, NamedQueue> queues) {
        this.clientSocket = clientSocket;
        this.exchanges = exchanges;
        this.queues = queues;
        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.err.println("error: failed to initialize reader/writer for client.");
            this.shouldRun = false;
        }
    }

    @Override
    public void run() {
        if (!this.shouldRun)
            return;

        this.writeToClient("ok SMQP");
        System.out.println("Client connected.");

        while (this.shouldRun) {
            String line = this.readFromClient();
            if (line == null || line.isBlank())
                continue;
            String[] command = line.split(" ");

            switch (command[0]) {
                case "exchange" -> this.handleExchange(command);
                case "queue" -> this.handleQueue(command);
                case "publish" -> this.handlePublish(command);
                case "exit" -> this.handleExit(command);
                case "bind" -> this.handleBind(command);
                case "subscribe" -> this.handleSubscribe(command);
                default -> this.writeToClient("error: unknown command: " + command[0]);
            }
        }
    }

    private void handleBind(String[] args) {
        if (args.length != 2) {
            this.writeToClient("error, usage: bind <binding key>");
            return;
        }

        if (this.queue == null || this.exchange == null) {
            this.writeToClient("error, queue or exchange not found.");
            return;
        }

        String routingKey = args[1];

        this.writeToClient("ok");
        this.exchange.bind(this.queue, routingKey);
    }

    private void handleQueue(String[] args) {
        if (args.length != 2) {
            this.writeToClient("error, usage: queue <name>");
            return;
        }

        String queueName = args[1];

        this.writeToClient("ok");
        this.queue = this.queues.computeIfAbsent(queueName, NamedQueue::new);
    }

    private void handleExchange(String[] args) {
        if (args.length != 3) {
            this.writeToClient("error, usage: exchange <type> <name>");
            return;
        }

        String type = args[1];
        String exchangeName = args[2];

        this.writeToClient("ok");
        switch (type) {
            case "fanout" -> this.exchange = this.exchanges.computeIfAbsent(exchangeName, FanoutExchange::new);
            case "default" -> {
            }
            case "direct" -> {
            }
            case "topic" -> {
            }
            default -> {
            }
        }
    }

    private void handlePublish(String[] args) {
        if (args.length != 3) {
            this.writeToClient("error, usage: publish <routing-key> <message>");
            return;
        }

        if (this.exchange == null) {
            this.writeToClient("error, exchange not set");
            return;
        }

        String routingKey = args[1];
        String message = args[2];

        this.writeToClient("ok");
        this.exchange.publish(routingKey, message);
    }

    private void handleSubscribe(String[] args) {
        if (args.length != 1) {
            this.writeToClient("error, usage: subscribe");
            return;
        }

        if (this.queue == null) {
            this.writeToClient("error, queue not set");
            return;
        }

        this.writeToClient("ok");
        Subscription subscription = new Subscription(this.queue, this::writeToClient);
        subscription.start();
        this.readFromClient();
        subscription.interrupt();
        this.writeToClient("ok");
    }

    private void handleExit(String[] args) {
        if (args.length != 1) {
            this.writeToClient("error: incorrect arguments. usage: exit");
            return;
        }
        this.shouldRun = false;

        try {
            this.reader.close();
            this.writer.close();
            this.clientSocket.close();
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            System.err.println("error: failed to close resources. " + e.getMessage());
        }
    }

    private void writeToClient(String message) {
        try {
            System.out.println(message);
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
