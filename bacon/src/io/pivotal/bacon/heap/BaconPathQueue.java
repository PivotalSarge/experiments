package io.pivotal.bacon.heap;

import io.pivotal.bacon.BaconPath;
import io.pivotal.bacon.DoubleMapQueue;

/**
 * Created by mdodge on 14/12/2016.
 */
public class BaconPathQueue implements io.pivotal.bacon.BaconPathQueue {
    DoubleMapQueue<BaconPath> queue = new DoubleMapQueue<BaconPath>();

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
