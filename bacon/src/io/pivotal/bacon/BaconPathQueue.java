package io.pivotal.bacon;

/**
 * Created by mdodge on 14/12/2016.
 */
public interface BaconPathQueue {
    public void enqueue(BaconPath baconPath);

    public BaconPath dequeue();
}
