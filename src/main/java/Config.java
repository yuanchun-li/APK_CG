import org.apache.commons.cli.*;
import soot.*;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.infoflow.InfoFlowAnalysis;
import soot.options.Options;

import java.io.*;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class Config {
    // nice2predict server url for predicting
    public static final String projectName = "APK_CG";

    // File path of android.jar which is forced to use by soot
    public static String forceAndroidJarPath = "";
    // Libraries' directory, to be added to soot classpath
    public static String codeDir = "";

    // Directory for result output
    // It should output a json format of training/predicting data
    public static String outputDir = "output";

    public static ArrayList<SootClass> applicationClasses;
    // @yzy
    // store call graph
    public static CallGraph applicationCallGraph;

    private static PrintStream resultPs;

    public static boolean parseArgs(String[] args) {
        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        Option quiet = new Option("quiet", "be extra quiet");
        Option debug = new Option("debug", "print debug information");
        Option output = Option.builder("o").argName("directory").required()
                .longOpt("output").hasArg().desc("path to output dir").build();
        Option input = Option.builder("i").argName("APK").required()
                .longOpt("input").hasArg().desc("path to target APK").build();
        Option sdk = Option.builder("sdk").argName("android.jar").required()
                .longOpt("android-sdk").hasArg().desc("path to android.jar").build();
//        Option sourceSinkOpt = Option.builder("source_sink").argName("SourcesAndSinks.txt").required()
//                .longOpt("source_sink").hasArg().desc("SourcesAndSinks.txt").build();

        options.addOption(quiet);
        options.addOption(debug);
        options.addOption(output);
        options.addOption(input);
        options.addOption(sdk);
//        options.addOption(sourceSinkOpt);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("debug")) Util.LOGGER.setLevel(Level.ALL);
            if (cmd.hasOption("quiet")) Util.LOGGER.setLevel(Level.WARNING);
            if (cmd.hasOption("i")) {
                Config.codeDir = cmd.getOptionValue("i");
                File codeDirFile = new File(Config.codeDir);
                if (!codeDirFile.exists()) {
                    throw new ParseException("Input file does not exist.");
                }
            }
            if (cmd.hasOption('o')) {
                Config.outputDir = cmd.getOptionValue('o');
                File workingDir = new File(String.format("%s/APK_CG_%s/",
                        Config.outputDir, Util.getTimeString()));

                Config.outputDir = workingDir.getPath();
                if (!workingDir.exists() && !workingDir.mkdirs()) {
                    throw new ParseException("Error generating output directory.");
                }
            }
            if (cmd.hasOption("sdk")) {
                Config.forceAndroidJarPath = cmd.getOptionValue("sdk");
                File sdkFile = new File(Config.forceAndroidJarPath);
                if (!sdkFile.exists()) {
                    throw new ParseException("Android jar does not exist.");
                }
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator(new Comparator<Option>() {
                @Override
                public int compare(Option o1, Option o2) {
                    return o1.getOpt().length() - o2.getOpt().length();
                }
            });
            formatter.printHelp(Config.projectName, options, true);
            return false;
        }
        return true;
    }

    public static void init() {
        Util.LOGGER.log(Level.INFO, "initializing...");

        File logFile = new File(String.format("%s/gen_cg.log", Config.outputDir));
        File resultFile = new File(String.format("%s/cg.txt", Config.outputDir));

        try {
            FileHandler fh = new FileHandler(logFile.getAbsolutePath());
            fh.setFormatter(new SimpleFormatter());
            Util.LOGGER.addHandler(fh);
            resultPs = new PrintStream(new FileOutputStream(resultFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        SetupApplication app = new SetupApplication(forceAndroidJarPath, codeDir);
        try{
            app.calculateSourcesSinksEntrypoints("SourcesAndSinks.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
        soot.G.reset();

        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(codeDir));
        Options.v().set_force_android_jar(forceAndroidJarPath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);

        Options.v().set_no_bodies_for_excluded(true);

        Options.v().setPhaseOption("cg.spark", "on");
        Scene.v().loadNecessaryClasses();

        applicationClasses = new ArrayList<>();
        for (SootClass cls : Scene.v().getApplicationClasses()) {
            applicationClasses.add(cls);
        }
        Collections.sort(applicationClasses, new Comparator<SootClass>() {
            @Override
            public int compare(SootClass o1, SootClass o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(
                        o1.getName(), o2.getName());
            }
        });

//        ArrayList<SootMethod> entries = new ArrayList<>();
//        for (SootClass appCls : applicationClasses) {
//            for (SootMethod appMethod : appCls.getMethods()) {
//                if (appMethod.isPublic() && appMethod.hasActiveBody()) {
//                    entries.add(appMethod);
//                }
//            }
//        }
        AndroidEntryPointCreator epCreator = app.getEntryPointCreator();
        SootMethod entryPoint = epCreator.createDummyMain();
        Options.v().set_main_class(entryPoint.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));

        Util.LOGGER.info("initialization finished...");
        Util.LOGGER.info(String.format("[input]%s, [output]%s", Config.codeDir, Config.outputDir));
    }

    public static PrintStream getResultPs() {
        if (resultPs == null) {
            Util.LOGGER.warning("result printer is null, use stdout instead.");
            return System.out;
        }
        return resultPs;
    }
}