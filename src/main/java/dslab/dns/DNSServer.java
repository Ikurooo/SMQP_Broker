package dslab.dns;

import dslab.ComponentFactory;
import dslab.config.DNSServerConfig;

public class DNSServer implements IDNSServer {

    public DNSServer(DNSServerConfig config) {
    }

    @Override
    public void run() {
    }

    @Override
    public void shutdown() {
    }

    public static void main(String[] args) {
        ComponentFactory.createDNSServer(args[0]).run();
    }
}
