package dslab.broker;

import java.util.concurrent.ConcurrentHashMap;

class FanoutExchange implements Exchange {

    private final ConcurrentHashMap<String, NamedQueue> qs = new ConcurrentHashMap<>();
    private final String name;

    public FanoutExchange(String name) {
        this.name = name;
    }

    @Override
    public void bind(NamedQueue queue, String routingKey) {
        this.qs.put(queue.getName(), queue);
    }

    @Override
    public void publish(String routingKey, String message) {
        this.qs.forEach((key, value) -> value.enqueue(message));
    }

    public String getName() {
        return this.name;
    }
}
