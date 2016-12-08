package pivotal.io.bacon;

/**
 * Created by mdodge on 07/12/2016.
 */
public class BaconNumber {
    Actor one;

    Actor two;

    public BaconNumber(String one, String two) {
        this.one = new Actor(one);
        //System.out.println("one=" + (this.one == null ? "null" : this.one.format()));
        this.two = new Actor(two);
        //System.out.println("two=" + (this.two == null ? "null" : this.two.format()));
    }

    public int cardinality() {
        return 0; // TODO SARGE
    }
}
