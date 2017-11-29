import java.io.BufferedReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Queue;

public class ArgumentInvoker {
  private static void invoke(Queue<String> tokens, Object object, String name) throws Throwable {
    final Method method = findMethod(object.getClass(), name);
    if (method != null) {
      if (0 < method.getParameterCount()) {
        Object[] parameters = new Object[method.getParameterCount()];
        int index = 0;
        for (Type type : method.getParameterTypes()) {
          // Adjust the type to box primitive types.
          if (boolean.class == type) {
            type = Boolean.class;
          } else if (byte.class == type) {
            type = Byte.class;
          } else if (short.class == type) {
            type = Short.class;
          } else if (int.class == type) {
            type = Integer.class;
          } else if (long.class == type) {
            type = Long.class;
          } else if (float.class == type) {
            type = Float.class;
          } else if (double.class == type) {
            type = Double.class;
          }

          if (!tokens.isEmpty()) {
            final String parameter = tokens.remove();
            try {
              Constructor constructor = ((Class) type).getConstructor(String.class);
              parameters[index++] = constructor.newInstance(parameter);
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {
              e.printStackTrace(System.err);
              parameters[index++] = parameter;
            } catch (InvocationTargetException e) {
              throw e.getTargetException();
            }
          } else {
            parameters[index++] = null;
          }
        }
        try {
          method.invoke(object, parameters);
        } catch (IllegalAccessException e) {
          e.printStackTrace(System.err);
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        }
      } else {
        try {
          method.invoke(object);
        } catch (IllegalAccessException e) {
          e.printStackTrace(System.err);
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        }
      }
    }
  }

  private static Method findMethod(Class clazz, String name) {
    if (name != null) {
      for (Method method : clazz.getDeclaredMethods()) {
        if (name.equals(method.getName())) {
          return method;
        }
      }
    }
    return null;
  }

  protected void invoke(Reader reader, Object object)
      throws Throwable {
    final BufferedReader input = new BufferedReader(reader);
    String line;
    while (null != (line = input.readLine())) {
      Queue<String> tokens = new LinkedList<String>();
      String[] strings = line.split("(?<=[^\\\\])[ \t]");
      for (String string : strings) {
        string = string.replace("#.*", "");
        string = string.replace("\\ ", " ");
        string = string.replace("\\\t", "\t");
        tokens.add(string);
      }
      while (!tokens.isEmpty()) {
        final String name = tokens.remove();
        invoke(tokens, object, name);
      }
    }
  }
}
