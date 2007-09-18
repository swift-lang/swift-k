package org.griphyn.vdl.karajan.lib.swiftscript;



import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;

import java.io.*;

public class ExtractInt extends VDLFunction {
	static {
		setArguments(ExtractInt.class, new Arg[] { PA_VAR });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		DSHandle handle = null;
		try {
			handle = (DSHandle) PA_VAR.getValue(stack);

			if (!handle.isClosed()) {

				if (logger.isDebugEnabled()) {
					logger.debug("Waiting for " + handle);
				}
				throw new FutureNotYetAvailable(VDLFunction.addFutureListener(stack, handle));
			}

			String fn = argList(filename(handle), true);
			Reader freader = new FileReader(fn);
			BufferedReader breader = new BufferedReader(freader);
			String str = breader.readLine();
			freader.close();
			Double i = new Double(str);

			return RootDataNode.newNode(Types.FLOAT, i);
		} catch(IOException ioe) {
			throw new ExecutionException("reading integer content of file",ioe);
		} catch(HandleOpenException he) {
throw new FutureNotYetAvailable(VDLFunction.addFutureListener(stack, handle));

		}
	}
}
