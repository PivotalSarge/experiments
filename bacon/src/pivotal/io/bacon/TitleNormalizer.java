package pivotal.io.bacon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mdodge on 08/12/2016.
 */
public class TitleNormalizer {
    final static Pattern pattern = Pattern.compile("(.*(?: \\(\\d+\\)?)).*");

    public TitleNormalizer() {
        // NOP
    }

    public String normalize(String str) {
        StringBuilder builder = new StringBuilder();

        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            builder.append(matcher.group(1));
        }

        return builder.toString();
    }
}
