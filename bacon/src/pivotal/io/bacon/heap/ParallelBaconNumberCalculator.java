package pivotal.io.bacon.heap;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pivotal.io.bacon.BaconNumber;
import pivotal.io.bacon.BaconPath;

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
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ie) {
                // NOP
            }
        }
        try {
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            // NOP
        } finally {
            pool.shutdownNow();
        }
    }

    class Task implements Runnable {
        BaconNumber baconNumber;

        BaconPath baconPath;

        Task(BaconNumber baconNumber, BaconPath baconPath) {
            this.baconNumber = baconNumber; this.baconPath = baconPath;
        }

        public void run() {
            List<BaconPath> candidates = analyzeCandidate(baconNumber, baconPath);
            for (BaconPath candidate : candidates) {
                pool.execute(new Task(baconNumber, candidate));
            }
        }
    }
}
