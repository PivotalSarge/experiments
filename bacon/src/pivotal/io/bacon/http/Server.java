package pivotal.io.bacon.http;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    private HttpServer httpServer;

    public Server(int port) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 5);
        httpServer.createContext("/bacon", new BaconHandler());
        httpServer.createContext("/", new MinimalHandler());
        httpServer.setExecutor(null); // creates a default executor
    }

    public void start() {
        httpServer.start();
    }
};
