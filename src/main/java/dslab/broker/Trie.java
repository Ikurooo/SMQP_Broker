package dslab.broker;

import java.util.Stack;
import java.util.Arrays;
import java.util.Optional;

public class Trie {
    public TrieNode root;

    public Trie() {
        this.root = new TrieNode(".");
    }

    public void bind(NamedQueue queue, String[] routingKey) {
        TrieNode[] currHolder = {this.root};
        Arrays.stream(routingKey).forEach(keyPart -> {
            currHolder[0].getNext().computeIfAbsent(keyPart, TrieNode::new);
            currHolder[0] = currHolder[0].getNext().get(keyPart);
        });
        currHolder[0].insertQueue(queue);
    }

    public void publish(String[] routingKey, String message) {
        Stack<Pair<TrieNode, Integer>> stack = new Stack<>();
        stack.push(new Pair<>(this.root, 0));

        while (!stack.isEmpty()) {
            Pair<TrieNode, Integer> curr = stack.pop();

            if (curr.index() >= routingKey.length) {
                Optional.ofNullable(curr.node().getNext().get("#"))
                        .map(TrieNode::getQueues)
                        .orElse(curr.node().getQueues())
                        .forEach(queue -> queue.enqueue(message));
                continue;
            }

            if (curr.node().getNext().containsKey(routingKey[curr.index()]))
                stack.push(new Pair<>(curr.node().getNext().get(routingKey[curr.index()]), curr.index() + 1));


            if (curr.node().getNext().containsKey("*"))
                stack.push(new Pair<>(curr.node().getNext().get("*"), curr.index() + 1));

            if (curr.node().getNext().containsKey("#")) {
                stack.push(new Pair<>(curr.node().getNext().get("#"), curr.index()));
                stack.push(new Pair<>(curr.node(), curr.index() + 1));
            }
        }
    }
}
