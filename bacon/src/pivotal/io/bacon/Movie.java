package pivotal.io.bacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mdodge on 08/12/2016.
 */
public class Movie {
    String title;

    List<Actor> actors;

    public Movie(String title) {
        this.title = title;
        this.actors = new ArrayList<Actor>();
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void addActor(Actor actor) {
        if (!actors.contains(actor)) {
            actors.add(actor);
        }
    }

    public String toString() {
        return title;
    }
}
