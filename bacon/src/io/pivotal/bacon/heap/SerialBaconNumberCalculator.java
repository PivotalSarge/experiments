package io.pivotal.bacon.heap;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.pivotal.bacon.Actor;
import io.pivotal.bacon.BaconNumber;
import io.pivotal.bacon.BaconNumberCalculator;
import io.pivotal.bacon.BaconPath;
import io.pivotal.bacon.FileLoader;
import io.pivotal.bacon.Movie;

/**
 * Created by mdodge on 14/12/2016.
 */
public class SerialBaconNumberCalculator extends BaconNumberCalculator {
    Set examined = Collections.synchronizedSet(new HashSet());

    io.pivotal.bacon.BaconPathQueue working = new BaconPathQueue();

    List<BaconPath> matches = Collections.synchronizedList(new LinkedList<BaconPath>());

    public SerialBaconNumberCalculator() {
        super(new ActorDatabase(), new MovieDatabase());

        FileLoader fileLoader = new FileLoader();
        fileLoader.load("/Users/mdodge/experiments/bacon/tiny.list", actorDatabase, movieDatabase);
//        System.out.println("actorDatabase.size()=" + actorDatabase.size());
//        System.out.println("movieDatabase.size()=" + movieDatabase.size());
    }

    public void calculate(BaconNumber baconNumber) {
        working.enqueue(new BaconPath(baconNumber.getFirst()));
        while (!working.isEmpty()) {
            BaconPath baconPath = working.dequeue();
            for (BaconPath candidate : analyzeCandidate(baconNumber, baconPath)) {
                working.enqueue(candidate);
            }
        }
        update(baconNumber, matches);
    }

    protected List<BaconPath> analyzeCandidate(BaconNumber baconNumber, BaconPath baconPath) {
        List<BaconPath> candidates = new LinkedList<BaconPath>();

//        System.out.println("\n\nbaconPath=" + baconPath);
        Actor next = baconPath.getLast();
//        System.out.println("Next: " + next + (examined.contains(next) ? " has " : " has NOT ") + "been examined");
        if (!examined.contains(next)) {
            examined.add(next);
//            System.out.println("next.getMovies().size()=" + next.getMovies().size());
            for (Movie movie : next.getMovies()) {
//                System.out.println("1: " + movie + ":\t" + movie.getActors());
                for (Actor actor : movie.getActors()) {
//                    System.out.println(actor.getMovies().size() + "\t" + actor);
//                    System.out.println("Candidate: " + actor + (examined.contains(actor) ? " has " : " has NOT ") + "been examined");
                    if (!next.equals(actor)) {
                        BaconPath candidate = create(baconPath, movie, actor);
//                        System.out.println("candidate=" + candidate);
//                        System.out.println("last=" + baconNumber.getLast());
                        if (baconNumber.getFirst().equals(candidate.getFirst())
                                && baconNumber.getLast().equals(candidate.getLast())) {
//                            System.out.println("MATCH!");
                            if (!matches.isEmpty() && candidate.size() < matches.get(0).size()) {
                                matches.clear();
                            }

                            if (matches.isEmpty() || candidate.size() == matches.get(0).size()) {
                                matches.add(candidate);
                            }
                        } else {
                            candidates.add(candidate);
//                            System.out.println("working.size()=" + working.size());
                        }
                    }
                }
            }
        }

        return candidates;
    }
}
