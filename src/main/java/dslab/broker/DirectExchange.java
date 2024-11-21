package dslab.broker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
        List<NamedQueue> queues = qs.get(routingKey);
        if (queues != null) {
            for (NamedQueue queue : queues) {
                queue.enqueue(message); 
            }
        }
    }

    public String getName() {
        return this.name;
    }
}
