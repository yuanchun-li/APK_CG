import soot.SootClass;
import soot.SootMethod;

/**
 * Created by yzy on 15-11-23.
 */

public class Main {
    //TODO 1: check overrides when restoring vertexes;
    //DONE 2: filter training data and predicting data
    //DONE 3: generate mapping
    //DOING 4: evaluate with open source apps:
    // TODO: add more features (consider class annotations, method annotations, field reaching definitions etc)
    // FIXME: some different fields and methods are predicted to same name
    // for each open-sourced app, generate a debug version,
    // a release version and the mapping.txt corresponding to the release version

    public static void main(String[] args) {
        // write your code here
//        PrintStream os = System.out;

        if (!Config.parseArgs(args)) {
            return;
        }

        Config.init();

        for (SootClass cls : Config.applicationClasses) {
            for (SootMethod mth: cls.getMethods()){
                String DeclaringClass = mth.getDeclaringClass().toString();
                String MethodDeclaration = mth.getName().toString() +
                                           mth.getParameterTypes().toString()
                                              .replace('[', '(').replace(']', ')');
                System.out.println(DeclaringClass + '.' + MethodDeclaration);
            }
        }
    }
}
