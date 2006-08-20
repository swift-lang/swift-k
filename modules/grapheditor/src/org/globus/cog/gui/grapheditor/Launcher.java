// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/* 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor;

import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.generic.DefaultCommandListener;
import org.globus.cog.gui.grapheditor.generic.DisplayService;
import org.globus.cog.gui.grapheditor.generic.RootNode;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.GraphFrame;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

public class Launcher {
	private static Logger logger = Logger.getLogger(Launcher.class);

	private static RootNode root;

	public static void main(String[] args) {
		ArgumentParser options = new ArgumentParser();
		options.setExecutableName("graph-editor");
		options.addOption("s",
				"Starts the service on the specified port. If no port is specified, "
						+ "9999 is used.", "port", ArgumentParser.OPTIONAL);
		options.addFlag("h", "Displays this help message and exits.");
		options.addAlias("h", "help");
		options.addOption("l", "specifies a file to be loaded on startup", "file",
				ArgumentParser.OPTIONAL);
		options.addAlias("l", "load");
		options.addOption("t",
				"Starts on the specified target. If missing, the default target (the "
						+ "Swing GUI) will be used", "target", ArgumentParser.OPTIONAL);
		options.addAlias("t", "target");
		options.addOption("q",
				"Render the graph and quit. Useful with non-interactive targets. This is "
						+ "the default when the -load option is used. In server mode, the program "
						+ "will wait for a graph first, render it and then quit.",
				ArgumentParser.OPTIONAL);
		options.addAlias("q", "quit");
		options.addOption("r", "In server mode loop and wait for updates, and render "
				+ "them, as opposed to quitting after the graph is received.",
				ArgumentParser.OPTIONAL);
		options.addAlias("r", "loop");
		options.addOption("o",
				"Pass additional options to various sub-components. The value must be"
						+ " quoted and has the form [property=value[, property=value[,...]]]. "
						+ "Take a look at etc/grapheditor.properties for a list of properties.",
				"options", ArgumentParser.OPTIONAL);
		options.addAlias("o", "options");
		try {
			options.parse(args);
			if (options.isPresent("h")) {
				options.usage();
				System.exit(1);
			}
			options.checkMandatory();
		}
		catch (ArgumentParserException e) {
			System.out.println("Error parsing arguments: " + e.getMessage());
			options.usage();
			System.exit(1);
		}
		root = new RootNode();
		if (options.isPresent("s")) {
			new DisplayService(options.getIntValue("s", 9999), new DefaultCommandListener(root));
		}
		try {
			if (options.isPresent("t")) {
				RendererFactory.setCurrentTarget(options.getStringValue("t"));
			}
			parseProperties("target.properties", root);
			RootContainer rootContainer = RendererFactory.newRootContainer();
			parseProperties("grapheditor.properties", root);
			if (options.isPresent("o")) {
				if (!parseOptions(options.getStringValue("o"), root)) {
					System.out.println("Error parsing options.");
					options.usage();
					System.exit(1);
				}
			}
			rootContainer.setRootNode(root);
			rootContainer.activate();
			if (options.isPresent("l")) {
				if (!options.hasValue("l")) {
					System.out.println("No file name supplied!");
					options.usage();
					System.exit(1);
				}
				rootContainer.load(options.getStringValue("l"));
			}
			rootContainer.run();
		}
		catch (Exception e) {
			logger.error("Execution terminated with the following exception:", e);
		}
		System.exit(0);

	}

	public static void parseProperties(String name, NodeComponent root) {
		try {
			URL url = root.getClass().getClassLoader().getResource(name);
			Properties properties = new Properties();
			properties.load(url.openStream());
			Enumeration e = properties.propertyNames();
			while (e.hasMoreElements()) {
				String propName = (String) e.nextElement();
				if (propName.startsWith("root.container")) {
					try {
						int lastDot = propName.lastIndexOf(".");
						String target = propName.substring(lastDot + 1);
						Class rootContainerClass = Class.forName(properties.getProperty(propName));
						RendererFactory.addRootContainer(target, rootContainerClass);
					}
					catch (Exception e1) {
						logger.warn("Invalid property: " + propName + "="
								+ properties.getProperty(propName), e1);
					}
				}
				else {
					logger.debug("Setting property " + propName);
					root.setPropertyValue(propName, properties.getProperty(propName));
				}
			}
		}
		catch (Exception e) {
			logger.warn("Could not load " + name + ". Using default settings.");
			RendererFactory.setDefaultRootContainer(GraphFrame.class);
		}
	}

	public static boolean parseOptions(String options, NodeComponent root) {
		try {
			StringTokenizer st = new StringTokenizer(options, ",");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				String[] pair = token.split("=");
				root.setPropertyValue(pair[0].trim(), pair[1].trim());
			}
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}