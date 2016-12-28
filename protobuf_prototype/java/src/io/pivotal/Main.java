package io.pivotal;

public class Main {
  public static void main(String[] args) throws Exception {
    Server server = new Server();
    server.start();

    Shell shell = new Shell();
    shell.runCommandLoop(server);

    server.stop();
  }
}
