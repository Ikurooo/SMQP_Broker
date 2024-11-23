package dslab.broker;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TrieNode {
    private final String word;
    private final List<NamedQueue> boundQueues;
    private final ConcurrentMap<String, TrieNode> next;

    public TrieNode(String word) {
        this.word = word;
        this.next = new ConcurrentHashMap<>();
        this.boundQueues = Collections.synchronizedList(new ArrayList<NamedQueue>());
    }

    public void insertQueue(NamedQueue queue) {
        this.boundQueues.add(queue);
    }

    public Iterable<NamedQueue> getQueues() {
        return this.boundQueues;
    }

    public ConcurrentMap<String, TrieNode> getNext() {
        return this.next;
    }

    public String getWord() {
        return this.word;
    }
}
