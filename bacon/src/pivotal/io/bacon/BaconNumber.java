package pivotal.io.bacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mdodge on 07/12/2016.
 */
public class BaconNumber {
    Actor first;

    Actor last;

    List<BaconPath> matches = new ArrayList<BaconPath>();

    BaconNumber(Actor first, Actor last) {
        this.first = first;
        this.last = last;
    }

    public Actor getFirst() {
        return first;
    }

    void setFirst(Actor first) {
        this.first = first;
    }

    public Actor getLast() {
        return last;
    }

    void setLast(Actor last) {
        this.last = last;
    }

    public int cardinality() {
        if (!matches.isEmpty()) {
            return matches.get(0).size() - 1;
        }
        return -1;
    }

    void setMatches(List<BaconPath> matches) {
        this.matches = matches;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        String caption = "From " + first + " to " + last;
        builder.append(caption);
        builder.append("\n");
        char[] chars = new char[caption.length()];
        Arrays.fill(chars, '=');
        builder.append(new String(chars));
        builder.append("\n");
        if (matches.isEmpty()) {
            builder.append("Bacon number: N/A");
        } else {
            builder.append("Bacon number: " + cardinality());
            for (BaconPath baconPath : matches) {
                builder.append("\n");
                builder.append(baconPath.toString());
            }
        }

        return builder.toString();
    }
}
