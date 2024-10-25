package dslab;

import dslab.broker.Broker;
import dslab.broker.IBroker;
import dslab.config.BrokerConfig;
import dslab.config.ConfigParser;
import dslab.config.DNSServerConfig;
import dslab.dns.DNSServer;
import dslab.dns.IDNSServer;

public class ComponentFactory {
    /**
     * Creates a broker via the given config. Used for Testing
     * @param config config of the broker
     * @return a new broker
     */
    public static IBroker createBroker(BrokerConfig config) {
        return new Broker(config);
    }

    /**
     * Creates a broker via the .properties file
     * @param componentId the id of the broker e.g. "broker-0"
     * @return a new broker
     */
    public static IBroker createBroker(String componentId) {
        ConfigParser parser = new ConfigParser(componentId);
        BrokerConfig brokerConfig = parser.toBrokerConfig();

        return createBroker(brokerConfig);
    }

    public static IDNSServer createDNSServer(DNSServerConfig config) {
        return new DNSServer(config);
    }

    /**
     * Creates a new DNS server with the given componentId
     * @param componentId the id of the DNS Server e.g. "dns-0"
     * @return a new DNS server
     */
    public static IDNSServer createDNSServer(String componentId) {
        ConfigParser parser = new ConfigParser(componentId);
        DNSServerConfig dnsServerConfig = parser.toDNSServerConfig();

        return createDNSServer(dnsServerConfig);
    }
}
