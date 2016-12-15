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
        super(new ActorDatabase(), new MovieDatabase());

        FileLoader fileLoader = new FileLoader();
        fileLoader.load("/Users/mdodge/experiments/bacon/tiny.list", actorDatabase, movieDatabase);
    }

    public synchronized void calculate(BaconNumber baconNumber) {
        this.baconNumber = baconNumber;

        count.set(0);

        working.enqueue(new BaconPath(baconNumber.getFirst()));

        setUp();

        while (!working.isEmpty() || 0 < count.get()) {
            progress();
        }

        tearDown();

        update(baconNumber, matches);
    }

    protected void setUp() {
        // NOP
    }

    protected void progress() {
        // NOP
    }

    protected void tearDown() {
        // NOP
    }

    protected void calculateNext() {
        BaconPath baconPath = working.dequeue();
        if (baconPath != null) {
            count.incrementAndGet();

            for (BaconPath candidate : analyzeCandidates(baconPath)) {
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

    private List<BaconPath> analyzeCandidates(BaconPath baconPath) {
        List<BaconPath> candidates = new LinkedList<BaconPath>();

        Actor next = baconPath.getLast();
        if (!examined.contains(next)) {
            examined.add(next);
            for (Movie movie : next.getMovies()) {
                for (Actor actor : movie.getActors()) {
                    if (!next.equals(actor)) {
                        analyzeCandidate(candidates, create(baconPath, movie, actor));
                    }
                }
            }
        }

        return candidates;
    }

    private void analyzeCandidate(List<BaconPath> candidates, BaconPath candidate) {
        if (baconNumber.getFirst().equals(candidate.getFirst())
                && baconNumber.getLast().equals(candidate.getLast())) {
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
