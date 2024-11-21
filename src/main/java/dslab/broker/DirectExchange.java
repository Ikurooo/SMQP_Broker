package dslab.broker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

public class DirectExchange implements Exchange {

    private final ConcurrentHashMap<String, List<NamedQueue>> qs = new ConcurrentHashMap<>();
    private final String name;

    public DirectExchange(String name) {
        this.name = name;
    }

    public void bind(NamedQueue queue, String routingKey) {
        qs.computeIfAbsent(routingKey, key -> new CopyOnWriteArrayList<>()).add(queue);
    }

    public void publish(String routingKey, String message) {
        Optional.ofNullable(qs.get(routingKey))
                .ifPresent(queues -> queues.forEach(queue -> queue.enqueue(message)));
    }

    public String getName() {
        return this.name;
    }
}
