// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 12, 2005
 */
package org.globus.cog.karajan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.debugger.DebuggerFrame;
import org.globus.cog.karajan.translator.KarajanTranslator;
import org.globus.cog.karajan.util.Cache;
import org.globus.cog.karajan.util.Monitor;
import org.globus.cog.karajan.util.serialization.XMLConverter;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.PrintStreamChannel;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.futures.FutureFault;
import org.globus.cog.karajan.workflow.futures.FuturesMonitor;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

public class Loader {
	private static final Logger logger = Logger.getLogger(Loader.class);

	public static final String ARG_SHOWSTATS = "showstats";
	public static final String ARG_DEBUGGER = "debugger";
	public static final String ARG_HELP = "help";
	public static final String ARG_DEBUG = "debug";
	public static final String ARG_MONITOR = "monitor";
	public static final String ARG_DUMPSTATE = "dumpstate";
	public static final String ARG_INTERMEDIATE = "intermediate";
	public static final String ARG_CACHE = "cache";
	public static final String ARG_EXECUTE = "execute";
	public static final String ARG_CSTDOUT = "stdoutUnordered";

	public static void main(String[] argv) {
		ArgumentParser ap = buildArgumentParser();
		boolean debug = false, cache = false;
		long start = System.currentTimeMillis();
		String project = null, source = null;
		try {
			ap.parse(argv);

			Map arguments = new Hashtable();
			if (ap.isPresent(ARG_HELP)) {
				ap.usage();
				System.exit(0);
			}
			if (ap.isPresent(ARG_SHOWSTATS)) {
				Configuration.getDefault().set(Configuration.SHOW_STATISTICS, true);
			}
			if (ap.isPresent(ARG_DEBUGGER)) {
				Configuration.getDefault().set(Configuration.DEBUGGER, true);
			}
			if (ap.isPresent(ARG_DEBUG)) {
				FlowNode.debug = true;
				FuturesMonitor.debug = true;
				installKeyboardHooks();
			}
			if (ap.isPresent(ARG_MONITOR)) {
				new Monitor().start();
			}
			if (ap.isPresent(ARG_DUMPSTATE)) {
				Configuration.getDefault().set(Configuration.DUMP_STATE_ON_ERROR, true);
			}
			if (ap.isPresent(ARG_INTERMEDIATE)) {
				Configuration.getDefault().set(Configuration.WRITE_INTERMEDIATE_SOURCE, true);
			}
			if (ap.isPresent(ARG_CACHE)) {
				cache = true;
			}
			if (ap.hasValue(ARG_EXECUTE)) {
				if (ap.hasValue(ArgumentParser.DEFAULT)) {
					error("Cannot use both -" + ARG_EXECUTE + " and a file");
				}
				source = ap.getStringValue(ARG_EXECUTE);
			}
			else {
				if (!ap.hasValue(ArgumentParser.DEFAULT)) {
					error("No project specified");
				}
				project = ap.getStringValue(ArgumentParser.DEFAULT);
			}
		}
		catch (ArgumentParserException e) {
			System.err.println("Error parsing arguments: " + e.getMessage() + "\n");
			ap.usage();
			System.exit(1);
		}

		boolean runerror = false;

		try {
			ElementTree tree;
			if (project != null) {
				tree = load(project);
			}
			else {
				project = "_";
				tree = loadFromString(source);
			}

			if (cache) {
				if (source != null) {
					error("Cannot use -" + ARG_CACHE + " with -" + ARG_EXECUTE);
				}
				File f = new File(project);
				File c = new File(project + ".cache");
				try {
					if (f.lastModified() < c.lastModified()) {
						FileReader fr = new FileReader(project + ".cache");
						tree.setCache((Cache) XMLConverter.readObject(fr));
						fr.close();
					}
				}
				catch (Exception e) {
					c.delete();
				}
			}
			tree.setName(project);
			tree.getRoot().setProperty(FlowElement.FILENAME, project);
			if (Configuration.getDefault().getFlag(Configuration.DEBUGGER)) {
				DebuggerFrame debugger = new DebuggerFrame(tree);
				debugger.pack();
				debugger.setVisible(true);
				debugger.waitFor();
			}
			else {
				ExecutionContext ec = new ExecutionContext(tree);
				ec.setDumpState(Configuration.getDefault().getFlag(
						Configuration.DUMP_STATE_ON_ERROR));
				if (ap.isPresent(ARG_CSTDOUT)) {
					ec.setStdout(new PrintStreamChannel(System.out, true));
				}
				ec.setArguments(ap.getArguments());
				ec.start();
				/*
				 * Strange thing here. For even slightly not so short programs,
				 * by the time control flow reaches this point. the execution is
				 * already done.
				 */
				ec.waitFor();
				if (ec.isFailed()) {
					runerror = true;
				}
			}
			if (cache) {
				try {
					FileWriter fw = new FileWriter(project + ".cache");
					XMLConverter.serializeObject(tree.getCache(), fw);
					fw.close();
				}
				catch (Exception e) {
					logger.warn("Failed to save cache", e);
				}
			}
		}
		catch (Exception e) {
			logger.debug("Detailed exception:", e);
			error("Could not start execution.\n\t" + e.getMessage());
		}

		long end = System.currentTimeMillis();
		if (Configuration.getDefault().getFlag(Configuration.SHOW_STATISTICS)) {
			System.out.println("Done.");
			System.out.println("Total execution time: " + ((double) (end - start) / 1000) + " s");
			System.out.println("Total elements executed: " + FlowNode.startCount);
			System.out.println("Average element execution rate: "
					+ (int) ((double) FlowNode.startCount / (end - start) * 1000)
					+ " elements/second");
			System.out.println("Total events: " + EventBus.eventCount);
			System.out.println("Avarage event rate: "
					+ (int) ((double) EventBus.eventCount * 1000 / (end - start))
					+ " events/second");
			System.out.println("Cummulative event time: " + EventBus.cummulativeEventTime + " ms");
			System.out.println("Average event time: " + (double) EventBus.cummulativeEventTime
					/ EventBus.eventCount * 1000 + " us");
			System.out.println("Total future faults: " + FutureFault.count);
			System.out.println("Memory in use at termination: "
					+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
					/ 1024 + "KB");
			System.out.println("Free memory at termination: " + (Runtime.getRuntime().freeMemory())
					/ 1024 + "KB");
		}

		System.exit(runerror ? 2 : 0);
	}

	public static ElementTree load(String project) throws SpecificationException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(project));
		return load(project, br, project.endsWith(".xml") || project.endsWith(".kml"));
	}

	public static ElementTree loadFromString(String source) throws SpecificationException {
		BufferedReader br = new BufferedReader(new StringReader(source));
		return load("_", br, source.startsWith("<"));
	}

	private static ElementTree load(String name, Reader reader, boolean xml)
			throws SpecificationException {
		try {
			if (!EventBus.isInitialized()) {
				EventBus.initialize();
			}
			EventBus.suspendAll();
			ElementTree source;
			if (xml) {
				source = XMLConverter.readSource(reader, name);
			}
			else {
				source = XMLConverter.readSource(new KarajanTranslator(reader, name).translate(),
						name, false);
			}
			EventBus.resumeAll();
			return source;
		}
		catch (Exception e) {
			throw new SpecificationException("Error reading source: " + e.getMessage(), e);
		}
	}

	private static ArgumentParser buildArgumentParser() {
		ArgumentParser ap = new ArgumentParser();
		ap.setExecutableName("cog-workflow");
		ap.addOption(ArgumentParser.DEFAULT, "A file (.xml or .k) to execute", "file",
				ArgumentParser.OPTIONAL);
		ap.setArguments(true);
		ap.addOption(ARG_EXECUTE, "Execute the script given as argument", "string",
				ArgumentParser.OPTIONAL);
		ap.addAlias(ARG_EXECUTE, "e");
		ap.addFlag(ARG_SHOWSTATS, "Show various execution statistics at the end of the "
				+ "execution");
		ap.addFlag(ARG_DEBUG,
				"Enable debugging. This will enable a number of internal tests at the "
						+ "expense of speed. You should not use this since it is useful only "
						+ "for catching subtle consistency issues with the interpreter.");
		ap.addFlag(ARG_DEBUGGER, "EXPERIMENTAL and BUGGY. Starts the internal graphical debugger");
		ap.addFlag(ARG_MONITOR, "Shows resource monitor");
		ap.addFlag(ARG_DUMPSTATE, "If specified, in case of a fatal error, the interpreter will "
				+ "dump the state in a file");
		ap.addFlag(ARG_INTERMEDIATE, "Saves intermediate code resulting from the translation "
				+ "of .k files");
		ap.addFlag(ARG_HELP, "Display usage information");
		ap.addAlias(ARG_HELP, "h");
		ap.addFlag(ARG_CACHE, "EXPERIMENTAL! Enables cache persistance");
		ap.addFlag(ARG_CSTDOUT,
				"Make print() invocations produce produce results in the order they are executed. By default"
						+ " the order is lexical.");
		return ap;
	}

	protected static void error(final String err) {
		System.err.println(err);
		System.exit(1);
	}

	private static void installKeyboardHooks() {
		new Thread() {
			public void run() {
				try {
					while (true) {
						while (System.in.available() == 0) {
							Thread.sleep(250);
						}
						int c = System.in.read();
						if (c == 'f') {
							System.out.println("Futures: " + FuturesMonitor.monitor);
						}
						if (c == 't') {
							System.out.println("Threads: " + FlowNode.threadTracker);
						}
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				catch (InterruptedException e) {
					return;
				}
			}
		}.start();
	}
}