package io.pivotal.bacon.heap;

import java.util.concurrent.LinkedBlockingQueue;

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
        return queue.isEmpty();
    }

    public void enqueue(BaconPath baconPath) {
        queue.offer(baconPath);
    }

    public BaconPath dequeue() {
        for (int i = 0; i < 5; ++i) {
            try {
                return queue.take();
            } catch (InterruptedException ie) {
                // NOP
            }
        }
        return null;
    }
}
