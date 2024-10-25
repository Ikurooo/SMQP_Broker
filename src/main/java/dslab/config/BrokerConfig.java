package dslab.config;

import java.util.Objects;

public record BrokerConfig(
        String componentId,
        String host,
        int port,
        String domain,
        String dnsHost,
        int dnsPort
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrokerConfig that = (BrokerConfig) o;
        return dnsPort == that.dnsPort &&
                port == that.port &&
                Objects.equals(dnsHost, that.dnsHost) &&
                Objects.equals(domain, that.domain) &&
                Objects.equals(host, that.host) &&
                Objects.equals(componentId, that.componentId);
    }
}
