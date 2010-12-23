/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.swift.catalog.TCEntry;
import org.griphyn.vdl.karajan.TCCache;
import org.griphyn.vdl.util.FQN;

public class Executable extends VDLFunction {
	public static final Arg PA_TR = new Arg.Positional("tr");
	public static final Arg PA_HOST = new Arg.Positional("host");

	static {
		setArguments(Executable.class, new Arg[] { PA_TR, PA_HOST });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		TCCache tc = getTC(stack);
		String tr = TypeUtil.toString(PA_TR.getValue(stack));
		BoundContact bc = (BoundContact) PA_HOST.getValue(stack);
		TCEntry tce = getTCE(tc, new FQN(tr), bc);
		if (tce == null) {
			return tr;
		}
		else {
			return tce.getPhysicalTransformation();
		}
	}
}
