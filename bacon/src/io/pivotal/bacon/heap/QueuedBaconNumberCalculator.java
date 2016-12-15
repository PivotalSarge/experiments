package io.pivotal.bacon.heap;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.pivotal.bacon.Actor;
import io.pivotal.bacon.BaconNumber;
import io.pivotal.bacon.BaconNumberCalculator;
import io.pivotal.bacon.BaconPath;
import io.pivotal.bacon.FileLoader;
import io.pivotal.bacon.Movie;

/**
 * Created by mdodge on 15/12/2016.
 */
public class QueuedBaconNumberCalculator extends BaconNumberCalculator {
    protected final Object semaphore = new Object();

    protected io.pivotal.bacon.BaconPathQueue working = new BaconPathQueue();

    private final AtomicInteger count = new AtomicInteger();

    private BaconNumber baconNumber;

    private Set<Actor> examined = Collections.synchronizedSet(new HashSet<Actor>());

    private List<BaconPath> matches = Collections.synchronizedList(new LinkedList<BaconPath>());

    protected QueuedBaconNumberCalculator() {
        super(new io.pivotal.bacon.heap.ActorDatabase(), new io.pivotal.bacon.heap.MovieDatabase());

        FileLoader fileLoader = new FileLoader();
        fileLoader.load("/Users/mdodge/experiments/bacon/tiny.list", actorDatabase, movieDatabase);
    }

    public synchronized void calculate(BaconNumber baconNumber) {
        count.set(0);

        working.enqueue(new BaconPath(baconNumber.getFirst()));

        setUp(baconNumber);

        while (!working.isEmpty() || 0 < count.get()) {
//          System.out.println("0: isEmpty=" + working.isEmpty() + "\tcount=" + count.get());
            progress(baconNumber);
//          System.out.println("1: isEmpty=" + working.isEmpty() + "\tcount=" + count.get());
        }

        tearDown();

        update(baconNumber, matches);
    }

    protected void setUp(BaconNumber baconNumber) {
        // NOP
    }

    protected void progress(BaconNumber baconNumber) {
        // NOP
    }

    protected void tearDown() {
        // NOP
    }

    protected void calculateNext(BaconNumber baconNumber) {
        BaconPath baconPath = working.dequeue();
        if (baconPath != null) {
            count.incrementAndGet();

            List<BaconPath> candidates = analyzeCandidate(baconNumber, baconPath);
//            System.out.println("candidates.size()=" + candidates.size());
            for (BaconPath candidate : candidates) {
//                System.out.println("candidate=" + candidate);
                working.enqueue(candidate);
            }

            count.decrementAndGet();
            if (count.get() < 1) {
                synchronized (semaphore) {
                    semaphore.notifyAll();
                }
            }
        }
    }

    private List<BaconPath> analyzeCandidate(BaconNumber baconNumber, BaconPath baconPath) {
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
