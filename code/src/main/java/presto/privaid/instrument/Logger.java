package presto.privaid.instrument;

public class Logger {
    private static boolean verb = false;
    private static final String TAG = Logger.class.getSimpleName();

    public static void setVerbose(String verbose) {
        verb = Boolean.parseBoolean(verbose);
    }

    public static void setVerbose(boolean verbose) {
        verb = verbose;
    }

    public static void verb(String tag, String msg) {
        if (verb) {
            System.out.println("[" + tag + "] " + ANSI.CYAN + "VERBOSE" + ANSI.RESET + " " + msg);
        }
    }

    public static void info(String tag, String msg) {
        System.out.println("[" + tag + "] " + ANSI.GREEN + "INFO" + ANSI.RESET + " " + msg);
    }

    public static void trace(String tag, String msg) {
        System.out.println("[" + tag + "] " + ANSI.YELLOW + "TRACE" + ANSI.RESET + " " + msg);
    }

    public static void warn(String tag, String msg) {
        System.out.println("[" + tag + "] " + ANSI.RED + "WARN" + ANSI.RESET + " " + msg);
    }

    public static void warn(String msg) {
        System.err.println(ANSI.RED + "WARN " + msg + ANSI.RESET);
    }

    public static void err(String tag, String msg) {
        System.err.println("[" + tag + "] " + ANSI.RED_BACKGROUND + ANSI.WHITE + "ERROR " + msg + ANSI.RESET);
        throw new RuntimeException("[" + tag + "] " + msg);
    }

    public static void err(String msg) {
        err("ERROR", msg);
    }

    public static void err() {
        throw new RuntimeException();
    }

    public static void stat(String msg) {
        System.out.println(ANSI.YELLOW_BACKGROUND + ANSI.BLACK + "[STAT]" + " " + msg + ANSI.RESET);
    }

    private class ANSI {
        private static final String RESET = "\u001B[0m";
        private static final String BLACK = "\u001B[30m";
        private static final String RED = "\u001B[31m";
        private static final String GREEN = "\u001B[32m";
        private static final String YELLOW = "\u001B[33m";
        private static final String BLUE = "\u001B[34m";
        private static final String PURPLE = "\u001B[35m";
        private static final String CYAN = "\u001B[36m";
        private static final String WHITE = "\u001B[37m";

        private static final String BLACK_BACKGROUND = "\u001B[40m";
        private static final String RED_BACKGROUND = "\u001B[41m";
        private static final String GREEN_BACKGROUND = "\u001B[42m";
        private static final String YELLOW_BACKGROUND = "\u001B[43m";
        private static final String BLUE_BACKGROUND = "\u001B[44m";
        private static final String PURPLE_BACKGROUND = "\u001B[45m";
        private static final String CYAN_BACKGROUND = "\u001B[46m";
        private static final String WHITE_BACKGROUND = "\u001B[47m";
    }
}