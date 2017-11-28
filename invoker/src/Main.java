public class Main extends ArgumentInvoker {
  public static void main(String[] args) {
    int status = 0;
    try {
      new Main().invoke(args);
    } catch (Exception e) {
      status = 1;
      e.printStackTrace(System.err);
    }
    System.exit(status);
  }

  public void none() {
    System.out.println("none()");
  }

  public void just_string(String s) {
    System.out.println("just_string(" + s + ")");
  }

  public void just_int(int i) {
    System.out.println("just_int(" + i + ")");
  }

  @Override
  protected String getDefaultMethodName() {
    return "none";
  }
}
