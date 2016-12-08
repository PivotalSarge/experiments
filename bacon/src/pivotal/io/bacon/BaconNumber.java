package pivotal.io.bacon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Created by mdodge on 07/12/2016.
 */
public class BaconNumber {
    ActorDatabase actorDatabase;

    MovieDatabase movieDatabase;

    Actor one;

    Actor two;

    Set examined = new HashSet();

    Queue<BaconPath> working = new LinkedList<BaconPath>();

    List<BaconPath> baconPaths = new LinkedList<BaconPath>();

    public BaconNumber(ActorDatabase actorDatabase, MovieDatabase movieDatabase, String one, String two) {
        this.actorDatabase = actorDatabase;
        this.movieDatabase = movieDatabase;
        this.one = this.actorDatabase.get(one);
        this.two = this.actorDatabase.get(two);
        working.add(new BaconPath(this.one));
        while (!working.isEmpty()) {
            doOne();
        }
    }

    void doOne() {
        BaconPath baconPath = working.remove();
        Actor next = baconPath.getLast();
        System.out.println(next + (examined.contains(next) ? " has " : " has NOT ") + "been examined");
        if (!examined.contains(next)) {
            examined.add(next);

            System.out.println(next.getMovies().size());
            for (Movie movie : next.getMovies()) {
                for (Actor actor : movie.getActors()) {
                    if (!examined.contains(actor)) {
                        BaconPath candidate = create(baconPath, movie, actor);
                        if (one.equals(candidate.getFirst()) && two.equals(candidate.getLast())) {
                            if (!baconPaths.isEmpty() && candidate.size() < baconPaths.get(0).size()) {
                                baconPaths.clear();
                            }

                            if (baconPaths.isEmpty() || candidate.size() == baconPaths.get(0).size()) {
                                baconPaths.add(candidate);
                            }
                        }
                    }
                }
            }
        }
    }

    BaconPath create(BaconPath baconPath, Movie movie, Actor actor) {
        try {
            BaconPath candidate = (BaconPath) baconPath.clone();
            candidate.addAssociation(movie, actor);
            return candidate;
        } catch (CloneNotSupportedException cnse) {
            System.err.println(cnse.getMessage());
            cnse.printStackTrace();
        }
        return null;
    }

    public int cardinality() {
        if (!baconPaths.isEmpty()) {
            return baconPaths.get(0).size();
        }
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        String caption = "From " + one + " to " + two;
        builder.append(caption);
        builder.append("\n");
        char[] chars = new char[caption.length()];
        Arrays.fill(chars, '=');
        builder.append(new String(chars));
        builder.append("\n");
        builder.append("Bacon number: " + cardinality());
        for (BaconPath baconPath : baconPaths) {
            builder.append("\n");
            builder.append(baconPath.toString());
        }

        return builder.toString();
    }
}
