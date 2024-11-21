package dslab.broker;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultExchange implements Exchange {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<String, NamedQueue> qs = new ConcurrentHashMap<>();
    private final String name;

    public DefaultExchange(String name) {
        this.name = name;
    }

    @Override
    public void bind(NamedQueue queue, String routingKey) {
        lock.writeLock().lock();
        this.qs.putIfAbsent(queue.getName(), queue);
        lock.writeLock().unlock();
    }

    @Override
    public void publish(String routingKey, String message) {
        lock.writeLock().lock();
        qs.computeIfPresent(routingKey, (key, queue) -> {
            queue.enqueue(message);
            return queue;
        });
        lock.writeLock().unlock();
    }

    public String getName() {
        return this.name;
    }
}
