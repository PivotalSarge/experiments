package io.pivotal.bacon.heap;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.pivotal.bacon.Actor;
import io.pivotal.bacon.BaconNumber;
import io.pivotal.bacon.BaconNumberCalculator;
import io.pivotal.bacon.BaconPath;
import io.pivotal.bacon.FileLoader;
import io.pivotal.bacon.Movie;

/**
 * Created by mdodge on 14/12/2016.
 */
public class SerialBaconNumberCalculator extends QueuedBaconNumberCalculator {
    public SerialBaconNumberCalculator() {
        // NOP
    }

    protected void progress() {
        calculateNext();
    }
}
