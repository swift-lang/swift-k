/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class FileNames extends VDLFunction {
	static {
		setArguments(FileNames.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String[] f = filename(stack);
		for (int i = 0; i < f.length; i++) {
			f[i] = relativize(f[i]);
		}
		return f;
	}
}
