package io.pivotal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static List<String> splitCommandLine(String commandLine) {
        List<String> arguments = new ArrayList<String>();
        Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(commandLine);
        while (matcher.find()) {
            arguments.add(matcher.group(1).replace("\"", ""));
        }
        return arguments;
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.start();

        final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            final List<String> arguments = splitCommandLine(console.readLine());
            if (!arguments.isEmpty()) {
                final String command = arguments.get(0);
                if (command.equalsIgnoreCase("quit")) {
                    break;
                } else if (command.equalsIgnoreCase("put")) {
                    if (!arguments.get(1).isEmpty() && !arguments.get(2).isEmpty()) {
                        System.out.println();
                        System.out.println(arguments.get(2) + "=" + arguments.get(3));

                        server.put(arguments.get(1), arguments.get(2), arguments.get(3));
                    }
                } else if (command.equalsIgnoreCase("get")) {
                    if (!arguments.get(1).isEmpty() && !arguments.get(2).isEmpty()) {
                        final String value = server.get(arguments.get(1), arguments.get(2));

                        System.out.println();
                        System.out.println(arguments.get(2) + "=" + value);
                    }
                } else if (command.equalsIgnoreCase("invalidate")) {
                    if (!arguments.get(1).isEmpty() && !arguments.get(2).isEmpty()) {
                        server.invalidate(arguments.get(1), arguments.get(2));
                    }
                } else if (command.equalsIgnoreCase("destroy")) {
                    if (!arguments.get(1).isEmpty() && !arguments.get(2).isEmpty()) {
                        server.destroy(arguments.get(1), arguments.get(2));
                    }
                } else {
                    if (!command.equalsIgnoreCase("help")) {
                        System.err.println("Unknown command: " + command);
                    }
                    System.out.println("Valid commands:");
                    System.out.println("\thelp                       -- print this help message");
                    System.out.println("\tquit                       -- exit");
                    System.out.println("\tget <region> <key>         -- get the value for a arguments.get(2)");
                    System.out.println("\tput <region> <key> <value> -- put the value for a arguments.get(2)");
                    System.out.println("\tinvalidate <region> <key>  -- invalidate the arguments.get(2)");
                    System.out.println("\tdestroy <region> <key>     -- destroy the arguments.get(2)");
                }
            }
        }

        server.stop();
    }
}
