package dslab.broker;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultExchange implements Exchange {

    private final ConcurrentHashMap<String, NamedQueue> qs = new ConcurrentHashMap<>();
    private final String name;

    public DefaultExchange(String name) {
        this.name = name;
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

    public String getName() {
        return this.name;
    }
}
