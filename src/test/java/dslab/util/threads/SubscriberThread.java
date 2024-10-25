package dslab.util.threads;

import dslab.util.Constants;
import dslab.util.helper.TelnetClientHelper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SubscriberThread extends Thread {

    private final TelnetClientHelper helper;
    private final String exchangeName;
    private final String exchangeType;
    private final String queueName;
    private final int expectedMessages;

    private final List<String> receivedMessages = new LinkedList<>();

    public SubscriberThread(int remotePort, String exchangeType, String exchangeName, String queueName, int numberOfExpectedMessages) {
        helper = new TelnetClientHelper(Constants.LOCALHOST, remotePort);
        this.exchangeName = exchangeName;
        this.exchangeType = exchangeType;
        this.queueName = queueName;
        this.expectedMessages = numberOfExpectedMessages;
    }

    @Override
    public void run() {
        try {
            helper.connectAndReadResponse();
            helper.sendCommandAndReadResponse("exchange %s %s".formatted(exchangeType, exchangeName));
            helper.sendCommandAndReadResponse("queue %s".formatted(queueName));
            helper.sendCommandAndReadResponse("subscribe");

            for (int i = 0; i < expectedMessages; i++) {
                String s = helper.readResponse();
                receivedMessages.add(s);
            }
        } catch (IOException e) {
            // ignored
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        try {
            helper.disconnect();
        } catch (IOException e) {
            // ignored
        }
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }
}
