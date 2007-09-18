package org.griphyn.vdl.karajan.lib.swiftscript;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;

import java.io.*;

public class ExtractInt extends VDLFunction {
	static {
		setArguments(ExtractInt.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		try {
			String fn = argList(filename(stack), true);
			Reader freader = new FileReader(fn);
			BufferedReader breader = new BufferedReader(freader);
			String str = breader.readLine();
			freader.close();
			Double i = new Double(str);

			return RootDataNode.newNode(Types.FLOAT, i);
		} catch(IOException ioe) {
			throw new ExecutionException("reading integer content of file",ioe);
		}
	}
}
