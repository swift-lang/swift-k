package org.griphyn.vdl.karajan.lib.swiftscript;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;

public class FileName extends VDLFunction {
	static {
		setArguments(FileName.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		String s = argList(filename(stack), true);
		DSHandle result = RootDataNode.newNode(Types.STRING, s);
		int provid = VDLFunction.nextProvenanceID();
		//VDLFunction.logProvenanceParameter(provid, (DSHandle) PA_VAR.getValue(stack), "input");
		//VDLFunction.logProvenanceResult(provid, result, "filename");
		return result;
	}
}
