package pivotal.io.bacon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mdodge on 08/12/2016.
 */
public class FileLoader {
    final static Pattern pattern = Pattern.compile("^([^\\t]*)[\\t]+(.*)");

    public void load(String path, ActorDatabase actorDatabase, MovieDatabase movieDatabase) {
        NameNormalizer nameNormalizer = new NameNormalizer();
        TitleNormalizer titleNormalizer = new TitleNormalizer();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            Actor actor = null;
            String line = null;
            while ((line = reader.readLine()) != null) {
                //System.out.println("actor=" + actor);
                //System.out.println(line);
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches() && 2 <= matcher.groupCount()) {
                    String name = nameNormalizer.normalize(matcher.group(1));
                    //System.out.println("name=" + name);
                    if (name != null && !name.isEmpty()) {
                        actor = actorDatabase.get(name);
                    }
                    // Restrict to movies.
                    if (!matcher.group(2).isEmpty()
                            && !matcher.group(2).contains("(TV)")
                            && !matcher.group(2).contains("(V)")) {
                        String title = titleNormalizer.normalize(matcher.group(2));
                        Movie movie = movieDatabase.get(title);
                        if (actor != null && movie != null) {
                            movie.addActor(actor);
                            actor.addMovie(movie);
                            //System.out.println(actor.getMovies().size() + "\t" + movie.getActors().size());
                        }
                        //System.out.println("0: " + movie + ":\t" + movie.getActors());
                    }
                    //else System.out.println(matcher.group(2) + " is a TV show");
                }
                else {
                    actor = null;
                }
            }
        } catch (IOException io) {
            System.err.println(io.getMessage());
            io.printStackTrace();
        }

    }
}
