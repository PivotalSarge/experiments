package pivotal.io.bacon;

import pivotal.io.bacon.http.Server;

import java.io.IOException;

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
        }
        else {
            NameNormalizer normalizer = new NameNormalizer();
            for (String arg : args) {
                arg = normalizer.normalize(arg);
                BaconNumber baconNumber = new BaconNumber("Bacon, Kevin", arg);
                System.out.println(arg + ": " + baconNumber.cardinality());
            }
        }
    }
}
