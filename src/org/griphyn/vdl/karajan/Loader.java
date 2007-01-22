/*
 * Created on Jan 12, 2005
 */
package org.griphyn.vdl.karajan;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.LinkedStack;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Monitor;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.PrintStreamChannel;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;
import org.griphyn.vdl.karajan.functions.ConfigProperty;
import org.griphyn.vdl.util.VDL2Config;
import org.griphyn.vdl.util.VDL2ConfigProperties;
import org.griphyn.vdl.util.VDL2ConfigProperties.PropInfo;

public class Loader extends org.globus.cog.karajan.Loader {
	private static final Logger logger = Logger.getLogger(Loader.class);

	public static final String ARG_SHOWSTATS = "showstats";
	public static final String ARG_DEBUGGER = "debugger";
	public static final String ARG_HELP = "help";
	public static final String ARG_MONITOR = "monitor";
	public static final String ARG_RESUME = "resume";
	public static final String ARG_INSTANCE_CONFIG = "config";
	public static final String ARG_TYPECHECK = "typecheck";
	public static final String ARG_DRYRUN = "dryrun";

	public static final String CONST_VDL_OPERATION = "vdl:operation";
	public static final String VDL_OPERATION_RUN = "run";
	public static final String VDL_OPERATION_TYPECHECK = "typecheck";
	public static final String VDL_OPERATION_DRYRUN = "dryrun";

	public static void main(String[] argv) {
		ArgumentParser ap = buildArgumentParser();
		long start = System.currentTimeMillis();
		String project = null;
		try {
			ap.parse(argv);

			Map arguments = new Hashtable();
			if (ap.isPresent(ARG_HELP)) {
				ap.usage();
				System.exit(0);
			}
			if (ap.isPresent(ARG_MONITOR)) {
				new Monitor().start();
			}
			if (!ap.hasValue(ArgumentParser.DEFAULT)) {
				error("No project specified");
			}
			project = ap.getStringValue(ArgumentParser.DEFAULT);
		}
		catch (ArgumentParserException e) {
			System.err.println("Error parsing arguments: " + e.getMessage() + "\n");
			ap.usage();
			System.exit(1);
		}

		boolean runerror = false;

		try {
			ElementTree tree = null;
			if (project != null) {
				tree = load(project);
			}
			else {
				System.err.println("No project specified");
				ap.usage();
				System.exit(1);
			}

			tree.setName(project);
			tree.getRoot().setProperty(FlowElement.FILENAME, project);

			ExecutionContext ec = new VDL2ExecutionContext(tree);

			// no order
			ec.setStdout(new PrintStreamChannel(System.out, true));

			if (ap.hasValue(ARG_RESUME)) {
				ec.addArgument("-rlog:resume=" + ap.getStringValue(ARG_RESUME));
			}

			VariableStack stack = new LinkedStack(ec);
			VDL2Config config = loadConfig(ap, stack);
			addCommandLineProperties(config, ap);

			if (ap.isPresent(ARG_DRYRUN)) {
				stack.setGlobal(CONST_VDL_OPERATION, VDL_OPERATION_DRYRUN);
			}
			else if (ap.isPresent(ARG_TYPECHECK)) {
				stack.setGlobal(CONST_VDL_OPERATION, VDL_OPERATION_TYPECHECK);
			}
			else {
				stack.setGlobal(CONST_VDL_OPERATION, VDL_OPERATION_RUN);
			}
			stack.setGlobal("vds.home", System.getProperty("vds.home"));

			ec.start(stack);
			ec.setArguments(ap.getArguments());
			ec.waitFor();
			if (ec.isFailed()) {
				runerror = true;
			}
		}
		catch (Exception e) {
			logger.debug("Detailed exception:", e);
			error("Could not start execution.\n\t" + e.getMessage());
		}

		System.exit(runerror ? 2 : 0);
	}

	private static VDL2Config loadConfig(ArgumentParser ap, VariableStack stack) throws IOException {
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

	private static void addCommandLineProperties(VDL2Config config, ArgumentParser ap) {
		Map desc = VDL2ConfigProperties.getPropertyDescriptions();
		Iterator i = desc.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			String name = (String) e.getKey();
			if (ap.isPresent(name)) {
				config.setProperty(name, ap.getStringValue(name));
			}
		}
	}

	private static ArgumentParser buildArgumentParser() {
		ArgumentParser ap = new ArgumentParser();
		ap.setArguments(true);
		ap.setExecutableName("vdlrun");
		ap.addOption(ArgumentParser.DEFAULT, "A file (.xml or .k) to execute", "file",
				ArgumentParser.OPTIONAL);

		ap.addFlag(ARG_HELP, "Display usage information");
		ap.addAlias(ARG_HELP, "h");

		ap.addFlag(ARG_TYPECHECK, "Does a typecheck instead of executing the workflow");

		ap.addFlag(ARG_DRYRUN,
				"Runs the workflow without submitting any jobs (can be used to get a graph)");

		ap.addFlag(ARG_MONITOR, "Shows a graphical resource monitor");

		ap.addOption(ARG_RESUME, "Resumes the execution using a log file", "file",
				ArgumentParser.OPTIONAL);
		ap.addOption(
				ARG_INSTANCE_CONFIG,
				"Indicates the VDL2 configuration file to be used for this run."
						+ " Properties in this configuration file will override the default properties. "
						+ "If individual command line arguments are used for properties, they will override "
						+ "the contents of this file.", "file", ArgumentParser.OPTIONAL);

		Map desc = VDL2ConfigProperties.getPropertyDescriptions();
		Iterator i = desc.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			PropInfo pi = (PropInfo) e.getValue();
			ap.addOption((String) e.getKey(), pi.desc, pi.validValues, ArgumentParser.OPTIONAL);
		}
		return ap;
	}
}