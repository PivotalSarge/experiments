package pivotal.io.bacon.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import pivotal.io.bacon.Actor;
import pivotal.io.bacon.ActorDatabase;
import pivotal.io.bacon.BaconNumber;
import pivotal.io.bacon.FileLoader;
import pivotal.io.bacon.MovieDatabase;
import pivotal.io.bacon.NameNormalizer;

//import java.io.InputStream;

/**
 * Created by mdodge on 07/12/2016.
 */
public class BaconHandler implements HttpHandler {
    ActorDatabase actorDatabase;

    MovieDatabase movieDatabase;

    BaconHandler(String path) {
        pivotal.io.bacon.heap.ActorDatabase actorDatabase = new pivotal.io.bacon.heap.ActorDatabase();
        pivotal.io.bacon.heap.MovieDatabase movieDatabase = new pivotal.io.bacon.heap.MovieDatabase();
        NameNormalizer normalizer = new NameNormalizer();
        FileLoader fileLoader = new FileLoader();
        fileLoader.load("tiny.list", actorDatabase, movieDatabase);
    }

    public void handle(HttpExchange t) throws IOException {
        String response = new String();

        // Read the request body? InputStream is = t.getRequestBody();
        // String path = t.getHttpContext().getPath();
        Map<String, String> query = new HashMap<String, String>();
        URI uri = t.getRequestURI();
        String[] parts = uri.getQuery().split("&");
        if (query.containsKey("name")) {
            response = "length=" + parts.length;
            for (String part : parts) {
                response += "\n" + part;
                String[] tuple = part.split("=");
                if (1 < tuple.length) {
                    query.put(tuple[0], tuple[1]);
                }
            }
            response += "\nname=";
            response += query.get("name");
            BaconNumber baconNumber = new BaconNumber(actorDatabase, movieDatabase, Actor.KEVIN_BACON, query.get("name"));
            response += "\ncardinality=" + baconNumber.cardinality();
        } else {
            response = "<html><body><strong>No name provided.</strong></body></html>";
        }
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
