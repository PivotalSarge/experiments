package io.pivotal.bacon.heap;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.pivotal.bacon.BaconPath;

/**
 * Created by mdodge on 14/12/2016.
 */
public class BaconPathQueue implements io.pivotal.bacon.BaconPathQueue {
    LinkedBlockingQueue<BaconPath> queue = new LinkedBlockingQueue<BaconPath>();

    public BaconPathQueue() {
        // NOP
    }

    public boolean isEmpty() {
//        System.out.println("isEmpty(): queue.size()=" + queue.size());
        return queue.isEmpty();
    }

    public void enqueue(BaconPath baconPath) {
        try {
            queue.put(baconPath);
        } catch (InterruptedException ie) {
            // NOP
        }
    }

    public BaconPath dequeue() {
        try {
            return queue.take();
        } catch (InterruptedException ie) {
            // NOP
        }
        return null;
    }
}
