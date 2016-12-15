package io.pivotal.bacon.heap;

import java.util.ArrayList;
import java.util.List;

import io.pivotal.bacon.BaconNumber;
import io.pivotal.bacon.BaconPath;

/**
 * Created by mdodge on 14/12/2016.
 */
public class ParallelBaconNumberCalculator extends SerialBaconNumberCalculator {
    static final int NUMBER_OF_THREADS = 8;

    public ParallelBaconNumberCalculator() {
        // NOP
    }

    @SuppressWarnings("deprecation")
    public void calculate(BaconNumber baconNumber) {
        clearCount();

        working.enqueue(new BaconPath(baconNumber.getFirst()));

        List<Executor> executors = new ArrayList<Executor>();
        for (int i = 0; i < NUMBER_OF_THREADS; ++i) {
            Executor executor = new Executor("Executor " + i, baconNumber);
            executors.add(executor);
            new Thread(executor).start();
        }

        synchronized (this) {
            while (!working.isEmpty() || 0 < count) {
//                System.out.println("0: isEmpty=" + working.isEmpty() + "\tcount=" + count);
                try {
                    wait(1000);
                } catch (Exception e) {
                    // NOP
                }
//                System.out.println("1: isEmpty=" + working.isEmpty() + "\tcount=" + count);
            }
        }

        for (Executor executor : executors) {
            executor.done = true;
        }

        update(baconNumber, matches);
    }

    class Executor implements Runnable {
        final String name;

        final BaconNumber baconNumber;

        volatile boolean done = false;

        Executor(String name, BaconNumber baconNumber) {
            this.name = name;
            this.baconNumber = baconNumber;
        }

        public void run() {
//            System.out.println("START  " + name);
            while (!done) {
//                System.out.println("TAKE   " + name);
                calculateNext(baconNumber);
                synchronized(working) {
                    try {
                        working.wait(10);
                    }
                    catch (InterruptedException ie) {
                        // NOP
                    }
                }
            }
//            System.out.println("FINISH " + name);
        }
    }
}
