// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *  
 */
package org.globus.cog.karajan.workflow.nodes;

import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;

public class ProjectNode extends SequentialWithArguments {
	public static final Logger logger = Logger.getLogger(ProjectNode.class);
	
	public static final Arg A_NAME = new Arg.Positional("name");

	private PrintStream out;

	static {
		setArguments(ProjectNode.class, new Arg[] { A_NAME });
	}

	public ProjectNode() {
		setElementType("project");
	}

	public static void main(String[] argv) {
		System.err.println("Use org.globus.cog.karajan.workflow.Loader as the main class");
		System.exit(2);
	}

	public ProjectNode getProjectNode() {
		return this;
	}
}