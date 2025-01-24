package com.jit;

public class Main {
    private static Jit jit = new Jit();

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printHelp();
                return;
            }

            switch (args[0]) {
                case "init":
                    jit.init();
                    break;
                case "add":
                    handleAdd(args);
                    break;
                case "commit":
                    handleCommit(args);
                    break;
                case "checkout":
                    handleCheckout(args);
                    break;
                case "status":
                    jit.status();
                    break;
                default:
                    printHelp();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("Jit - Simple Version Control System\n");
        System.out.println("Usage: jit <command> [options]\n");
        System.out.println("Commands:");
        System.out.println("  init           Initialize empty repository");
        System.out.println("  add <pattern>  Add files matching pattern");
        System.out.println("  commit <msg>   Commit staged changes");
        System.out.println("  checkout <id>  Checkout specific commit");
        System.out.println("  status         Show repository status");
    }

    private static void handleAdd(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Specify file pattern to add");
            return;
        }
        jit.add(args[1]);
    }

    private static void handleCommit(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Commit message required");
            return;
        }
        jit.commit(args[1]);
    }

    private static void handleCheckout(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Commit ID required");
            return;
        }
        jit.checkout(args[1]);
    }
}