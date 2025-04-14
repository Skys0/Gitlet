package gitlet;

import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Skyss7
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                GitletMethod.init();
                break;
            case "add":
                CheckIfInit();
                GitletMethod.add(args);
                break;
            case "rm":
                CheckIfInit();
                GitletMethod.rm(args);
                break;
            case "commit":
                CheckIfInit();
                GitletMethod.commit(args);
                break;
            case "log":
                CheckIfInit();
                GitletMethod.printLog();
                break;
            case "global-log":
                CheckIfInit();
                GitletMethod.printGlobalLog();
                break;
            case "find":
                CheckIfInit();
                GitletMethod.Find(args);
                break;
            case "status":
                CheckIfInit();
                GitletMethod.status();
                break;
            case "checkout":
                CheckIfInit();
                GitletMethod.checkout(args);
                break;
            case "branch":
                CheckIfInit();
                GitletMethod.CreateBranch(args);
                break;
            case "rm-branch":
                CheckIfInit();
                GitletMethod.rmBranch(args);
                break;
            case "reset":
                CheckIfInit();
                GitletMethod.reset(args);
                break;
            case "merge":
                CheckIfInit();
                GitletMethod.merge(args);
                break;
            default:
                Repository.NoCommand();
                break;
        }
    }
}
