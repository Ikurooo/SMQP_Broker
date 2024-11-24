package dslab.broker;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultExchange implements Exchange {

    private final String name;
    private final String type;
    private final ConcurrentHashMap<String, NamedQueue> qs;

    public DefaultExchange(String name) {
        this.name = name;
        this.type = "default";
        this.qs = new ConcurrentHashMap<>();
    }

    @Override
    public void bind(NamedQueue queue, String routingKey) {
        this.qs.putIfAbsent(queue.getName(), queue);
    }

    @Override
    public void publish(String routingKey, String message) {
        qs.computeIfPresent(routingKey, (key, queue) -> {
            queue.enqueue(message);
            return queue;
        });
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getType() {
        return this.type;
    }
}
