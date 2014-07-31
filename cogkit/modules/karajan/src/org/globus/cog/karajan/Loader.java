/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import k.rt.Context;
import k.rt.Executor;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.RootScope;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.Main;
import org.globus.cog.karajan.futures.FutureFault;
import org.globus.cog.karajan.parser.NativeParser;
import org.globus.cog.karajan.parser.ParsingException;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.KarajanProperties;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

/**
 * This is the main entry point into running a Karajan script. It handles
 * the loading and parsing of both .xml and .k files, processes command
 * line arguments, sets up the stack/context and runs the script.
 * 
 * @author Mihael Hategan
 *
 */
public class Loader {
	private static final Logger logger = Logger.getLogger(Loader.class);

	public static final String ARG_SHOWSTATS = "showstats";
	public static final String ARG_HELP = "help";
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

			Map<String, String> arguments = new HashMap<String, String>();
			if (ap.isPresent(ARG_HELP)) {
				ap.usage();
				System.exit(0);
			}
			if (ap.isPresent(ARG_SHOWSTATS)) {
				Configuration.getDefault().set(Configuration.SHOW_STATISTICS, true);
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
			WrapperNode tree;
			if (project != null) {
				tree = load(project);
			}
			else {
				project = "_";
				tree = loadFromString(source);
			}
			
			tree.setProperty(WrapperNode.FILENAME, project);
			
			Context context = new Context();
			context.setArguments(ap.getArguments());
			
			Main root = compile(tree, context);
			
			if (CompilerSettings.DUMP_COMPILED_TREE) {
				root.dump(new File(project + ".compiled"));
			}

			Executor ec = new Executor(root);
			
			ec.start(context);
			
			ec.waitFor();
			if (ec.isFailed()) {
				runerror = true;
			}
		
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.debug("Detailed exception:", e);
			error("Could not start execution.\n\t" + e.getMessage());
		}

		long end = System.currentTimeMillis();
		if (Configuration.getDefault().getFlag(Configuration.SHOW_STATISTICS)) {
			System.out.println("Done.");
			System.out.println("Total execution time: " + ((double) (end - start) / 1000) + " s");
			System.out.println("Total elements executed: " + Node.startCount);
			System.out.println("Average element execution rate: "
					+ (int) ((double) Node.startCount / (end - start) * 1000)
					+ " elements/second");
			System.out.println("Total future faults: " + FutureFault.count);
			System.out.println("Memory in use at termination: "
					+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
					/ 1024 + "KB");
			System.out.println("Free memory at termination: " + (Runtime.getRuntime().freeMemory())
					/ 1024 + "KB");
			System.out.println("Context switches: " + LWThread.contextSwitches);
		}

		System.exit(runerror ? 2 : 0);
	}

	private static Main compile(WrapperNode n, Context context) throws CompilationException {
		return (Main) n.compile(null, new RootScope(KarajanProperties.getDefault(), 
				(String) n.getProperty(WrapperNode.FILENAME), context));
	}

	public static WrapperNode load(String project) throws IOException, ParsingException {
		BufferedReader br = new BufferedReader(new FileReader(project));
		return load(project, br);
	}

	public static WrapperNode loadFromString(String source) throws ParsingException, IOException {
		BufferedReader br = new BufferedReader(new StringReader(source));
		return load("_", br);
	}

	private static WrapperNode load(String name, Reader reader) throws ParsingException, IOException {
		NativeParser p = new NativeParser(name, reader);
		WrapperNode n = p.parse();
		n.setProperty(WrapperNode.FILENAME, name);
		return n;
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
}