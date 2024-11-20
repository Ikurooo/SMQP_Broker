package dslab.broker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class FanoutExchange implements Exchange {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<String, NamedQueue> qs = new ConcurrentHashMap<>();
    private static volatile FanoutExchange instance;
    private final String name;

    public FanoutExchange(String name) {
        this.name = name;
    }

    @Override
    public void bind(NamedQueue queue, String routingKey) {
        this.lock.writeLock().lock();
        this.qs.put(queue.getName(), queue);
        this.lock.writeLock().unlock();
    }

    @Override
    public void publish(String routingKey, String message) {
        lock.writeLock().lock();
        this.qs.forEach((key, value) -> value.enqueue(message));
        lock.writeLock().unlock();
    }

    public String getName() {
        return name;
    }
}