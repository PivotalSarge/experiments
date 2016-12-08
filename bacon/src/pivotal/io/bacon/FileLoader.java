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
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches() && 2 <= matcher.groupCount()) {
                    String name = nameNormalizer.normalize(matcher.group(1));
                    String title = titleNormalizer.normalize(matcher.group(2));
                    // Restrict to movies.
                    if (!title.isEmpty() && '"' != title.charAt(0)) {
                        if (name != null && !name.isEmpty()) {
                            actor = actorDatabase.get(name);
                        }
                        Movie movie = movieDatabase.get(title);
                        if (actor != null && movie != null) {
                            movie.addActor(actor);
                            actor.addMovie(movie);
//                            if (1 < movie.getActors().size()) {
//                                System.out.println(movie + ": " + movie.getActors());
//                            }
                        }
                    }
                }
            }
        } catch (IOException io) {
            System.err.println(io.getMessage());
            io.printStackTrace();
        }

    }
}
