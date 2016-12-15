package io.pivotal.bacon.heap;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.pivotal.bacon.BaconNumber;
import io.pivotal.bacon.BaconPath;

/**
 * Created by mdodge on 15/12/2016.
 */
public class PooledBaconNumberCalculator extends SerialBaconNumberCalculator {
    static final int NUMBER_OF_THREADS = 16;

    ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public PooledBaconNumberCalculator() {
        // NOP
    }

    public void calculate(BaconNumber baconNumber) {
        if (pool == null) {
            pool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        }

        pool.execute(new Executor(baconNumber, new BaconPath(baconNumber.getFirst())));
        // Is it strictly accurate to add one for 'two'?
        while (examined.size() + 1 < actorDatabase.size()) {
            try {
//                System.out.println("Sleeping...");
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ie) {
                // NOP
            }
        }
//        System.out.println("Shutting down...");
        try {
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            // NOP
        } finally {
            pool.shutdownNow();
        }

        update(baconNumber, matches);
    }

    class Executor implements Runnable {
        BaconNumber baconNumber;

        BaconPath baconPath;

        Executor(BaconNumber baconNumber, BaconPath baconPath) {
//            System.out.println("first=" + baconNumber.getFirst());
//            System.out.println("last=" + baconNumber.getLast());
            this.baconNumber = baconNumber;
//            System.out.println("baconPath=" + baconPath);
            this.baconPath = baconPath;
        }

        public void run() {
            List<BaconPath> candidates = analyzeCandidate(baconNumber, baconPath);
            for (BaconPath candidate : candidates) {
                pool.execute(new Executor(baconNumber, candidate));
            }
        }
    }
}
