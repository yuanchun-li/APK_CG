import soot.SootClass;
import soot.SootMethod;

/**
 * Created by yzy on 15-11-23.
 */

public class Main {

    public static void main(String[] args) {
        // write your code here
//        PrintStream os = System.out;

        if (!Config.parseArgs(args)) {
            return;
        }

        Config.init();

    }
}
