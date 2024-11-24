package dslab.broker;

import java.util.concurrent.ConcurrentHashMap;

class FanoutExchange implements Exchange {

    private final String name;
    private final String type;
    private final ConcurrentHashMap<String, NamedQueue> qs;

    public FanoutExchange(String name) {
        this.name = name;
        this.type = "fanout";
        this.qs = new ConcurrentHashMap<>();
    }

    @Override
    public void bind(NamedQueue queue, String routingKey) {
        this.qs.put(queue.getName(), queue);
    }

    @Override
    public void publish(String routingKey, String message) {
        this.qs.forEach((key, value) -> value.enqueue(message));
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
