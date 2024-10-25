package dslab.config;

public class ConfigParser {

    private final String componentId;
    private final Config config;

    public ConfigParser(String componentId) {
        this.componentId = componentId;
        this.config = new Config(componentId);
    }

    public BrokerConfig toBrokerConfig() {
        return new BrokerConfig(
                componentId,
                config.getString("broker.host"),
                config.getInt("broker.port"),
                config.getString("broker.domain"),
                config.getString("dns.host"),
                config.getInt("dns.port")
        );
    }

    public DNSServerConfig toDNSServerConfig() {
        return new DNSServerConfig(componentId, config.getInt("dns.port"));
    }

}
