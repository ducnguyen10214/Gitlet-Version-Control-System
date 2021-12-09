package gitlet;

import java.io.IOException;
import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Duc Nguyen
 */
public class Main {

    /** The path to file SYSTEM. */
    private static final File PATHTOSYSTEM = new File(
            System.getProperty("user.dir") + "/.gitlet/SYSTEM");

    /** Check if enough operands come with the command.
     * @param lengthRequired the length required for the command to work.
     * @param args the command + operands.
     * @return boolean. */
    private static boolean checkOperands(int lengthRequired, String... args) {
        return lengthRequired == args.length;
    }

    /** Handle the `init` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void initHelper(ControlSystem newSystem, String... args)
            throws IOException {
        if (checkOperands(1, args)) {
            newSystem.init();
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `add` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void addHelper(ControlSystem newSystem, String... args) {
        if (checkOperands(2, args) && args[1] != null) {
            newSystem.add(args[1]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `commit` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void commitHelper(ControlSystem newSystem, String... args) {
        if (checkOperands(2, args) && args[1] != null) {
            newSystem.commit(args[1], "");
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `rm` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void rmHelper(ControlSystem newSystem, String... args) {
        if (checkOperands(2, args) && args[1] != null) {
            newSystem.rm(args[1]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `log` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void logHelper(ControlSystem newSystem, String... args) {
        if (checkOperands(1, args)) {
            newSystem.log();
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `global-log` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void globalLogHelper(ControlSystem newSystem,
                                       String... args) {
        if (checkOperands(1, args)) {
            newSystem.globalLog();
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `find` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void findHelper(ControlSystem newSystem, String... args) {
        if (checkOperands(2, args) && args[1] != null) {
            newSystem.find(args[1]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `status` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void statusHelper(ControlSystem newSystem, String... args) {
        if (checkOperands(1, args)) {
            newSystem.status();
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `checkout` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void checkoutHelper(ControlSystem newSystem,
                                      String... args) {
        if (args.length <= 4 && args.length >= 2) {
            if ((args.length == 3 && args[1].equals("--")
                    && args[2] != null)
                    || (args.length == 4 && args[1] != null
                    && args[2].equals("--") && args[3] != null)
                    || (args.length == 2 && args[1] != null)) {
                newSystem.checkout(args);
                Utils.writeObject(PATHTOSYSTEM, newSystem);
            } else {
                System.out.println("Incorrect operands.");
            }
        }
    }

    /** Handle the `branch` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void branchHelper(ControlSystem newSystem, String... args)
            throws IOException {
        if (checkOperands(2, args) && args[1] != null) {
            newSystem.branch(args[1]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `rm-branch` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void rmBranchHelper(ControlSystem newSystem, String... args) {
        if (checkOperands(2, args) && args[1] != null) {
            newSystem.rmBranch(args[1]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `reset` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void resetHelper(ControlSystem newSystem, String... args) {
        if (checkOperands(2, args) && args[1] != null) {
            newSystem.reset(args[1]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Handle the `merge` command from Main.
     * @param newSystem the current system.
     * @param args the command + operands. */
    public static void mergeHelper(ControlSystem newSystem, String... args) {
        if (checkOperands(2, args) && args[1] != null) {
            newSystem.merge(args[1]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Helper function to handle remote-related commands.
     * @param newSystem the new system.
     * @param args command + operands. */
    public static void remoteHelper(ControlSystem newSystem, String... args)
            throws IOException {
        if (checkOperands(3, args)
                && args[0].equals("add-remote")) {
            newSystem.addRemote(args[1], args[2]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else if (checkOperands(2, args)
                && args[0].equals("rm-remote")) {
            newSystem.rmRemote(args[1]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else if (checkOperands(3, args)
                && args[0].equals("push")) {
            newSystem.push(args[1], args[2]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else if (checkOperands(3, args)
                && args[0].equals("fetch")) {
            newSystem.fetch(args[1], args[2]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        } else if  (checkOperands(3, args)
                && args[0].equals("pull")) {
            newSystem.pull(args[1], args[2]);
            Utils.writeObject(PATHTOSYSTEM, newSystem);
        }
    }

    /** Handle the case when the input is 0.
     * @param args the command + operands. */
    public static void emptyCommand(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
    }

    /** Handle the local-related commands.
     * @param newSystem the new system.
     * @param args command + operands.
     * @return boolean. */
    public static boolean switchLocal(ControlSystem newSystem, String... args)
            throws IOException {
        boolean match = false;
        switch (args[0]) {
        case "init":
            initHelper(newSystem, args);
            match = true;
            break;
        case "add":
            addHelper(newSystem, args);
            match = true;
            break;
        case "commit":
            commitHelper(newSystem, args);
            match = true;
            break;
        case "rm":
            rmHelper(newSystem, args);
            match = true;
            break;
        case "log":
            logHelper(newSystem, args);
            match = true;
            break;
        case "global-log":
            globalLogHelper(newSystem, args);
            match = true;
            break;
        case "find":
            findHelper(newSystem, args);
            match = true;
            break;
        case "status":
            statusHelper(newSystem, args);
            match = true;
            break;
        case "checkout":
            checkoutHelper(newSystem, args);
            match = true;
            break;
        case "branch":
            branchHelper(newSystem, args);
            match = true;
            break;
        case "rm-branch":
            rmBranchHelper(newSystem, args);
            match = true;
            break;
        case "reset":
            resetHelper(newSystem, args);
            match = true;
            break;
        case "merge":
            mergeHelper(newSystem, args);
            match = true;
            break;
        default:
        }
        return match;
    }

    /** Handle the remote-related commands.
     * @param newSystem the new system.
     * @param args the command + operands.
     * @return boolean. */
    public static boolean switchRemote(ControlSystem newSystem, String... args)
            throws IOException {
        boolean match = false;
        switch (args[0]) {
        case "add-remote":
            remoteHelper(newSystem, args);
            match = true;
            break;
        case "rm-remote":
            remoteHelper(newSystem, args);
            match = true;
            break;
        case "push":
            remoteHelper(newSystem, args);
            match = true;
            break;
        case "fetch":
            remoteHelper(newSystem, args);
            match = true;
            break;
        case "pull":
            remoteHelper(newSystem, args);
            match = true;
            break;
        default:
        }
        return match;
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        ControlSystem newSystem = new ControlSystem();
        emptyCommand(args);
        if (switchLocal(newSystem, args)) {
            System.exit(0);
        } else if (switchRemote(newSystem, args)) {
            System.exit(0);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }
}
