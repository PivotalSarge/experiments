package pivotal.io.bacon;

import java.io.IOException;

import pivotal.io.bacon.heap.ActorDatabase;
import pivotal.io.bacon.heap.MovieDatabase;
import pivotal.io.bacon.http.Server;

public class Main {
    public static void main(String[] args) {
        if (0 == args.length) {
            System.out.println("Starting server...");
            try {
                Server server = new Server(8000);
                server.start();
            } catch (IOException io) {
                io.printStackTrace();
            }
        } else {
            ActorDatabase actorDatabase = new ActorDatabase();
            MovieDatabase movieDatabase = new MovieDatabase();

            FileLoader fileLoader = new FileLoader();
            fileLoader.load("/Users/mdodge/experiments/bacon/tiny.list", actorDatabase, movieDatabase);

            NameNormalizer normalizer = new NameNormalizer();
            for (String arg : args) {
                System.out.println();
                arg = normalizer.normalize(arg);
                BaconNumber baconNumber = new BaconNumber(actorDatabase, movieDatabase, Actor.KEVIN_BACON, arg);
                System.out.println(baconNumber);
            }
        }
    }
}
