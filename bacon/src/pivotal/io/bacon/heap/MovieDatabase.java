package pivotal.io.bacon.heap;

import java.util.Map;
import java.util.HashMap;

import pivotal.io.bacon.Movie;

/**
 * Created by mdodge on 08/12/2016.
 */
public class MovieDatabase implements pivotal.io.bacon.MovieDatabase {
    /**
     * Map from movie name to movie.
     */
    Map<String, Movie> movies = new HashMap<String, Movie>();

    /**
     * Determines the number of movies in the database.
     *
     * @return Number of movies in the database.
     */
    public int size() {
        return movies.size();
    }

    /**
     * Gets the movie for the title from the database.
     * Creates the movie in the database if necessary.
     *
     * @param title Movie title.
     * @return Movie.
     */
    public Movie get(String title) {
        if (!movies.containsKey(title)) {
            Movie movie = new Movie(title);
            movies.put(title, movie);
            return movie;
        }
        return movies.get(title);
    }
}
