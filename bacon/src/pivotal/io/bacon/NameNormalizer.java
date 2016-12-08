package pivotal.io.bacon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mdodge on 08/12/2016.
 */
public class NameNormalizer {
    final static Pattern noCommaPattern = Pattern.compile("([^, \\t]+)\\s+(.*?)(?:\\s+(\\([CDILMVX]+\\)))?");

    final static Pattern commaPattern = Pattern.compile("([^, \\t]+),\\s+(.*?)(?:\\s+(\\([CDILMVX]+\\)))?");

    public NameNormalizer() {
        // NOP
    }

    public String normalize(String str) {
        StringBuilder builder = new StringBuilder();

        Matcher matcher = noCommaPattern.matcher(str);
        if (matcher.matches()) {
            builder.append(matcher.group(2));
            builder.append(", ");
            builder.append(matcher.group(1));
            if (matcher.group(3) != null) {
                builder.append(" ");
                builder.append(matcher.group(3));
            }
        } else {
            matcher = commaPattern.matcher(str);
            if (matcher.matches()) {
                builder.append(matcher.group(1));
                builder.append(", ");
                builder.append(matcher.group(2));
                if (matcher.group(3) != null) {
                    builder.append(" ");
                    builder.append(matcher.group(3));
                }
            } else {
                builder.append(str);
            }
        }

        return builder.toString();
    }
}
