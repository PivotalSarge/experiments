package pivotal.io.bacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mdodge on 07/12/2016.
 */
public class Actor {
    public static final String KEVIN_BACON = "Bacon, Kevin (I)";

    String name;

    List<Movie> movies;

    public Actor(String name) {
        this.name = name;
        this.movies = new ArrayList<Movie>();
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void addMovie(Movie movie) {
        if (!movies.contains(movie)) {
            movies.add(movie);
        }
    }

    public String toString() {
        return name;
    }
}
