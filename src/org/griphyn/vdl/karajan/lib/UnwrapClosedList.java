/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class UnwrapClosedList extends VDLFunction {
	public static final Logger logger = Logger.getLogger(UnwrapClosedList.class);
	
	public static final Arg.Positional PA_LIST = new Arg.Positional("list");

	static {
		setArguments(UnwrapClosedList.class, new Arg[] { PA_LIST });
	}

	/**
	 * Takes a supplied variable and path, and returns the unique value at that
	 * path. Path can contain wildcards, in which case an array is returned.
	 */
	public Object function(VariableStack stack) throws ExecutionException {
		@SuppressWarnings("unchecked")
        List<DSHandle> l = (List<DSHandle>) PA_LIST.getValue(stack);
		
		Object[] r = new Object[l.size()];
		
		for (int i = 0; i < r.length; i++) {
		    r[i] = l.get(i).getValue();
		}
		
		return r;
	}
}
