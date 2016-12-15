package io.pivotal.bacon.heap;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.pivotal.bacon.BaconNumber;
import io.pivotal.bacon.BaconPath;

/**
 * Created by mdodge on 14/12/2016.
 */
public class ParallelBaconNumberCalculator extends SerialBaconNumberCalculator {
    ExecutorService pool = Executors.newFixedThreadPool(16);

    public ParallelBaconNumberCalculator() {
        // NOP
    }

    public void calculate(BaconNumber baconNumber) {
        pool.execute(new Task(baconNumber, new BaconPath(baconNumber.getFirst())));
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

    class Task implements Runnable {
        BaconNumber baconNumber;

        BaconPath baconPath;

        Task(BaconNumber baconNumber, BaconPath baconPath) {
//            System.out.println("first=" + baconNumber.getFirst());
//            System.out.println("last=" + baconNumber.getLast());
            this.baconNumber = baconNumber;
//            System.out.println("baconPath=" + baconPath);
            this.baconPath = baconPath;
        }

        public void run() {
            List<BaconPath> candidates = analyzeCandidate(baconNumber, baconPath);
            for (BaconPath candidate : candidates) {
                pool.execute(new Task(baconNumber, candidate));
            }
        }
    }
}
