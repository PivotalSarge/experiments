package io.pivotal.bacon;

import java.io.IOException;

import io.pivotal.bacon.http.Server;

public class Main {
    private static final String PREFIX = "io.pivotal.bacon.";

    public static void main(String[] args) {
        if (0 == args.length) {
            System.out.println("Starting server...");
            try {
                Server server = new Server(8000);
                server.start();
            } catch (IOException io) {
                io.printStackTrace(System.err);
            }
        } else {
            String className = System.getProperty("bacon.calculator", "heap.SerialBaconNumberCalculator");
            if (!className.startsWith(PREFIX)) {
                className = PREFIX + className;
            }
            try {
                Class<BaconNumberCalculator> clazz = (Class<BaconNumberCalculator>) Class.forName(className);
                if (clazz != null) {
                    BaconNumberCalculator calculator = clazz.newInstance();
                    if (calculator != null) {
                        for (String arg : args) {
                            System.out.println();
                            System.out.println(arg);
                            BaconNumber baconNumber = calculator.calculate(Actor.KEVIN_BACON, arg);
                            System.out.println(baconNumber);
                        }
                    } else {
                        System.err.println("No instance for " + className);
                    }
                } else {
                    System.err.println("No class for " + className);
                }
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
