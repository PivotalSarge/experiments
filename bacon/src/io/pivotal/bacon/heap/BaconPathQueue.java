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
            queue.offer(baconPath, 10, TimeUnit.MILLISECONDS);
//        System.out.println("enqueue(): queue.size()=" + queue.size());
        }
        catch (InterruptedException ie) {
            ie.printStackTrace(System.err);
        }
    }

    public BaconPath dequeue() {
        try {
//        System.out.println("dequeue(): queue.size()=" + queue.size());
            return queue.poll(10, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ie) {
            ie.printStackTrace(System.err);
        }
        return null;
    }
}
