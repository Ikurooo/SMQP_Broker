package dslab.broker;

public class TopicExchange implements Exchange {

    private final String name;
    private final String type;
    private final Trie trie;

    public TopicExchange(String name) {
        this.name = name;
        this.type = "topic";
        this.trie = new Trie();
    }

    public void bind(NamedQueue queue, String routingKey) {
        this.trie.bind(queue, routingKey.split("\\."));
    }

    public void publish(String routingKey, String message) {
        this.trie.publish(routingKey.split("\\."), message);
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
