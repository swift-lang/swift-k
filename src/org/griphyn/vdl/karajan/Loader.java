/*
 * Created on Jan 12, 2005
 */
package org.griphyn.vdl.karajan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.globus.cog.karajan.scheduler.WeightedHostScoreScheduler;
import org.globus.cog.karajan.stack.LinkedStack;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Monitor;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.PrintStreamChannel;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.grid.AbstractGridNode;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;
import org.globus.cog.util.TextFileLoader;
import org.globus.swift.data.Director;
import org.griphyn.vdl.engine.Karajan;
import org.griphyn.vdl.karajan.functions.ConfigProperty;
import org.griphyn.vdl.karajan.lib.Execute;
import org.griphyn.vdl.karajan.lib.Log;
import org.griphyn.vdl.karajan.lib.New;
import org.griphyn.vdl.karajan.monitor.MonitorAppender;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.toolkit.VDLt2VDLx;
import org.griphyn.vdl.toolkit.VDLt2VDLx.IncorrectInvocationException;
import org.griphyn.vdl.toolkit.VDLt2VDLx.ParsingException;
import org.griphyn.vdl.util.VDL2Config;
import org.griphyn.vdl.util.VDL2ConfigProperties;
import org.griphyn.vdl.util.VDL2ConfigProperties.PropInfo;

public class Loader extends org.globus.cog.karajan.Loader {
    private static final Logger logger = Logger.getLogger(Loader.class);

    public static final String ARG_HELP = "help";
    public static final String ARG_VERSION = "version";
    public static final String ARG_MONITOR = "monitor";
    public static final String ARG_RESUME = "resume";
    public static final String ARG_INSTANCE_CONFIG = "config";
    public static final String ARG_TYPECHECK = "typecheck";
    public static final String ARG_DRYRUN = "dryrun";
    public static final String ARG_VERBOSE = "verbose";
    public static final String ARG_DEBUG = "debug";
    public static final String ARG_LOGFILE = "logfile";
    public static final String ARG_CDMFILE = "cdm.file";
    public static final String ARG_RUNID = "runid";
    public static final String ARG_TUI = "tui";
    public static final String ARG_RECOMPILE = "recompile";
    public static final String ARG_REDUCED_LOGGING = "reduced.logging";
    public static final String ARG_MINIMAL_LOGGING = "minimal.logging";

    public static final String CONST_VDL_OPERATION = "vdl:operation";
    public static final String VDL_OPERATION_RUN = "run";
    public static final String VDL_OPERATION_TYPECHECK = "typecheck";
    public static final String VDL_OPERATION_DRYRUN = "dryrun";

    public static String buildVersion;

    public static void main(String[] argv) {
        logger.debug("Loader started");
        ArgumentParser ap = buildArgumentParser();
        String project = null;
        try {
            ap.parse(argv);

            if (ap.isPresent(ARG_HELP)) {
                ap.usage();
                System.exit(0);
            }
            if (ap.isPresent(ARG_VERSION)){
            	ap.version();
            	System.exit(0);
            }
            if (ap.isPresent(ARG_MONITOR)) {
                new Monitor().start();
            }
            if (!ap.hasValue(ArgumentParser.DEFAULT)) {
                error("No SwiftScript program specified");
            }
            project = ap.getStringValue(ArgumentParser.DEFAULT);
        }
        catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage()
                    + "\n");
            shortUsage();
            System.exit(1);
        }

        if (!new File(project).exists()) {
            System.err.println("SwiftScript program does not exist: " + project
                    + "\n");
            shortUsage();
            System.exit(1);
        }

        boolean runerror = false;
        String projectName = projectName(project);

        String runID;
        if (ap.isPresent(ARG_RUNID)) {
            runID = ap.getStringValue(ARG_RUNID);
        }
        else {
            runID = getUUID();
        }

        try {
            //Thread.sleep(20000);
            setupLogging(ap, projectName, runID);
            if (logger.isDebugEnabled()) {
                logger.debug("arguments: " + Arrays.asList(argv));
                logger.debug("Max heap: " + Runtime.getRuntime().maxMemory());
            }
            
            if (ap.isPresent(ARG_CDMFILE)) {
                loadCDM(ap); 
            }
            
            if (!(new File(project).exists())) {
                logger.error("Input file " + project + " does not exist.");
                System.exit(4);
            }

            if (project.endsWith(".swift")) {
                try {
                    project = compile(project, ap.isPresent(ARG_RECOMPILE));
                }
                catch (ParsingException pe) {
                    // the compiler should have already logged useful
                    // error messages, so this log line is just for
                    // debugging
                    logger.debug("Exception when compiling " + project, pe);
                    System.exit(3);
                }
            }
            ElementTree tree = null;
            if (project != null) {
                tree = load(project);
            }
            else {
                System.err.println("No source file specified");
                shortUsage();
                System.exit(1);
            }

            tree.setName(projectName + "-" + runID);
            tree.getRoot().setProperty(FlowElement.FILENAME, project);

            VDL2ExecutionContext ec = new VDL2ExecutionContext(tree,
                projectName);
            ec.setRunID(runID);
            // no order
            ec.setStdout(new PrintStreamChannel(System.out, true));

            VariableStack stack = new LinkedStack(ec);
            VDL2Config config = loadConfig(ap, stack);
            addCommandLineProperties(config, ap);
            debugSitesText(config);
            
            if (ap.isPresent(ARG_DRYRUN)) {
                stack.setGlobal(CONST_VDL_OPERATION, VDL_OPERATION_DRYRUN);
            }
            else if (ap.isPresent(ARG_TYPECHECK)) {
                stack.setGlobal(CONST_VDL_OPERATION, VDL_OPERATION_TYPECHECK);
            }
            else {
                stack.setGlobal(CONST_VDL_OPERATION, VDL_OPERATION_RUN);
            }
            stack.setGlobal("swift.home", System.getProperty("swift.home"));
            stack.setGlobal("PATH_SEPARATOR", File.separator);

            List arguments = ap.getArguments();
            if (ap.hasValue(ARG_RESUME)) {
                arguments.add("-rlog:resume=" + ap.getStringValue(ARG_RESUME));
            }
            ec.setArguments(arguments);
            // long start = System.currentTimeMillis();
            new HangChecker(stack).start();
            ec.start(stack);
            ec.waitFor();
            if (ec.isFailed()) {
                runerror = true;
            }
        }
        catch (Exception e) {
            logger.debug("Detailed exception:", e);
            error("Could not start execution.\n\t" + e.getMessage());
        }

        if (runerror) {
            logger.info("Swift finished with errors");
        }
        else {
            logger.info("Swift finished with no errors");
        }
        if (ap.isPresent(ARG_TUI)) {
            ma.close();
        }
        System.exit(runerror ? 2 : 0);
    }

    private static void shortUsage() {
        System.err.print("For usage information:  swift -help\n\n");
    }
    
    static void loadCDM(ArgumentParser ap) {
        String cdmString = null;
        try { 
            cdmString = ap.getStringValue(ARG_CDMFILE);
            File cdmFile = new File(cdmString);
            debugText("CDM FILE", cdmFile);
            Director.load(cdmFile); 
        }
        catch (IOException e) { 
            logger.debug("Detailed exception:", e);
            error("Could not start execution.\n\t" +
                  "Could not read given CDM policy file: " + cdmString);
        }
    }
    
    public static String compile(String project) throws FileNotFoundException,
            ParsingException, IncorrectInvocationException,
            CompilationException, IOException {
        return compile(project, false);
    }

    public static String compile(String project, boolean forceRecompile) throws FileNotFoundException,
            ParsingException, IncorrectInvocationException,
            CompilationException, IOException {
        File swiftscript = new File(project);
        debugText("SWIFTSCRIPT", swiftscript);
        String projectBase = project.substring(0, project.lastIndexOf('.'));
        File xml = new File(projectBase + ".xml");
        File kml = new File(projectBase + ".kml");

        loadBuildVersion();

        boolean recompile = forceRecompile;

        if (swiftscript.lastModified() > kml.lastModified()) {
            logger.info(project + ": source file is new. Recompiling.");
            recompile = true;
        }

        if (kml.exists()) {
            // read first line of kml
            Reader fr = new FileReader(kml);
            BufferedReader br = new BufferedReader(fr);
            String firstLine = br.readLine();
            if (firstLine == null) {
                logger.debug("KML file is empty. Recompiling.");
                recompile = true;
            }
            else {
                String prefix = "<!-- CACHE ID ";
                int offset = firstLine.indexOf(prefix);
                if (offset < 0) {
                    // no build version in the KML
                    logger.info(project
                            + ": has no build version. Recompiling.");
                    recompile = true;
                }
                else {
                    String cut = firstLine.substring(offset + prefix.length());
                    int endOffset = cut.indexOf(" -->");
                    String kmlversion = cut.substring(0, endOffset);
                    logger.debug("kmlversion is >" + kmlversion + "<");
                    logger.debug("build version is >" + buildVersion + "<");
                    if (!(kmlversion.equals(buildVersion))) {
                        logger.info(project
                            + ": source file was"
                            + " compiled with a different version of Swift. Recompiling.");
                        recompile = true;
                    }
                }
            }
        }

        if (recompile) {
            VDLt2VDLx.compile(new FileInputStream(swiftscript),
                new PrintStream(new FileOutputStream(xml)));

            try {
                FileOutputStream f = new FileOutputStream(kml);
                Karajan.compile(xml.getAbsolutePath(), new PrintStream(f));
                f.close();
            }
            catch (Error e) {
                kml.delete();
                throw e;
            }
            catch (CompilationException e) {
                // if we leave a kml file around, then a subsequent
                // re-run will skip recompiling and cause a different
                // error message for the user
                kml.delete();
                throw e;
            }
            catch (Exception e) {
                // if we leave a kml file around, then a subsequent
                // re-run will skip recompiling and cause a different
                // error message for the user
                kml.delete();
                throw new CompilationException(
                    "Failed to convert .xml to .kml for " + project, e);
            }
        }
        else {
            logger.debug("Recompilation suppressed.");
        }
        return kml.getAbsolutePath();
    }

    /**
       Enter the text content of given files into the log
       @throws IOException
     */
    public static void debugText(String name, File file) {
    	Logger textLogger = Logger.getLogger("swift.textfiles");
    	try {
    		if (textLogger.isDebugEnabled()) {
    			String text = TextFileLoader.loadFromFile(file);
    			textLogger.debug("BEGIN " + name + ":\n" + text + "\n");
    			textLogger.debug("END " + name + ":");
    		}
    	}
    	catch (IOException e) { 
    		logger.warn("Could not open: " + file);
    	}
	}

    static void debugSitesText(VDL2Config config) {
    	VDL2Config defaultConfig = null;
    	try {
			defaultConfig = VDL2Config.getDefaultConfig(); 
			
		} catch (IOException e) {
			logger.warn("Could not log sites file text");			
		}
		String poolFile = config.getPoolFile();
		String defaultPoolFile = defaultConfig.getPoolFile();
		if (poolFile.equals(defaultPoolFile)) {
			Logger textLogger = Logger.getLogger("swift.textfiles");
			textLogger.debug("using default sites file");
		}
		else {
			debugText("SITES", new File(poolFile));
		}
    }
    
	private static void loadBuildVersion() {
        try {
            File f = new File(System.getProperty("swift.home")
                    + "/libexec/buildid.txt");
            BufferedReader br = new BufferedReader(new FileReader(f));
            buildVersion = br.readLine();
        }
        catch (IOException e) {
            buildVersion = null;
        }
    }

    private static VDL2Config loadConfig(ArgumentParser ap, VariableStack stack)
            throws IOException {
        VDL2Config conf;
        if (ap.hasValue(ARG_INSTANCE_CONFIG)) {
            String configFile = ap.getStringValue(ARG_INSTANCE_CONFIG);
            stack.setGlobal(ConfigProperty.INSTANCE_CONFIG_FILE, configFile);
            conf = VDL2Config.getConfig(configFile);
        }
        else {
            conf = (VDL2Config) VDL2Config.getConfig().clone();
        }
        stack.setGlobal(ConfigProperty.INSTANCE_CONFIG, conf);
        return conf;
    }

    private static void addCommandLineProperties(VDL2Config config,
            ArgumentParser ap) {
        Map desc = VDL2ConfigProperties.getPropertyDescriptions();
        Iterator i = desc.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            String name = (String) e.getKey();
            if (ap.isPresent(name)) {
            	String value = ap.getStringValue(name);
            	logger.debug("setting: " + name + " to: " + value);
            	config.setProperty(name, value);
            }
        }
    }

    private static ArgumentParser buildArgumentParser() {
        ArgumentParser ap = new ArgumentParser();
        ap.setArguments(true);
        ap.setExecutableName("swift");
        ap
            .addOption(ArgumentParser.DEFAULT,
                "A file (.swift or .kml) to execute", "file",
                ArgumentParser.OPTIONAL);

        ap.addFlag(ARG_HELP, "Display usage information");
	ap.addFlag(ARG_VERSION, "Version:");
        ap.addAlias(ARG_HELP, "h");

        ap.addFlag(ARG_RECOMPILE, 
            "Forces Swift to re-compile the invoked Swift script. " +
            "While Swift is meant to detect when recompilation is necessary, " +
            "in some special cases it fails to do so. This flag helps with those special cases.");
        ap.addFlag(ARG_TYPECHECK,
            "Does a typecheck instead of executing the SwiftScript program");

        ap
            .addFlag(
                ARG_DRYRUN,
                "Runs the SwiftScript program without submitting any jobs (can be used to get a graph)");

        ap.addFlag(ARG_MONITOR, "Shows a graphical resource monitor");

        ap.addOption(ARG_RESUME, "Resumes the execution using a log file",
            "file", ArgumentParser.OPTIONAL);
        ap
            .addOption(
                ARG_INSTANCE_CONFIG,
                "Indicates the Swift configuration file to be used for this run."
                        + " Properties in this configuration file will override the default properties. "
                        + "If individual command line arguments are used for properties, they will override "
                        + "the contents of this file.", "file",
                ArgumentParser.OPTIONAL);
        ap
            .addFlag(
                ARG_VERBOSE,
                "Increases the level of output that Swift produces on the console to include more detail "
                        + "about the execution");
        ap.addAlias(ARG_VERBOSE, "v");
        ap
            .addFlag(
                ARG_DEBUG,
                "Increases the level of output that Swift produces on the console to include lots of "
                        + "detail about the execution");
        ap.addAlias(ARG_DEBUG, "d");
        ap
            .addOption(
                ARG_LOGFILE,
                "Specifies a file where log messages should go to. By default Swift "
                        + "uses the name of the SwiftScript program being run and additional information to make the name unique.",
                "file", ArgumentParser.OPTIONAL);
        ap
        .addOption(
            ARG_CDMFILE,
            "Specifies a CDM policy file.",
            "file", ArgumentParser.OPTIONAL);
        ap
            .addOption(
                ARG_RUNID,
                "Specifies the run identifier. This must be unique for every invocation of a workflow and is used in several places to keep files from different executions cleanly separated. By default, a datestamp and random number are used to generate a run identifier.",
                "string", ArgumentParser.OPTIONAL);
        ap.addFlag(ARG_TUI);
        ap.addFlag(ARG_REDUCED_LOGGING, "Makes logging more terse by disabling provenance " +
        		"information and low-level task messages");
        ap.addFlag(ARG_MINIMAL_LOGGING, "Makes logging much more terse: " +
                 "reports warnings only");

        Map desc = VDL2ConfigProperties.getPropertyDescriptions();
        Iterator i = desc.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            PropInfo pi = (PropInfo) e.getValue();
            ap.addOption((String) e.getKey(), pi.desc, pi.validValues,
                ArgumentParser.OPTIONAL);
        }
        return ap;
    }

    private static MonitorAppender ma;

    protected static void setupLogging(ArgumentParser ap, String projectName,
            String runID) throws IOException {
        String logfile;
        if (ap.isPresent(ARG_LOGFILE)) {
            logfile = ap.getStringValue(ARG_LOGFILE);
        }
        else {
            logfile = projectName + "-" + runID + ".log";
        }

        VDL2Config config = VDL2Config.getConfig();
        config.put("logfile", logfile);
        
        File f = new File(logfile);

        FileAppender fa = (FileAppender) getAppender(FileAppender.class);
        if (fa == null) {
            logger.warn("Failed to configure log file name");
        }
        else {
            fa.setFile(f.getAbsolutePath());
            fa.activateOptions();
        }
        Level level = Level.WARN;
        if (ap.isPresent(ARG_VERBOSE)) {
            level = Level.INFO;
        }
        if (ap.isPresent(ARG_DEBUG)) {
            level = Level.DEBUG;
        }
        ConsoleAppender ca = (ConsoleAppender) getAppender(ConsoleAppender.class);
        if (ca == null) {
            logger.warn("Failed to configure console log level");
        }
        else {
            ca.setThreshold(level);
            ca.activateOptions();
        }
        Logger.getLogger(Log.class).setLevel(Level.INFO);
        if (ap.isPresent(ARG_TUI)) {
            ma = new MonitorAppender(projectName);
            Logger.getRootLogger().addAppender(ma);
            Logger.getLogger(Log.class).setLevel(Level.DEBUG);
            Logger.getLogger(AbstractGridNode.class).setLevel(Level.DEBUG);
            Logger.getLogger(Execute.class).setLevel(Level.DEBUG);
            Logger.getLogger(VDL2ExecutionContext.class).setLevel(Level.INFO);
            Logger.getLogger(WeightedHostScoreScheduler.class).setLevel(
                Level.INFO);
            ca.setThreshold(Level.FATAL);
        }
        else if (ap.isPresent(ARG_MINIMAL_LOGGING)) {
            fa.setThreshold(Level.WARN);
        }
        else if (ap.isPresent(ARG_REDUCED_LOGGING)) {
            Logger.getLogger(AbstractDataNode.class).setLevel(Level.WARN);
            Logger.getLogger(New.class).setLevel(Level.WARN);
            Logger.getLogger("org.globus.cog.karajan.workflow.service").setLevel(Level.WARN);
        }
    }

    protected static Appender getAppender(Class cls) {
        Logger root = Logger.getRootLogger();
        Enumeration e = root.getAllAppenders();
        while (e.hasMoreElements()) {
            Appender a = (Appender) e.nextElement();
            if (cls.isAssignableFrom(a.getClass())) {
                return a;
            }
        }
        return null;
    }

    protected static String projectName(String project) {
        project = project
            .substring(project.lastIndexOf(File.separatorChar) + 1);
        return project.substring(0, project.lastIndexOf('.'));
    }

    private static long lastTime = 0;

    private static DateFormat UID_DATE_FORMAT = new SimpleDateFormat(
        "yyyyMMdd-HHmm");

    public static synchronized String getUUID() {
        long l;
        // we want seconds and milliseconds from this one
        // that's 16 bits
        while (true) {
            l = System.currentTimeMillis() % (60 * 1000);
            if (l != lastTime) {
                lastTime = l;
                break;
            }
            else {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                }
            }
        }
        // and for the msbs, some random stuff
        int rnd;
        try {
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            rnd = prng.nextInt();
        }
        catch (NoSuchAlgorithmException e) {
            rnd = (int) (Math.random() * 0xffffff);
        }
        // and 24 bits
        rnd &= 0x00ffffff;
        l += ((long) rnd) << 16;
        return UID_DATE_FORMAT.format(new Date()) + '-' + alphanum(l);
    }

    public static final String codes = "0123456789abcdefghijklmnopqrstuvxyz";

    protected static String alphanum(long val) {
        StringBuffer sb = new StringBuffer();
        int base = codes.length();
        for (int i = 0; i < 8; i++) {
            int c = (int) (val % base);
            sb.append(codes.charAt(c));
            val = val / base;
        }
        return sb.toString();
    }

}
