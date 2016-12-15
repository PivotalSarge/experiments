package io.pivotal.bacon;

/**
 * Created by mdodge on 14/12/2016.
 */
public interface BaconPathQueue {
    boolean isEmpty();

    void enqueue(BaconPath baconPath);

    BaconPath dequeue();
}
