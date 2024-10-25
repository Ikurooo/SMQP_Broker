package dslab.broker;

import dslab.ComponentFactory;
import dslab.config.BrokerConfig;

public class Broker implements IBroker {

    public Broker(BrokerConfig config) {
    }

    @Override
    public void run() {

    }

    @Override
    public void shutdown() {
    }

    public static void main(String[] args) {
        ComponentFactory.createBroker(args[0]).run();
    }
}
