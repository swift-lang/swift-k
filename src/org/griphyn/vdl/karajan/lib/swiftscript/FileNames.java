/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;

public class FileNames extends VDLFunction {
	static {
		setArguments(FileNames.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String[] f = filename(stack);
		DSHandle[] h = new DSHandle[f.length];
		for (int i = 0; i < f.length; i++) {
			h[i] = RootDataNode.newNode(Types.STRING, relativize(f[i]));
		}
		return h;
	}
}
