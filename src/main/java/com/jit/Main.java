package com.jit;

public class Main {
    public static void main(String[] args) {
        try {
            Jit jit = new Jit();
            parseArguments(jit, args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void parseArguments(Jit jit, String[] args) throws Exception {
        if (args.length == 0) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "init":
                jit.init();
                break;
            case "add":
                handleAdd(jit, args);
                break;
            case "commit":
                handleCommit(jit, args);
                break;
            case "checkout":
                handleCheckout(jit, args);
                break;
            case "status":
                jit.status();
                break;
            default:
                printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("Jit - Simple Version Control System");
        System.out.println("Usage: jit <command> [options]\n");
        System.out.println("Commands:");
        System.out.println("  init            Initialize empty repository");
        System.out.println("  add <pattern>   Add files matching pattern");
        System.out.println("  commit <msg>    Commit staged changes");
        System.out.println("  checkout <id>   Checkout specific commit");
        System.out.println("  status          Show repository status");
    }

    private static void handleAdd(Jit jit, String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Error: Specify file pattern to add");
            return;
        }
        jit.add(args[1]);
    }

    private static void handleCommit(Jit jit, String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Error: Commit message required");
            return;
        }
        jit.commit(args[1]);
    }

    private static void handleCheckout(Jit jit, String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Error: Commit ID required");
            return;
        }
        jit.checkout(args[1]);
    }
}