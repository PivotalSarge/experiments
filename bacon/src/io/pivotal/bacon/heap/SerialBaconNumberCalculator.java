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
    Set<Actor> examined = Collections.synchronizedSet(new HashSet<Actor>());

    io.pivotal.bacon.BaconPathQueue working = new BaconPathQueue();

    List<BaconPath> matches = Collections.synchronizedList(new LinkedList<BaconPath>());

    boolean done = false;

    public SerialBaconNumberCalculator() {
        super(new ActorDatabase(), new MovieDatabase());

        FileLoader fileLoader = new FileLoader();
        fileLoader.load("/Users/mdodge/experiments/bacon/tiny.list", actorDatabase, movieDatabase);
//        System.out.println("actorDatabase.size()=" + actorDatabase.size());
//        System.out.println("movieDatabase.size()=" + movieDatabase.size());
    }

    protected synchronized void setDone() {
        done = true;
        notifyAll();
    }

    public void calculate(BaconNumber baconNumber) {
        done = false;

        working.enqueue(new BaconPath(baconNumber.getFirst()));

        while (!done) {
            if (calculateNext(baconNumber)) {
                setDone();
            }
        }

        update(baconNumber, matches);
    }

    protected boolean calculateNext(BaconNumber baconNumber) {
        BaconPath baconPath = working.dequeue();
        List<BaconPath> candidates = analyzeCandidate(baconNumber, baconPath);
        for (BaconPath candidate : candidates) {
            working.enqueue(candidate);
        }
        return (working.isEmpty() && candidates.isEmpty());
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
                        }
                    }
                }
            }
        }

//        System.out.println("working.isEmpty()=" + working.isEmpty() + "\tcandidates.size()=" + candidates.size());
        return candidates;
    }
}
