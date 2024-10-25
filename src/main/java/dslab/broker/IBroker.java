package dslab.broker;

import dslab.IServer;

/**
 * Interface for the Message Broker server.
 * <p>
 * NOTE: Do not delete this interface as it is part of the test suite and required for the next assignment.
 */
public interface IBroker extends IServer {

    /**
     * Implement the logic of the Message Broker server starting from this method.
     * It is recommended to create separate classes for the broker logic to keep the code clean and structured.
     */
    @Override
    void run();

    /**
     * Implement a graceful shutdown of the Message Broker server.
     */
    @Override
    void shutdown();
}
