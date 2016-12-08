package pivotal.io.bacon;

/**
 * Created by mdodge on 08/12/2016.
 */
public interface MovieDatabase {
    /**
     * Gets the movie for the title from the database.
     * Creates the movie in the database if necessary.
     *
     * @param title Movie title.
     * @return Movie.
     */
    public Movie get(String title);
}
