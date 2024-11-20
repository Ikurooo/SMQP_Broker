package dslab.broker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoutingNode {

    private final Map<String, RoutingNode> branches = new HashMap<>();
    private final Set<String> addresses = new HashSet<>();

    public Set<String> getAddresses() {
        return this.addresses;
    }

    public boolean isEmpty() {
        return this.branches.isEmpty();
    }

    public boolean containsKey(String key) {
        return this.addresses.contains(key);
    }

    public RoutingNode getValue(String key) {
        return this.branches.get(key);
    }

    public boolean setAddress(String address) {
        return this.addresses.add(address);
    }

    public boolean removeAddress(String address) {
        return this.addresses.remove(address);
    }

    public boolean findAddress(String address) {
        return this.addresses.contains(address);
    }

    public boolean addBranch(String branch) {
        return this.branches.putIfAbsent(branch, new RoutingNode()) == null;
    }

    public boolean removeBranch(String branch) {
        return this.branches.remove(branch) != null;
    }
}
