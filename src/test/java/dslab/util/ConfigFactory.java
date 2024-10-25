package dslab.util;

import dslab.config.BrokerConfig;
import dslab.config.DNSServerConfig;

public class ConfigFactory {

    public static DNSServerConfig createDNSServerConfig() {
        return new DNSServerConfig("dns-0", Constants.DNS_PORT);
    }

    public static BrokerConfig createBrokerConfigA2() {
        return createBrokerConfigsA2(1)[0];
    }

    public static BrokerConfig[] createBrokerConfigsA2(int numberOfBrokers) {
        BrokerConfig[] configs = new BrokerConfig[numberOfBrokers];

        int globalBasePort = 20000;

        for (int id = 0; id < numberOfBrokers; id++) {
            int offset = 10 * id;
            int brokerPort = globalBasePort + offset;

            configs[id] = new BrokerConfig(
                    "broker-%d".formatted(id),
                    Constants.LOCALHOST,
                    brokerPort,
                    "broker-%d.at".formatted(id),
                    Constants.LOCALHOST,
                    Constants.DNS_PORT
            );
        }

        return configs;
    }
}
