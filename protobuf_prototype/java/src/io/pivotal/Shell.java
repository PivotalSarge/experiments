package io.pivotal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mdodge on 27.12.16.
 */
public class Shell implements Server.Listener {
    public Shell() {
        // NOP
    }

    public void runCommandLoop(Server server) {
        server.addListener(this);

        try {
            final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                printPrompt(System.out);
                final List<String> arguments = splitCommandLine(console.readLine());
                if (!arguments.isEmpty()) {
                    final String command = arguments.get(0);
                    if (command.equalsIgnoreCase("quit")) {
                        break;
                    } else if (command.equalsIgnoreCase("put")) {
                        if (!arguments.get(1).isEmpty() && 1 < arguments.size() && !arguments.get(2).isEmpty()) {
                            System.out.println();
                            System.out.println(arguments.get(2) + "=" + arguments.get(3));

                            server.put(arguments.get(1), arguments.get(2), arguments.get(3));
                        }
                    } else if (command.equalsIgnoreCase("get")) {
                        if (!arguments.get(1).isEmpty() && 1 < arguments.size() && !arguments.get(2).isEmpty()) {
                            final String value = server.get(arguments.get(1), arguments.get(2));

                            System.out.println();
                            System.out.println(arguments.get(2) + "=" + value);
                        }
                    } else if (command.equalsIgnoreCase("invalidate")) {
                        if (!arguments.get(1).isEmpty() && 1 < arguments.size() && !arguments.get(2).isEmpty()) {
                            server.invalidate(arguments.get(1), arguments.get(2));
                        }
                    } else if (command.equalsIgnoreCase("destroy")) {
                        if (!arguments.get(1).isEmpty() && 1 < arguments.size() && !arguments.get(2).isEmpty()) {
                            server.destroy(arguments.get(1), arguments.get(2));
                        }
                    } else if (command.equalsIgnoreCase("dump")) {
                        if (!arguments.get(1).isEmpty()) {
                            server.dump(arguments.get(1));
                        }
                    } else {
                        if (!command.equalsIgnoreCase("help")) {
                            System.err.println("Unknown command: " + command);
                            printHelp(System.err);
                        } else {
                            printHelp(System.out);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.removeListener(this);
    }

    private void printPrompt(PrintStream stream) {
        stream.println();
        stream.print("> ");
    }

    private void printHelp(PrintStream stream) {
        stream.println("Valid commands:");
        stream.println("\thelp                       -- print this help message");
        stream.println("\tquit                       -- exit");
        stream.println("\tget <region> <key>         -- get the value for a region");
        stream.println("\tput <region> <key> <value> -- put the value for a region");
        stream.println("\tinvalidate <region> <key>  -- invalidate the region");
        stream.println("\tdestroy <region> <key>     -- destroy the region");
        stream.println("\tdump <region>              -- destroy the region");
    }

    private List<String> splitCommandLine(String commandLine) {
        List<String> arguments = new ArrayList<String>();
        Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(commandLine);
        while (matcher.find()) {
            arguments.add(matcher.group(1).replace("\"", ""));
        }
        return arguments;
    }

    public void regionChanged(String region) {
        printPrompt(System.out);
    }
}
