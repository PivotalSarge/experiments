package io.pivotal.bacon.heap;

import java.util.Map;
import java.util.HashMap;

import io.pivotal.bacon.Actor;

/**
 * Created by mdodge on 08/12/2016.
 */
public class ActorDatabase implements io.pivotal.bacon.ActorDatabase {
    /**
     * Map from actor name to actor.
     */
    Map<String, Actor> actors = new HashMap<String, Actor>();

    /**
     * Determines the number of actors in the database.
     *
     * @return Number of actors in the database.
     */
    public int size() {
        return actors.size();
    }

    /**
     * Gets the actor for the name from the database.
     * Creates the actor in the database if necessary.
     *
     * @param name Actor name.
     * @return Actor.
     */
    public Actor get(String name) {
        if (!actors.containsKey(name)) {
            Actor actor = new Actor(name);
            actors.put(name, actor);
            return actor;
        }
        return actors.get(name);
    }
}
