package pivotal.io.bacon;

/**
 * Created by mdodge on 08/12/2016.
 */
public interface ActorDatabase {
    /**
     * Gets the actor for the name from the database.
     * Creates the actor in the database if necessary.
     *
     * @param name Actor name.
     * @return Actor.
     */
    public Actor get(String name);
}
