package pivotal.io.bacon.heap;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pivotal.io.bacon.Actor;
import pivotal.io.bacon.BaconNumber;
import pivotal.io.bacon.BaconNumberCalculator;
import pivotal.io.bacon.BaconPath;
import pivotal.io.bacon.FileLoader;
import pivotal.io.bacon.Movie;

/**
 * Created by mdodge on 14/12/2016.
 */
public class SerialBaconNumberCalculator extends BaconNumberCalculator {
    Set examined = Collections.synchronizedSet(new HashSet());

    List<BaconPath> working = Collections.synchronizedList(new LinkedList<BaconPath>());

    List<BaconPath> matches = Collections.synchronizedList(new LinkedList<BaconPath>());

    public SerialBaconNumberCalculator() {
        super(new ActorDatabase(), new MovieDatabase());

        FileLoader fileLoader = new FileLoader();
        fileLoader.load("/Users/mdodge/experiments/bacon/tiny.list", actorDatabase, movieDatabase);
    }

    public void calculate(BaconNumber baconNumber) {
        working.add(new BaconPath(baconNumber.getFirst()));
        while (!working.isEmpty()) {
            BaconPath baconPath = working.remove(0);
            List<BaconPath> candidates = analyzeCandidate(baconNumber, baconPath);
            working.addAll(candidates);
        }
    }

    protected List<BaconPath> analyzeCandidate(BaconNumber baconNumber, BaconPath baconPath) {
        List<BaconPath> candidates = new LinkedList<BaconPath>();

        //System.out.println("\n\nbaconPath=" + baconPath);
        Actor next = baconPath.getLast();
        //System.out.println("Next: " + next + (examined.contains(next) ? " has " : " has NOT ") + "been examined");
        if (!examined.contains(next)) {
            examined.add(next);
            //System.out.println("next.getMovies().size()=" + next.getMovies().size());
            for (Movie movie : next.getMovies()) {
                //System.out.println("1: " + movie + ":\t" + movie.getActors());
                for (Actor actor : movie.getActors()) {
                    //System.out.println(actor.getMovies().size() + "\t" + actor);
                    //System.out.println("Candidate: " + actor + (examined.contains(actor) ? " has " : " has NOT ") + "been examined");
                    if (!next.equals(actor)) {
                        BaconPath candidate = create(baconPath, movie, actor);
                        //System.out.println("candidate=" + candidate);
                        //System.out.println("two=" + two);
                        if (baconNumber.getFirst().equals(candidate.getFirst())
                                && baconNumber.getLast().equals(candidate.getLast())) {
                            //System.out.println("MATCH!");
                            if (!matches.isEmpty() && candidate.size() < matches.get(0).size()) {
                                matches.clear();
                            }

                            if (matches.isEmpty() || candidate.size() == matches.get(0).size()) {
                                matches.add(candidate);
                            }
                        } else {
                            candidates.add(candidate);
                            //System.out.println("working.size()=" + working.size());
                        }
                    }
                }
            }
        }

        return candidates;
    }
}
