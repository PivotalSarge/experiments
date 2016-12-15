package io.pivotal.bacon.heap;

import java.util.ArrayList;
import java.util.List;

import io.pivotal.bacon.BaconNumber;
import io.pivotal.bacon.BaconPath;
import io.pivotal.bacon.FileLoader;

/**
 * Created by mdodge on 14/12/2016.
 */
public class ParallelBaconNumberCalculator extends QueuedBaconNumberCalculator {
    static final int NUMBER_OF_THREADS = 8;

    final List<Executor> executors = new ArrayList<Executor>();

    public ParallelBaconNumberCalculator() {
        // NOP
    }

    protected void setUp() {
        executors.clear();
        for (int i = 0; i < NUMBER_OF_THREADS; ++i) {
            Executor executor = new Executor("Executor " + i);
            executors.add(executor);
            executor.start();
        }
    }

    protected void progress() {
        synchronized (semaphore) {
            try {
                semaphore.wait(1000);
            } catch (Exception e) {
                // NOP
            }
        }
    }

    protected void tearDown() {
        for (Executor executor : executors) {
            executor.done = true;
            executor.interrupt();
        }
    }

    class Executor extends Thread {
        final String name;

        volatile boolean done = false;

        Executor(String name) {
            this.name = name;
        }

        public void run() {
//            System.out.println("START  " + name);
            while (!done) {
//                System.out.println("TAKE   " + name);
                calculateNext();
                synchronized (working) {
                    try {
                        working.wait(10);
                    } catch (InterruptedException ie) {
                        // NOP
                    }
                }
            }
//            System.out.println("FINISH " + name);
        }
    }
}
