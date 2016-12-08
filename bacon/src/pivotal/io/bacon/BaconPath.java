package pivotal.io.bacon;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Created by mdodge on 08/12/2016.
 */
public class BaconPath {
    Deque<Association> associations = new LinkedList<Association>();

    BaconPath() {
        // NOP
    }

    public BaconPath(Actor one) {
        associations.addLast(new Association(one));
    }

    public int size() {
        return associations.size();
    }

    public Actor getFirst() {
        if (!associations.isEmpty()) {
            return associations.getFirst().actor;
        }
        return null;
    }

    public Actor getLast() {
        if (!associations.isEmpty()) {
            return associations.getLast().actor;
        }
        return null;
    }

    public void addAssociation(Movie movie, Actor actor) {
        associations.addLast(new Association(movie, actor));
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Association association : associations) {
            if (association.movie == null) {
                builder.append(association.actor);
            } else {
                builder.append(" -> ");
                builder.append(association.movie);
                builder.append(" -> ");
                builder.append(association.actor);
            }
        }
        return builder.toString();
    }

    protected Object clone()
            throws CloneNotSupportedException {
        BaconPath other = new BaconPath();
        for (Association association : associations) {
            other.associations.addLast(association);
        }
        return other;
    }

    class Association {
        Movie movie;

        Actor actor;

        Association(Actor actor) {
            this.movie = null;
            this.actor = actor;
        }

        Association(Movie movie, Actor actor) {
            this.movie = movie;
            this.actor = actor;
        }
    }
}
