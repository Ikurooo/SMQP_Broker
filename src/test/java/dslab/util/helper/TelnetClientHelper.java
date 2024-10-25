package dslab.util.helper;

import dslab.util.CommandBuilder;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class TelnetClientHelper {

    private final TelnetClient client;
    private final String remoteHost;
    private final int remotePort;
    private BufferedReader reader;
    private PrintStream out;

    public TelnetClientHelper(String remoteHost, int remotePort) {
        this.client = new TelnetClient();
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public String connectAndReadResponse() throws IOException {
        this.client.connect(remoteHost, remotePort);
        this.out = new PrintStream(client.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));

        return readResponse();
    }

    public void waitForInitConnection() {
        await().pollInterval(5, TimeUnit.MILLISECONDS).atMost(1, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> {
                    connectAndReadResponse();
                    return true;
                });
    }

    public String waitForDnsRegistration(String domainName) {
        return await().pollInterval(5, TimeUnit.MILLISECONDS).atMost(1, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> {
                    String res = sendCommandAndReadResponse("resolve %s".formatted(domainName));
                    return res != null && !res.isEmpty() ? res : "ERROR IN DNS REGISTRATION";
                }, Objects::nonNull);
    }

    public String sendCommandAndReadResponse(String command) throws IOException {
        sendMsg(command);
        return readResponse();
    }

    private void sendMsg(String command) {
        out.println(command);
    }

    public void disconnect() throws IOException {
        if (this.client.isConnected()) this.client.disconnect();
    }

    public String readResponse() throws IOException {
        String res = reader.readLine();
        return res != null ? res.trim() : null;
    }

    public void publish(String exchangeName, String exchangeType, String routingKey, String msg) throws IOException {
        sendCommandAndReadResponse(CommandBuilder.exchange(exchangeType, exchangeName));
        sendCommandAndReadResponse(CommandBuilder.publish(routingKey, msg));
    }

    public String publish(String routingKey, String msg) throws IOException {
        return sendCommandAndReadResponse(CommandBuilder.publish(routingKey, msg));
    }

    public void subscribe(String exchangeName, String exchangeType, String queueName, String... bindingKeys) throws IOException {
        sendCommandAndReadResponse(CommandBuilder.exchange(exchangeType, exchangeName));
        sendCommandAndReadResponse(CommandBuilder.queue(queueName));
        for (String bindingKey : bindingKeys) sendCommandAndReadResponse(CommandBuilder.bind(bindingKey));
        sendCommandAndReadResponse(CommandBuilder.subscribe());
    }

    public void subscribe(String queueName) throws IOException {
        sendCommandAndReadResponse(CommandBuilder.queue(queueName));
        sendCommandAndReadResponse(CommandBuilder.subscribe());
    }
}
