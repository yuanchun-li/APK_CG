import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Util {
    public static final Logger LOGGER = Logger.getLogger("APK_CG");
    public static final String UNKNOWN = "<unknown>";

    public static String getTimeString() {
        long timeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
        Date date = new Date(timeMillis);
        return sdf.format(date);
    }

    public static void logException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        Util.LOGGER.warning(sw.toString());
    }

    public static float safeDivide(int obfuscated, int total) {
        if (total <= 0) return 1;
        return (float) obfuscated / total;
    }

    public static CallGraph generateCG() {
        PackManager.v().runPacks();
        return Scene.v().getCallGraph();
    }

    public static void printCG(CallGraph cg, PrintStream ps) {
        Iterator<Edge> edgeItr = cg.iterator();
        List<String> edgeList = new ArrayList<>();

        while(edgeItr.hasNext()){
            Edge edge = edgeItr.next();

            SootMethod srcMethod = edge.getSrc().method();
            String srcMethodDeclaration = srcMethod.getDeclaringClass().toString() + "." +
                    srcMethod.getName() +
                    srcMethod.getParameterTypes().toString()
                            .replace('[', '(').replace(']', ')');

            SootMethod tgtMethod = edge.getTgt().method();
            String tgtMethodDeclaration = tgtMethod.getDeclaringClass().toString() + "." +
                    tgtMethod.getName() +
                    tgtMethod.getParameterTypes().toString()
                            .replace('[', '(').replace(']', ')');

            edgeList.add(srcMethodDeclaration + " => " + tgtMethodDeclaration);
            //System.out.println(srcMethodDeclaration + " => " + tgtMethodDeclaration);
        }
        //System.out.println(applicationCallGraph.size());
        for (String edgeStr : edgeList){
            ps.println(edgeStr);
        }
    }
}
