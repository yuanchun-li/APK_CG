import java.util.Collections;

import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class Main {
    public final static String jarPath = "/home/yzy/Android/Sdk/platforms/";

    public final static String apk = "/home/yzy/work/2015_autumn/dynamic_trace/app-release.apk";

    public static void main(String[] args){
        SetupApplication app = new SetupApplication(jarPath, apk);
        try{
            app.calculateSourcesSinksEntrypoints("/home/yzy/work/2016_spring/cgGenerator/lib/SourcesAndSinks.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
        soot.G.reset();

        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(apk));
        Options.v().set_force_android_jar("/home/yzy/Android/Sdk/platforms/android-23/android.jar");
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().setPhaseOption("cg.spark verbose:true", "on");
        Scene.v().loadNecessaryClasses();

        SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
        Options.v().set_main_class(entryPoint.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        PackManager.v().runPacks();

        CallGraph cg = Scene.v().getCallGraph();
    }
}