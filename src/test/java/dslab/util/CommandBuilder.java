package dslab.util;

public class CommandBuilder {

    public static String exchange(String type, String name) {
        return String.format("exchange %s %s", type, name);
    }

    public static String queue(String name) {
        return String.format("queue %s", name);
    }

    public static String bind(String bindingKey) {
        return String.format("bind %s", bindingKey);
    }

    public static String publish(String routingKey, String message) {
        return String.format("publish %s %s", routingKey, message);
    }

    public static String subscribe() {
        return "subscribe";
    }

    public static String exit() {
        return "exit";
    }

    public static String resolve(String name) {
        return "resolve %s".formatted(name);
    }

    public static String unregister(String name) {
        return "unregister %s".formatted(name);
    }

    public static String register(String name, String ipPort) {
        return "register %s %s".formatted(name, ipPort);
    }
}
