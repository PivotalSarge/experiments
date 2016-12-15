package io.pivotal.bacon;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Created by mdodge on 14/12/2016.
 */
public abstract class BaconNumberCalculator {
    protected ActorDatabase actorDatabase = null;

    protected MovieDatabase movieDatabase = null;

    NameNormalizer nameNormalizer = new NameNormalizer();

    protected BaconNumberCalculator(ActorDatabase actorDatabase,
                                    MovieDatabase movieDatabase) {
        this.actorDatabase = actorDatabase;
        this.movieDatabase = movieDatabase;
    }

    public final BaconNumber calculate(String first, String last) {
        BaconNumber baconNumber = new BaconNumber(actorDatabase.get(nameNormalizer.normalize(first)),
                actorDatabase.get(nameNormalizer.normalize(last)));
        Instant start = Instant.now();
        calculate(baconNumber);
        System.out.println(Duration.between(start, Instant.now()));
        return baconNumber;
    }

    protected abstract void calculate(BaconNumber baconNumber);

    protected void update(BaconNumber baconNumber, List<BaconPath> matches) {
        baconNumber.setMatches(matches);
    }

    protected BaconPath create(BaconPath baconPath, Movie movie, Actor actor) {
        try {
            BaconPath candidate = (BaconPath) baconPath.clone();
            candidate.addAssociation(movie, actor);
            return candidate;
        } catch (CloneNotSupportedException cnse) {
            System.err.println(cnse.getMessage());
            cnse.printStackTrace(System.err);
        }
        return null;
    }
}
