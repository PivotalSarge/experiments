import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;

public class Main extends MethodInvoker {
  public static void main(String[] args) {
    int status = 0;
    try {
      new Main().invoke(args);
    } catch (Throwable t) {
      status = 1;
      t.printStackTrace(System.err);
    }
    System.exit(status);
  }

  public void invoke(String[] args) throws Throwable {
    if (args.length < 1) {
      invoke(new InputStreamReader(System.in), this, () -> System.out.print("> "), (str) -> {
      });
    } else {
      final File file = new File(args[0]);
      if (file.exists() && !file.isDirectory()) {
        FilePrompterAndTranscriber filePrompterAndTranscriber =
            new FilePrompterAndTranscriber(file);
        invoke(new FileReader(file), this, filePrompterAndTranscriber, filePrompterAndTranscriber);
      } else {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
          if (arg.contains(" ") || arg.contains("\t")) {
            arg = arg.replace(" ", "\\ ");
            arg = arg.replace("\t", "\\\t");
          }
          if (0 < builder.length()) {
            builder.append(' ');
          }
          builder.append(arg);
        }
        invoke(new StringReader(builder.toString()), this);
      }
    }
  }

  public void quit() {
    System.exit(0);
  }

  public void none() {
    System.out.println("none()");
  }

  public void just_string(String s) {
    System.out.println("just_string(\"" + s + "\")");
  }

  public void just_int(int i) {
    System.out.println("just_int(" + i + ")");
  }

  private static class FilePrompterAndTranscriber implements Prompter, Transcriber {
    private final String path;
    private int line = 0;

    private FilePrompterAndTranscriber(File file) {
      path = file.getAbsolutePath();
    }

    public void prompt() {
      ++line;
    }

    public void transcribe(String str) {
      System.out.println("# File " + path + ", line " + line + ": " + str);
    }
  }
}
