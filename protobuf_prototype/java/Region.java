import java.util.HashMap;
import java.util.Map;

public class Region {
  private String name;

  private Map<String, String> values;

  public Region(String name)
  {
    this.name = name;
    values = new HashMap<String, String>();
  }

  public String getName()
  {
    return name;
  }

  public boolean containsKey(String key)
  {
    return values.containsKey(key);
  }

  public String get(String key)
  {
    return values.get(key);
  }

  public void put(String key, String value)
  {
    values.put(key, value);
  }

  public void invalidate(String key)
  {
    values.replace(key, null);
  }

  public void destroy(String key)
  {
    values.remove(key);
  }

  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(name);
    builder.append(':');
    // values.forEach((k, v) -> builder.append(k + "=" + v + "\n"));
    boolean first = true;
    for (Map.Entry<String, String> entry : values.entrySet()) {
      if (first) {
        first = false;
      }
      else {
        builder.append(',');
      }
      builder.append(' ');
      builder.append(entry.getKey());
      builder.append('=');
      builder.append(entry.getValue());
    }
    return builder.toString();
  }
}
