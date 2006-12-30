/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class Assign extends VDLFunction {
	public static final Arg A_DVAR = new Arg.TypedPositional("dvar", DSHandle.class, "handle");
	public static final Arg A_SVAR = new Arg.Positional("svar");
	public static final Arg A_DPATH = new Arg.Optional("dpath", "");
	public static final Arg A_SPATH = new Arg.Optional("spath", "");

	static {
		setArguments(Assign.class, new Arg[] { A_DVAR, A_SVAR, A_DPATH, A_SPATH });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		Path dpath = parsePath(A_DPATH.getValue(stack), stack);
		Path spath = parsePath(A_SPATH.getValue(stack), stack);
		DSHandle dvar = (DSHandle) A_DVAR.getValue(stack);
		Object s = A_SVAR.getValue(stack);
		if (s == null) {
			throw new ExecutionException("Source variable is null");
		}
		try {
			dvar = dvar.getField(dpath);
			if (s instanceof List) {
				if (!Path.EMPTY_PATH.equals(spath)) {
					throw new ExecutionException(
							"If the source is an array there can be no source path (" + spath + ")");
				}
				int index = 0;
				Iterator i = ((List) s).iterator();
				while (i.hasNext()) {
					Object n = i.next();
					Path p = Path.EMPTY_PATH.addLast(String.valueOf(index), true);
					if (n instanceof DSHandle) {
						dvar.getField(p).set((DSHandle) n);
					}
					else {
						DSHandle field = dvar.getField(p);
						field.setValue(n);
						closeShallow(stack, field);
					}
					index++;
				}
				closeShallow(stack, dvar);
			}
			else if (s instanceof DSHandle) {
				DSHandle svar = (DSHandle) s;
				svar = svar.getField(spath);
				/*
				 * Iterator i = svar.getFringePaths().iterator(); while
				 * (i.hasNext()) { String leafSPath = (String) i.next(); Path
				 * leafPath = Path.parse(leafSPath); mergeListeners(stack,
				 * dvar.getField(leafPath), svar.getField(leafPath)); }
				 */
				dvar.set(svar);
			}
			else {
				dvar.setValue(s);
				closeShallow(stack, dvar);
			}
		}
		catch (InvalidPathException e) {
			throw new ExecutionException(e);
		}
		return null;
	}
}
