package dslab.dns;

import java.util.concurrent.ConcurrentHashMap;

public class BrokerConfigWriter {

    private static final ConcurrentHashMap<String, String> brokerMappings = new ConcurrentHashMap<>();

    public static void addMapping(String name, String ipPort) {
        if (name == null || name.isEmpty() || ipPort == null || ipPort.isEmpty()) {
            throw new IllegalArgumentException("Broker name and IP/Port cannot be null or empty.");
        }

        brokerMappings.put(name, ipPort);
    }

    public static void deleteMapping(String brokerName) {
        brokerMappings.remove(brokerName);
    }

    public static String getMapping(String name) {
        return brokerMappings.get(name);
    }
}
