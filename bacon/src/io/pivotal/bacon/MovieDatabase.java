package io.pivotal.bacon;

/**
 * Created by mdodge on 08/12/2016.
 */
public interface MovieDatabase {
    /**
     * Determines the number of movies in the database.
     *
     * @return Number of movies in the database.
     */
    public int size();

    /**
     * Gets the movie for the title from the database.
     * Creates the movie in the database if necessary.
     *
     * @param title Movie title.
     * @return Movie.
     */
    public Movie get(String title);
}
