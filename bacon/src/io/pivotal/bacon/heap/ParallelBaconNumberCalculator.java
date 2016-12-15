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
    static final int NUMBER_OF_THREADS = 16;

    public ParallelBaconNumberCalculator() {
        // NOP
    }

    public void calculate(BaconNumber baconNumber) {
        done = false;

        ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        for (int i = 0; i < NUMBER_OF_THREADS; ++i) {
            pool.execute(new Executor(baconNumber));
        }

        working.enqueue(new BaconPath(baconNumber.getFirst()));

        while (!done) {
            try {
                wait(10);
            }
            catch (Exception e) {
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

        update(baconNumber, matches);
    }

    class Executor implements Runnable {
        BaconNumber baconNumber;

        Executor(BaconNumber baconNumber) {
            this.baconNumber = baconNumber;
        }

        public void run() {
            if (calculateNext(baconNumber)) {
                setDone();
            }
        }
    }
}
