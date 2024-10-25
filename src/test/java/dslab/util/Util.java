package dslab.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class Util {

    private static boolean isTcpPortClosed(int port) {
        try {

            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(false);
            socket.connect(new InetSocketAddress(Constants.LOCALHOST, port));
            boolean connected = socket.finishConnect();
            socket.close();

            return !connected;
        } catch (IOException e) {
            return true;
        }
    }

    public static void waitForTcpPortsToClose(int... ports) {
        for (int port : ports) {
            await()
                    .pollInterval(5, TimeUnit.MILLISECONDS)
                    .atMost(1, TimeUnit.SECONDS)
                    .until(() -> Util.isTcpPortClosed(port));
        }
    }
}
