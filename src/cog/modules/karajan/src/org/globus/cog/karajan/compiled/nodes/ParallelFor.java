
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 7, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;


public class ParallelFor extends AbstractParallelIterator {
	public static final Arg A_NAME = new Arg.Positional("name");
	public static final Arg A_IN = new Arg.Positional("in");
	
	static {
		setArguments(ParallelFor.class, new Arg[] { A_NAME, A_IN });
	}
}
