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
    private final Exchange defaultExchange;
    private BufferedWriter writer;
    private BufferedReader reader;
    private NamedQueue queue;
    private Exchange exchange;

    private volatile boolean shouldRun = true;

    public BrokerClientHandler(Socket clientSocket, Map<String, Exchange> exchanges,
                               Map<String, NamedQueue> queues, Exchange defaultExchange) {
        this.clientSocket = clientSocket;
        this.exchanges = exchanges;
        this.queues = queues;
        this.defaultExchange = defaultExchange;
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
                case "exit" -> this.handleExit(command);
                case "bind" -> this.handleBind(command);
                case "queue" -> this.handleQueue(command);
                case "publish" -> this.handlePublish(command);
                case "exchange" -> this.handleExchange(command);
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

        this.exchange.bind(this.queue, routingKey);
        this.writeToClient("ok");
    }

    private void handleQueue(String[] args) {
        if (args.length != 2) {
            this.writeToClient("error, usage: queue <name>");
            return;
        }

        String queueName = args[1];

        this.queue = this.queues.computeIfAbsent(queueName, NamedQueue::new);
        this.bindToDefaultOnCreate(queueName, this.queue);
        this.writeToClient("ok");
    }

    private void bindToDefaultOnCreate(String queueName, NamedQueue queue) {
        this.defaultExchange.bind(queue, queueName);
    }

    private void handleExchange(String[] args) {
        if (args.length != 3) {
            this.writeToClient("error, usage: exchange <type> <name>");
            return;
        }

        String type = args[1];
        String exchangeName = args[2];

        if (this.exchanges.containsKey(exchangeName) && !this.exchanges.get(exchangeName).getType().equals(type)) {
            this.writeToClient("error: exchange already exists under a different type.");
            return;
        }

        switch (type) {
            case "default" -> this.exchange = this.defaultExchange;
            case "topic" -> this.exchange = this.exchanges.computeIfAbsent(exchangeName, TopicExchange::new);
            case "fanout" -> this.exchange = this.exchanges.computeIfAbsent(exchangeName, FanoutExchange::new);
            case "direct" -> this.exchange = this.exchanges.computeIfAbsent(exchangeName, DirectExchange::new);
            default -> {
            }
        }

        this.writeToClient("ok");
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

        this.exchange.publish(routingKey, message);
        this.writeToClient("ok");
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

        Subscription subscription = new Subscription(this.queue, this::writeToClient);
        subscription.start();
        this.writeToClient("ok");
        this.readFromClient();
        subscription.interrupt();
        this.writeToClient("ok");
    }

    private void handleExit(String[] args) {
        if (args.length != 1) {
            this.writeToClient("error: incorrect arguments. usage: exit");
            return;
        }

        try {
            this.shouldRun = false;
            if (this.writer != null) {
                this.writeToClient("ok bye");
                this.writer.close();
            }
            if (this.reader != null)
                this.reader.close();
            if (this.clientSocket != null && !this.clientSocket.isClosed())
                this.clientSocket.close();
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            System.err.println("error: failed to close resources. " + e.getMessage());
        }
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
