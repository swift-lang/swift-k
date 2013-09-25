// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.ant;

import java.util.Iterator;

import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.CanvasFrame;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;
import org.globus.cog.util.graph.Node;

/**
 * Extends a CanvasFrame to provide a menu for loading, importing, saving Ant
 * stuff
 */
public class ProjectFrame extends CanvasFrame {
	public ProjectFrame(ProjectNode node) {
		super(node);
	}

	public static void main(String[] args) {
		System.out.print("Starting...");
		ProjectNode node = new ProjectNode();
		ArgumentParser options = new ArgumentParser();
		options.setExecutableName("ant-viewer");
		options.addFlag("help", "Displays this help message and exits");
		options.addAlias("help", "h");
		options.addOption("load", "Loads the specified file", "file", ArgumentParser.OPTIONAL);
		options.addAlias("load", "l");
		options.addOption("ant", "Imports the specified Ant buildfile", "file",
				ArgumentParser.OPTIONAL);
		options.addAlias("ant", "a");
		options.addOption("execute",
				"Tells the viewer to start executing the named target after loading", "target",
				ArgumentParser.OPTIONAL);
		options.addAlias("execute", "e");
		options.addOption("expand", "Expands the indicated target", "target",
				ArgumentParser.OPTIONAL);
		options.addAlias("expand", "x");
		try {
			options.parse(args);
			if (options.isPresent("help")) {
				options.usage();
				System.exit(0);
			}
			options.checkMandatory();
		}
		catch (ArgumentParserException e) {
			System.out.println("Error parsing arguments: " + e.getMessage());
			options.usage();
			System.exit(1);
		}

		ProjectFrame frame = new ProjectFrame(node);
		if (options.hasValue("ant")) {
			node.importAntBuildfile(options.getStringValue("ant"));
		}
		else if (options.hasValue("load")) {
			node.load(options.getStringValue("load"));
		}
		if (options.hasValue("expand")) {
			GraphCanvas canvas = node.getCanvas();
			if (canvas == null) {
				error("Invalid target name: " + options.getStringValue("expand"));
			}
			Iterator i = canvas.getGraph().getNodesIterator();
			while (i.hasNext()) {
				Node n = (Node) i.next();
				NodeComponent nc = (NodeComponent) n.getContents();
				if (nc instanceof TargetNode) {
					if (nc.getPropertyValue("name").equals(options.getStringValue("expand"))) {
						new CanvasFrame(nc);
					}
				}
			}
			if (options.hasValue("execute")) {
				node.executeTarget(options.getStringValue("execute"));
			}
		}
		System.out.println("done");
		frame.run();
		System.exit(0);
	}

	private static void error(String message) {
		System.out.println(message);
		System.exit(0);
	}
}