package presto.privaid.instrument;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import presto.privaid.*;
import presto.privaid.firebase.ContentDictionaryManager;
import presto.privaid.firebase.Dictionary;
import presto.privaid.firebase.Dispatcher;
import presto.privaid.firebase.TestRandomizer;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(final String[] args) {
        Configs.apkPath = args[0];
        Logger.setVerbose(args[1]);
        Configs.instrumentSpecPath = args[2];
        Configs.sdkPlatformsPath = args[3];
        Configs.runtimeClsDir = args[4];
        String[] parts = Configs.apkPath.split("/");
        Configs.apkName = parts[parts.length - 1];
        Configs.appPackage = Configs.apkName.substring(0, Configs.apkName.length() - 4);
        System.out.println(Configs.appPackage);
        Path sootOutApkPath = Paths.get("sootOutput", Configs.apkName);

        String excludeLibPath = "excluded_packages.txt";
        try {
            Configs.excludePackages = Files.readAllLines(Paths.get(excludeLibPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.delete(sootOutApkPath);
        } catch (IOException ignored) {
        }

        try {
            Gson gson = new Gson();
            Map specs = gson.fromJson(new FileReader(Configs.instrumentSpecPath), Map.class);
            Configs.appInstrumentSpec = (Map) specs.get(Configs.appPackage);
            System.out.println("Instrumentation specs: " + Configs.appInstrumentSpec);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        runJTP(Configs.apkPath, Configs.sdkPlatformsPath);
    }

    // jimple transformation pack
    static void runJTP(final String apkPath, final String platformDir) {
        settings(apkPath, platformDir);

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myInstrumenter", new MySceneTransformer()));

        final String[] sootArgs = {
                "-w",
                "-process-multiple-dex",
                "-p", "jb", "stabilize-local-names:true",
                "-keep-line-number",
                "-allow-phantom-refs",};

        soot.Main.main(sootArgs);
    }

    static void settings(final String apkPath, final String platformDir) {
        // prefer Android APK files// -src-prec apk
        Options.v().set_src_prec(Options.src_prec_apk);
        // output as APK, too//-f J
        Options.v().set_output_format(Options.output_format_dex);

        // set Android platform jars and apk
        Options.v().set_android_jars(platformDir);
        Options.v().set_process_dir(Lists.newArrayList(apkPath));

        // load instrument helper class
        Options.v().set_prepend_classpath(true);
        Options.v().set_soot_classpath(Configs.runtimeClsDir);

        // sometimes using array to parse args does not work
        Options.v().set_whole_program(true); // -w
        Options.v().set_process_multiple_dex(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);
        Options.v().setPhaseOption("jb", "stabilize-local-names:true"); // "-p", "jb", "stabilize-local-names:true"

        loadInstrumentationClasses();
    }

    private static void loadInstrumentationClasses() {
        Scene.v().loadClass(BaseDispatcher.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(DatabaseController.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(EventInfo.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(IRandomize.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(IRandomize.class.getName() + "$1", SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(IStopService.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(Scheduler.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(SendImpl.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(SendJobService.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(SQLiteOpenHelperWrapper.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(StopServiceJob.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(Timer.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(Tracker.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(Utils.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(Work.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(Work.class.getName() + "$Type", SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(Work.class.getName() + "$WorkThread", SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(WorkThreadPoolExecutor.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(WorkThreadPoolExecutor.class.getName() + "$1", SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(WorkThreadPoolExecutor.class.getName() + "$2", SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(Dispatcher.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(Dictionary.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(ContentDictionaryManager.class.getName(), SootClass.HIERARCHY).setApplicationClass();
        Scene.v().loadClass(TestRandomizer.class.getName(), SootClass.HIERARCHY).setApplicationClass();
    }
}
