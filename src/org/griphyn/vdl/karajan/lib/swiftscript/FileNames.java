/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.type.Types;

public class FileNames extends VDLFunction {
	static {
		setArguments(FileNames.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String[] f = filename(stack);
		DSHandle returnArray = new RootArrayDataNode(Types.STRING.arrayType());
		try {
			for (int i = 0; i < f.length; i++) {
				Path p = parsePath("["+i+"]", stack);
				DSHandle h = returnArray.getField(p);
				h.setValue(relativize(f[i]));
			}
		} catch (InvalidPathException e) {
			throw new ExecutionException("Unexpected invalid path exception",e);
		}
		returnArray.closeShallow();
		
		int provid = VDLFunction.nextProvenanceID();
		logProvenanceParameter(provid, (DSHandle) PA_VAR.getValue(stack), "input");
		logProvenanceResult(provid, returnArray, "filenames");

		return returnArray;
	}
}
