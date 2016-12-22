package io.pivotal;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.startServer();

        final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            final String commandLine = console.readLine();
            System.out.println("You typed: " + commandLine);
        }
    }
}
